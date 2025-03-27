package com.movesense.showcaseapp.section_00_mainView;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.model.HeartRate;
import com.movesense.showcaseapp.sensor.SensorDataManager;

public class ExerciseSessionActivity extends AppCompatActivity {
    private TextView heartRateDisplay, stepCountDisplay, distanceDisplay, paceDisplay;
    private Chronometer chronometer;
    private Button pauseButton, stopButton;
    private Disposable heartRateDisposable, stepDataDisposable;
    private boolean isPaused = false;
    private long pauseOffset = 0;

    // Variables for heart rate smoothing
    private float smoothedHeartRate = 0;
    private boolean firstHR = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_session);

        heartRateDisplay = findViewById(R.id.exerciseHeartRateDisplay);
        stepCountDisplay = findViewById(R.id.stepCountDisplay);
        distanceDisplay = findViewById(R.id.distanceDisplay);
        paceDisplay = findViewById(R.id.paceDisplay);
        chronometer = findViewById(R.id.chronometer);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

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

        // Increase heart rate update frequency to 2Hz for faster response,
        // then apply exponential smoothing.
        heartRateDisposable = SensorDataManager.getInstance().getHeartRateObservable()
                .sample(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hr -> {
                    float newHR = hr.body.average;
                    if (firstHR) {
                        smoothedHeartRate = newHR;
                        firstHR = false;
                    } else {
                        // α=0.2: 20% new value, 80% previous smoothed value.
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
                        float pace = elapsedMinutes / distanceKm;
                        paceDisplay.setText(String.format(Locale.US, "Pace: %.2f min/km", pace));
                    } else {
                        paceDisplay.setText("Pace: N/A");
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        chronometer.stop();
        if (heartRateDisposable != null) {
            heartRateDisposable.dispose();
            heartRateDisposable = null;
        }
        if (stepDataDisposable != null) {
            stepDataDisposable.dispose();
            stepDataDisposable = null;
        }
    }
}
