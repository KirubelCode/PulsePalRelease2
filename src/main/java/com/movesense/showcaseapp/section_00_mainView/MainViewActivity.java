package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.section_01_movesense.MovesenseActivity;
import com.movesense.showcaseapp.sensor.SensorDataManager;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MainViewActivity extends AppCompatActivity {

    private static final String TAG = "MainViewActivity";
    private static final String PREFS = "PulsePalPrefs";
    private static final String KEY_SERIAL = "sensorSerial";

    private Button startExerciseSessionButton;
    private Button postWorkoutResultsButton;
    private Button settingsButton;
    private Button signOutButton;
    private Button connectSensorButton;
    private TextView sensorStatusTv;
    private TextView heartRateDisplay;

    private CompositeDisposable subscriptions;
    private Disposable dashboardHeartRateDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        // Retrieves and assigns all the dashboard’s buttons and display TextViews from the layout
        startExerciseSessionButton   = findViewById(R.id.startExerciseSessionButton);
        postWorkoutResultsButton     = findViewById(R.id.postWorkoutResultsButton);
        settingsButton               = findViewById(R.id.settingsButton);
        signOutButton                = findViewById(R.id.signOutButton);
        connectSensorButton          = findViewById(R.id.connectSensorButton);
        sensorStatusTv               = findViewById(R.id.sensorStatusTv);
        heartRateDisplay             = findViewById(R.id.heartRateDisplay);

        // Manage and dispose of rxJava subscriptions
        subscriptions = new CompositeDisposable();

        // Auto‐reconnect if we have a saved serial
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(KEY_SERIAL, null);
        if (saved != null) {
            sensorStatusTv.setText("Connected to: " + saved);
            connectSensorButton.setVisibility(Button.GONE);
            SensorDataManager.getInstance()
                    .startSubscriptions(Mds.builder().build(this), saved);
        } else {
            connectSensorButton.setVisibility(Button.VISIBLE);
        }

        startExerciseSessionButton.setOnClickListener(v ->
                startActivity(new Intent(MainViewActivity.this, ExerciseSessionActivity.class))
        );

        postWorkoutResultsButton.setOnClickListener(v ->
                startActivity(new Intent(MainViewActivity.this, WorkoutResultsActivity.class))
        );

        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(MainViewActivity.this, SettingsActivity.class))
        );

        signOutButton.setOnClickListener(v -> {
            SensorDataManager.getInstance().stopSubscriptions();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_SERIAL).apply();
            startActivity(new Intent(MainViewActivity.this, LoginActivity.class));
            finish();
        });

        // Connect to sensor button
        connectSensorButton.setOnClickListener(v ->
                startActivity(new Intent(MainViewActivity.this, MovesenseActivity.class))
        );

        checkSensorConnection();
    }

    // Check sensor connection by checking singleton instance
    private void checkSensorConnection() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        if (MovesenseConnectedDevices.getConnectedDevices().size() > 0) {
            String serial = MovesenseConnectedDevices.getConnectedDevice(0).getSerial();
            sensorStatusTv.setText("Connected to: " + serial);
            connectSensorButton.setVisibility(Button.GONE);
            SensorDataManager.getInstance()
                    .startSubscriptions(Mds.builder().build(this), serial);
            prefs.edit().putString(KEY_SERIAL, serial).apply();
        } else {
            sensorStatusTv.setText("No sensor connected.");
            connectSensorButton.setVisibility(Button.VISIBLE);
            SensorDataManager.getInstance().stopSubscriptions();
            prefs.edit().remove(KEY_SERIAL).apply();
        }

        // Observes subscription connections from the movesense device
        subscriptions.add(
                MdsRx.Instance.connectedDeviceObservable()
                        .subscribe(device -> {
                            SharedPreferences.Editor editor = prefs.edit();
                            if (device.getConnection() != null) {
                                String s = device.getSerial();
                                sensorStatusTv.setText("Connected to: " + s);
                                connectSensorButton.setVisibility(Button.GONE);
                                SensorDataManager.getInstance()
                                        .startSubscriptions(Mds.builder().build(this), s);
                                editor.putString(KEY_SERIAL, s);
                            } else {
                                sensorStatusTv.setText("No sensor connected.");
                                connectSensorButton.setVisibility(Button.VISIBLE);
                                SensorDataManager.getInstance().stopSubscriptions();
                                editor.remove(KEY_SERIAL);
                            }
                            editor.apply();
                        }, throwable -> Log.e(TAG, "Error observing sensor connection", throwable))
        );
    }


    @Override
    protected void onResume() {
        super.onResume();
        dashboardHeartRateDisposable = SensorDataManager.getInstance()
                .getHeartRateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hr ->
                        heartRateDisplay.setText(
                                String.format(Locale.US, "Heart Rate: %.2f bpm", hr.body.average)
                        )
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dashboardHeartRateDisposable != null) {
            dashboardHeartRateDisposable.dispose();
            dashboardHeartRateDisposable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
    }
}
