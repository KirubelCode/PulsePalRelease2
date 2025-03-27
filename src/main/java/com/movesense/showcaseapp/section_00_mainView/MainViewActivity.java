package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.movesense.mds.Mds;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.section_01_movesense.MovesenseActivity;
import com.movesense.showcaseapp.sensor.SensorDataManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import java.util.Locale;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainViewActivity extends AppCompatActivity {

    private Button startExerciseSessionButton, postWorkoutResultsButton, settingsButton, signOutButton, connectSensorButton;
    private TextView sensorStatusTv, heartRateDisplay;
    private CompositeDisposable subscriptions;
    private Disposable dashboardHeartRateDisposable;
    private static final String TAG = "MainViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        // Initialize UI elements
        startExerciseSessionButton = findViewById(R.id.startExerciseSessionButton);
        postWorkoutResultsButton = findViewById(R.id.postWorkoutResultsButton);
        settingsButton = findViewById(R.id.settingsButton);
        signOutButton = findViewById(R.id.signOutButton);
        connectSensorButton = findViewById(R.id.connectSensorButton);
        sensorStatusTv = findViewById(R.id.sensorStatusTv);
        heartRateDisplay = findViewById(R.id.heartRateDisplay);

        subscriptions = new CompositeDisposable();

        // Button click listeners
        startExerciseSessionButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, ExerciseSessionActivity.class)));
        postWorkoutResultsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, WorkoutResultsActivity.class)));
        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, SettingsActivity.class)));
        signOutButton.setOnClickListener(view -> {
            SensorDataManager.getInstance().stopSubscriptions();
            startActivity(new Intent(MainViewActivity.this, LoginActivity.class));
            finish();
        });
        connectSensorButton.setOnClickListener(view -> connectMovesenseSensor());

        checkSensorConnection();
    }

    private void connectMovesenseSensor() {
        startActivity(new Intent(MainViewActivity.this, MovesenseActivity.class));
    }

    private void checkSensorConnection() {
        if (MovesenseConnectedDevices.getConnectedDevices().size() > 0) {
            String serial = MovesenseConnectedDevices.getConnectedDevice(0).getSerial();
            sensorStatusTv.setText("Connected to: " + serial);
            connectSensorButton.setVisibility(View.GONE);
            // Start the centralized sensor subscriptions
            SensorDataManager.getInstance().startSubscriptions(Mds.builder().build(this), serial);
        } else {
            sensorStatusTv.setText("No sensor connected.");
            connectSensorButton.setVisibility(View.VISIBLE);
        }

        // Monitor connection status changes
        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(mdsConnectedDevice -> {
                    if (mdsConnectedDevice.getConnection() != null) {
                        sensorStatusTv.setText("Connected to: " + mdsConnectedDevice.getSerial());
                        connectSensorButton.setVisibility(View.GONE);
                        SensorDataManager.getInstance().startSubscriptions(Mds.builder().build(this), mdsConnectedDevice.getSerial());
                    } else {
                        sensorStatusTv.setText("No sensor connected.");
                        connectSensorButton.setVisibility(View.VISIBLE);
                        SensorDataManager.getInstance().stopSubscriptions();
                    }
                }, throwable -> Log.e(TAG, "Error observing sensor connection", throwable)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSensorConnection();
        // Subscribe only to heart rate data for the dashboard.
        dashboardHeartRateDisposable = SensorDataManager.getInstance().getHeartRateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hr ->
                        heartRateDisplay.setText(String.format(Locale.US, "Heart Rate: %.2f bpm", hr.body.average))
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dashboardHeartRateDisposable != null) {
            dashboardHeartRateDisposable.dispose();
            dashboardHeartRateDisposable = null;
        }
        // Do not stop sensor subscriptions here; they remain active.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
    }
}
