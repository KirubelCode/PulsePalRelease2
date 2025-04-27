package com.movesense.showcaseapp.section_00_mainView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.sensor.SensorDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExerciseSessionActivity extends AppCompatActivity {
    private static final String TAG = "ExerciseSessionActivity";
    private static final String BASE_URL = "https://pulsepal.store/";

    // UI elements
    private TextView heartRateDisplay, stepCountDisplay, distanceDisplay, paceDisplay, caloriesDisplay;
    private Chronometer chronometer;
    private Button pauseButton, stopButton, setGoalButton;
    private ProgressBar goalProgressBar, goalProgressBar2, goalProgressBar3;
    private TextView goalProgressText, goalProgressText2, goalProgressText3;
    private KonfettiView confettiView;

    // Sensor subscriptions
    private Disposable heartRateDisposable, stepDataDisposable;

    // Session state
    private boolean isPaused;
    private long pauseOffset;
    private float smoothedHeartRate;
    private boolean firstHR;
    private long lastCalorieTimestamp;
    private float lastPace, totalCalories;

    // User info
    private float userWeight;
    private int userAge;
    private String userGender;

    // Networking & session
    private int sessionId;
    private Handler handler;
    private Runnable dataPointRunnable;
    private ApiService apiService;

    // Goal tracking
    private List<GoalsResponse.Goal> sessionGoals = new ArrayList<>();
    private int secondsInZone;
    private long lastZoneCheckTs;
    private boolean navigatingToGoal;
    private boolean[] goalCompleted = new boolean[3];

    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_session);

        // Bind UI
        heartRateDisplay   = findViewById(R.id.exerciseHeartRateDisplay);
        stepCountDisplay   = findViewById(R.id.stepCountDisplay);
        distanceDisplay    = findViewById(R.id.distanceDisplay);
        paceDisplay        = findViewById(R.id.paceDisplay);
        caloriesDisplay    = findViewById(R.id.caloriesDisplay);
        chronometer        = findViewById(R.id.chronometer);
        pauseButton        = findViewById(R.id.pauseButton);
        stopButton         = findViewById(R.id.stopButton);
        setGoalButton      = findViewById(R.id.setGoalButton);
        goalProgressBar    = findViewById(R.id.goalProgressBar);
        goalProgressText   = findViewById(R.id.goalProgressText);
        goalProgressBar2   = findViewById(R.id.goalProgressBar2);
        goalProgressText2  = findViewById(R.id.goalProgressText2);
        goalProgressBar3   = findViewById(R.id.goalProgressBar3);
        goalProgressText3  = findViewById(R.id.goalProgressText3);
        confettiView       = findViewById(R.id.confettiView);

        // Initialize state
        isPaused         = false;
        pauseOffset      = 0;
        smoothedHeartRate= 0;
        firstHR          = true;
        totalCalories    = 0;
        lastPace         = 0;
        lastCalorieTimestamp = 0;
        handler          = new Handler();
        navigatingToGoal = false;
        secondsInZone    = 0;
        lastZoneCheckTs  = System.currentTimeMillis();
        Arrays.fill(goalCompleted, false);

        // Load user prefs
        SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
        if (!prefs.contains("weight") || !prefs.contains("age")
                || !prefs.contains("gender") || !prefs.contains("userEmail")) {
            Toast.makeText(this, "Missing user data. Please login again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        userWeight  = prefs.getFloat("weight", -1f);
        userAge     = prefs.getInt("age", -1);
        userGender  = prefs.getString("gender", null);
        String userEmail = prefs.getString("userEmail", null);

        // Setup API client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Button handlers
        pauseButton.setOnClickListener(v -> togglePause());
        stopButton.setOnClickListener(v -> stopSession());
        setGoalButton.setOnClickListener(v -> {
            if (sessionId <= 0) {
                Toast.makeText(this, "Session not initialized yet",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionGoals.size() >= 3) {
                Toast.makeText(this, "You can set up to 3 goals only.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isPaused) togglePause();   // pause before navigating
            navigatingToGoal = true;
            Intent i = new Intent(this, GoalTrackActivity.class);
            i.putExtra("session_id", sessionId);
            startActivity(i);
        });

        // Create a new session on server
        apiService.createSession(new CreateSessionRequest(userEmail))
                .enqueue(new Callback<ResponseModel>() {
                    @Override public void onResponse(Call<ResponseModel> call,
                                                     Response<ResponseModel> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            sessionId = resp.body().getSession_id();

                            // Keep CPU running when app in background
                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                    "PulsePal:SessionWakeLock");
                            wakeLock.acquire();

                            // Start collecting data
                            SensorDataManager.getInstance().startExerciseSession();
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            chronometer.start();
                            lastCalorieTimestamp = System.currentTimeMillis();
                            subscribeSensorStreams();

                            // Schedule periodic data sends
                            handler = new Handler();
                            dataPointRunnable = () -> {
                                sendDataPoint();            // send metrics to backend
                                handler.postDelayed(dataPointRunnable, 5000);
                            };
                            handler.post(dataPointRunnable);

                            fetchSessionGoal();            // load goals to track
                        }
                    }
                    @Override public void onFailure(Call<ResponseModel> call, Throwable t) { }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigatingToGoal) {
            navigatingToGoal = false;
            fetchSessionGoal();                // refresh goals
            if (!isPaused) {
                chronometer.start();
                subscribeSensorStreams();
                handler.post(dataPointRunnable);
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Leave this session? Your progress will be lost.")
                .setPositiveButton("Yes", (d, w) -> super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    // Pause/resume everything
    private void togglePause() {
        if (isPaused) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            pauseButton.setText("Pause");
            isPaused = false;
            SensorDataManager.getInstance().resumeStepCounter();
            lastCalorieTimestamp = System.currentTimeMillis();
            subscribeSensorStreams();
        } else {
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            chronometer.stop();
            pauseButton.setText("Resume");
            isPaused = true;
            SensorDataManager.getInstance().pauseStepCounter();
            unsubscribeSensorStreams();
        }
    }

    // Confirm and end session
    private void stopSession() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to end the session?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
                    handler.removeCallbacks(dataPointRunnable);

                    // Gather final stats
                    int   finalSteps  = SensorDataManager.getInstance().getLatestStepCount();
                    float finalDist   = SensorDataManager.getInstance().getLatestDistance();
                    float finalPace   = lastPace;
                    float finalCals   = totalCalories;
                    int   finalZone   = secondsInZone;

                    // Persist summary locally
                    SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putInt   ("summary_" + sessionId + "_steps",    finalSteps)
                            .putFloat ("summary_" + sessionId + "_distance", finalDist)
                            .putFloat ("summary_" + sessionId + "_pace",     finalPace)
                            .putFloat ("summary_" + sessionId + "_calories", finalCals)
                            .putInt   ("summary_" + sessionId + "_zone",     finalZone)
                            .apply();

                    finalizeSession();  // send final stats to server
                    SensorDataManager.getInstance().stopExerciseSession();

                    // Show results screen
                    Intent i = new Intent(this, WorkoutResultsActivity.class);
                    i.putExtra("session_id",      sessionId);
                    i.putExtra("final_steps",     finalSteps);
                    i.putExtra("final_distance",  finalDist);
                    i.putExtra("final_pace",      finalPace);
                    i.putExtra("final_calories",  finalCals);
                    i.putExtra("final_timeInZone",finalZone);
                    startActivity(i);

                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Subscribe to heart rate and goal-tracking
    private void subscribeSensorStreams() {
        heartRateDisposable = SensorDataManager.getInstance()
                .getHeartRateObservable()
                .sample(500, TimeUnit.MILLISECONDS)         // throttle to 500ms
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hr -> {
                    float newHR = hr.body.average;
                    if (firstHR) { smoothedHeartRate = newHR; firstHR = false; }
                    else { smoothedHeartRate = 0.2f*newHR + 0.8f*smoothedHeartRate; }
                    heartRateDisplay.setText(
                            String.format(Locale.US, "HR: %.2f bpm", smoothedHeartRate)
                    );

                    long now = System.currentTimeMillis();
                    for (int idx=0; idx<sessionGoals.size() && idx<3; idx++) {
                        GoalsResponse.Goal g = sessionGoals.get(idx);
                        if (!"heart_rate".equals(g.metric)) continue;
                        boolean inZone = ">=".equals(g.operator)
                                ? smoothedHeartRate >= g.target_value
                                : smoothedHeartRate <= g.target_value;
                        if (inZone) {
                            secondsInZone += (now-lastZoneCheckTs)/1000;
                            if (secondsInZone > g.duration_seconds)
                                secondsInZone = g.duration_seconds;
                        }
                        lastZoneCheckTs = now;

                        ProgressBar bar = idx==0 ? goalProgressBar
                                : idx==1 ? goalProgressBar2 : goalProgressBar3;
                        TextView txt = idx==0 ? goalProgressText
                                : idx==1 ? goalProgressText2 : goalProgressText3;

                        int max = g.duration_seconds;
                        int prog = secondsInZone;
                        bar.setMax(max); bar.setProgress(prog);

                        if (prog>=max && !goalCompleted[idx]) {
                            goalCompleted[idx]=true;
                            triggerConfetti();
                        }

                        int pct = max>0 ? (int)(100f*prog/max) : 0;
                        String done = String.format(Locale.US, "%d:%02d", prog/60, prog%60);
                        String tot  = String.format(Locale.US, "%d:%02d", max/60, max%60);
                        txt.setText(String.format(
                                Locale.US, "Time in HR zone: %s / %s (%d%%)", done, tot, pct
                        ));
                        break;
                    }
                });

        stepDataDisposable = SensorDataManager.getInstance()
                .getStepDataObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stepData -> {
                    stepCountDisplay.setText(
                            String.format(Locale.US, "Steps: %d", stepData.stepCount)
                    );
                    distanceDisplay.setText(
                            String.format(Locale.US, "Distance: %.2f m", stepData.distance)
                    );

                    if (!isPaused) {
                        long activeTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                        float elapsedMin = activeTime/60000f;
                        float km = stepData.distance/1000f;
                        if (km>0) lastPace = elapsedMin/km;
                        paceDisplay.setText(
                                km>0 ? String.format(Locale.US, "Pace: %.2f min/km", lastPace)
                                        : "Pace: N/A"
                        );

                        long now = System.currentTimeMillis();
                        if (lastCalorieTimestamp==0) lastCalorieTimestamp=now;
                        long dt = now-lastCalorieTimestamp;
                        if (dt>=1000) {
                            float cpm = calculateCaloriesWithHeartRate(
                                    smoothedHeartRate, userWeight, userAge, userGender
                            );
                            totalCalories += (cpm/60f)*(dt/1000f);
                            lastCalorieTimestamp = now;
                        }
                    }

                    caloriesDisplay.setText(
                            String.format(Locale.US, "Calories: %.1f kcal", totalCalories)
                    );
                    updateGoalProgress(stepData.stepCount, stepData.distance);
                });
    }

    private void unsubscribeSensorStreams() {
        if (heartRateDisposable != null) heartRateDisposable.dispose();
        if (stepDataDisposable  != null) stepDataDisposable.dispose();
    }

    // Load goals and initialize progress bars
    private void fetchSessionGoal() {
        JsonObject body = new JsonObject();
        body.addProperty("session_id", sessionId);
        apiService.getSessionGoals(body)
                .enqueue(new Callback<GoalsResponse>() {
                    @Override public void onResponse(Call<GoalsResponse> c, Response<GoalsResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().success) {
                            sessionGoals = r.body().goals;
                            secondsInZone = 0;
                            lastZoneCheckTs = System.currentTimeMillis();
                            Arrays.fill(goalCompleted, false);
                            for (int i=0; i<sessionGoals.size() && i<3; i++) {
                                GoalsResponse.Goal g = sessionGoals.get(i);
                                ProgressBar bar = i==0 ? goalProgressBar
                                        : i==1 ? goalProgressBar2 : goalProgressBar3;
                                TextView txt = i==0 ? goalProgressText
                                        : i==1 ? goalProgressText2 : goalProgressText3;
                                int max = "heart_rate".equals(g.metric)
                                        ? g.duration_seconds
                                        : (int)g.target_value;
                                bar.setMax(max); bar.setProgress(0);
                                String label = "heart_rate".equals(g.metric)
                                        ? "Time in HR zone"
                                        : g.metric.equals("distance") ? "Distance"
                                        : g.metric.equals("calories") ? "Calories"
                                        : g.metric.equals("pace") ? "Pace" : "Steps";
                                String tot = "heart_rate".equals(g.metric)
                                        ? String.format(Locale.US, "%d:%02d", max/60, max%60)
                                        : String.valueOf(max);
                                txt.setText(
                                        String.format(Locale.US, "%s: 0 / %s (0%%)", label, tot)
                                );
                            }
                        }
                    }
                    @Override public void onFailure(Call<GoalsResponse> c, Throwable t) {}
                });
    }

    // Update non-HR goal progress
    private void updateGoalProgress(int steps, float distance) {
        if (sessionGoals.isEmpty()) return;
        for (int i=0; i<sessionGoals.size() && i<3; i++) {
            GoalsResponse.Goal g = sessionGoals.get(i);
            if ("heart_rate".equals(g.metric)) continue;
            ProgressBar bar = i==0 ? goalProgressBar
                    : i==1 ? goalProgressBar2 : goalProgressBar3;
            TextView txt = i==0 ? goalProgressText
                    : i==1 ? goalProgressText2 : goalProgressText3;
            int max = bar.getMax(), prog = 0;
            switch (g.metric) {
                case "calories": prog = Math.min((int)totalCalories, max); break;
                case "steps":    prog = Math.min(steps, max); break;
                case "distance": prog = Math.min((int)distance, max); break;
                case "pace":
                    boolean hit = "<=".equals(g.operator)
                            ? lastPace <= g.target_value
                            : lastPace >= g.target_value;
                    prog = hit ? max : 0; break;
            }
            bar.setProgress(prog);
            if (prog>=max && !goalCompleted[i]) {
                goalCompleted[i] = true;
                triggerConfetti();
            }
            int pct = max>0 ? (int)(100f*prog/max) : 0;
            String unit = g.metric.equals("distance") ? "m"
                    : g.metric.equals("calories") ? "kcal"
                    : g.metric.equals("pace")      ? "min/km"
                    : "steps";
            txt.setText(String.format(Locale.US, "%d / %d %s (%d%%)", prog, max, unit, pct));
        }
    }

    // Show confetti when goal achieved
    private void triggerConfetti() {
        confettiView.build()
                .addColors(Color.YELLOW, Color.MAGENTA, Color.GREEN)
                .setDirection(0.0, 359.0)
                .setSpeed(2f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE)
                .addSizes(new Size(12, 5))
                .streamFor(300, 3000L);
    }

    // Send live metrics periodically
    private void sendDataPoint() {
        apiService.insertDataPoint(new DataPointRequest(
                sessionId,
                System.currentTimeMillis(),
                smoothedHeartRate,
                SensorDataManager.getInstance().getLatestStepCount(),
                SensorDataManager.getInstance().getLatestDistance(),
                lastPace,
                Math.round(totalCalories * 10f) / 10f
        )).enqueue(new Callback<ResponseModel>() {
            @Override public void onResponse(Call<ResponseModel> c, Response<ResponseModel> r) {}
            @Override public void onFailure(Call<ResponseModel> c, Throwable t) {}
        });
    }

    // Calculate calories from HR
    private float calculateCaloriesWithHeartRate(float hr, float wt, int age, String gender) {
        if (hr <= 0) return 0;
        double cpm = "female".equalsIgnoreCase(gender)
                ? ((age*.074)-(wt*.05741)+(hr*.4472)-20.4022)/4.184
                : ((age*.2017)-(wt*.09036)+(hr*.6309)-55.0969)/4.184;
        return (float)Math.max(cpm, 0);
    }

    // Finalise session and send summary
    private void finalizeSession() {
        float duration = (SystemClock.elapsedRealtime() - chronometer.getBase())/60000f;
        if (duration<0.1f || sessionId<=0) {
            Toast.makeText(this,
                    "Session not saved (too short or uninitialized)",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.finalizeSession(new FinalizeSessionRequest(
                sessionId,
                duration,
                SensorDataManager.getInstance().getLatestStepCount(),
                SensorDataManager.getInstance().getLatestDistance(),
                lastPace, smoothedHeartRate, totalCalories
        )).enqueue(new Callback<ResponseModel>() {
            @Override public void onResponse(Call<ResponseModel> c, Response<ResponseModel> r) {}
            @Override public void onFailure(Call<ResponseModel> c, Throwable t) {}
        });
    }
}
