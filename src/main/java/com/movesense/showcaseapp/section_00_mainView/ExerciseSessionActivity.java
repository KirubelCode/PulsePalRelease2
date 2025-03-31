package com.movesense.showcaseapp.section_00_mainView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.sensor.SensorDataManager;
import com.movesense.showcaseapp.model.HeartRate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ExerciseSessionActivity extends AppCompatActivity {
    private TextView heartRateDisplay, stepCountDisplay, distanceDisplay, paceDisplay, caloriesDisplay;
    private Chronometer chronometer;
    private Button pauseButton, stopButton;
    private Disposable heartRateDisposable, stepDataDisposable;
    private boolean isPaused = false;
    private long pauseOffset = 0;
    private float smoothedHeartRate = 0;
    private boolean firstHR = true;

    private float userWeight;
    private int userAge;
    private String userGender;
    private float totalCalories = 0f;
    private long lastCalorieTimestamp = 0;
    private float lastPace = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_session);

        heartRateDisplay = findViewById(R.id.exerciseHeartRateDisplay);
        stepCountDisplay = findViewById(R.id.stepCountDisplay);
        distanceDisplay = findViewById(R.id.distanceDisplay);
        paceDisplay = findViewById(R.id.paceDisplay);
        caloriesDisplay = findViewById(R.id.caloriesDisplay);
        chronometer = findViewById(R.id.chronometer);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

        SharedPreferences prefs = getSharedPreferences("PulsePalPrefs", MODE_PRIVATE);
        if (!prefs.contains("weight") || !prefs.contains("age") || !prefs.contains("gender")) {
            Toast.makeText(this, "Missing user data. Please login again.", Toast.LENGTH_LONG).show();
            finish(); // or redirect to login
            return;
        }

        userWeight = prefs.getFloat("weight", -1f);
        userAge = prefs.getInt("age", -1);
        userGender = prefs.getString("gender", null);


        pauseButton.setOnClickListener(v -> {
            if (isPaused) {
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.start();
                pauseButton.setText("Pause");
                isPaused = false;
                SensorDataManager.getInstance().resumeStepCounter();
            } else {
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                chronometer.stop();
                pauseButton.setText("Resume");
                isPaused = true;
                SensorDataManager.getInstance().pauseStepCounter();
            }
        });

        stopButton.setOnClickListener(v -> {
            SensorDataManager.getInstance().stopExerciseSession();
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorDataManager.getInstance().startExerciseSession();
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();

        heartRateDisposable = SensorDataManager.getInstance().getHeartRateObservable()
                .sample(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hr -> {
                    float newHR = hr.body.average;
                    if (firstHR) {
                        smoothedHeartRate = newHR;
                        firstHR = false;
                    } else {
                        smoothedHeartRate = 0.2f * newHR + 0.8f * smoothedHeartRate;
                    }
                    heartRateDisplay.setText(String.format(Locale.US, "Heart Rate: %.2f bpm", smoothedHeartRate));
                });

        stepDataDisposable = SensorDataManager.getInstance().getStepDataObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stepData -> {
                    stepCountDisplay.setText(String.format(Locale.US, "Steps: %d", stepData.stepCount));
                    distanceDisplay.setText(String.format(Locale.US, "Distance: %.2f m", stepData.distance));

                    long activeTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                    float elapsedMinutes = activeTime / 60000f;
                    float distanceKm = stepData.distance / 1000f;

                    if (distanceKm > 0) {
                        lastPace = elapsedMinutes / distanceKm;
                        paceDisplay.setText(String.format(Locale.US, "Pace: %.2f min/km", lastPace));
                    } else {
                        paceDisplay.setText("Pace: N/A");
                    }

                    long currentTime = System.currentTimeMillis();
                    if (lastCalorieTimestamp == 0) lastCalorieTimestamp = currentTime;
                    long elapsedCalorieMillis = currentTime - lastCalorieTimestamp;

                    if (!isPaused && elapsedCalorieMillis >= 1000) {
                        float caloriesPerMinute = smoothedHeartRate > 90
                                ? calculateCaloriesWithHeartRate(smoothedHeartRate, userWeight, userAge, userGender)
                                : calculateCaloriesWithMET(lastPace, userWeight);

                        float caloriesThisPeriod = (caloriesPerMinute / 60f) * (elapsedCalorieMillis / 1000f);
                        totalCalories += caloriesThisPeriod;
                        lastCalorieTimestamp = currentTime;
                    }


                    caloriesDisplay.setText(String.format(Locale.US, "Calories: %.1f kcal", totalCalories));
                });
    }

    private float calculateCaloriesWithHeartRate(float heartRate, float weightKg, int age, String gender) {
        if (heartRate <= 0) return 0;

        double caloriesPerMin;
        if (gender.equalsIgnoreCase("female")) {
            caloriesPerMin = ((age * 0.074) - (weightKg * 0.05741) + (heartRate * 0.4472) - 20.4022) / 4.184;
        } else {
            caloriesPerMin = ((age * 0.2017) - (weightKg * 0.09036) + (heartRate * 0.6309) - 55.0969) / 4.184;
        }

        return Math.max((float) caloriesPerMin, 0);
    }

    private float calculateCaloriesWithMET(float pace, float weightKg) {
        float met;
        if (pace == 0) return 0;
        if (pace > 9f) {
            met = 3.5f; // light walk
        } else if (pace > 6f) {
            met = 6.0f; // brisk walk
        } else {
            met = 8.3f; // jog
        }

        float minutes = 1f / 60f;
        return met * weightKg * minutes;
    }

    @Override
    protected void onPause() {
        super.onPause();
        chronometer.stop();
        if (heartRateDisposable != null) heartRateDisposable.dispose();
        if (stepDataDisposable != null) stepDataDisposable.dispose();
    }
}
