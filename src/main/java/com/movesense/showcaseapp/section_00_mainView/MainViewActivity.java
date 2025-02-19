package com.movesense.showcaseapp.section_00_mainView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.model.HeartRate;
import com.movesense.showcaseapp.model.LinearAcceleration;
import com.movesense.showcaseapp.model.MdsConnectedDevice;
import com.movesense.showcaseapp.section_01_movesense.MovesenseActivity;
import java.util.Locale;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MainViewActivity extends AppCompatActivity {

    private Button startExerciseSessionButton, postWorkoutResultsButton, settingsButton, signOutButton, connectSensorButton;
    private TextView sensorStatusTv, heartRateDisplay, xAxisDisplay, yAxisDisplay, zAxisDisplay;
    private CompositeDisposable subscriptions;
    private MdsSubscription heartRateSubscription, linearAccelerationSubscription;

    private static final String HEART_RATE_PATH = "Meas/Hr";
    private static final String LINEAR_ACCELERATION_PATH = "Meas/Acc/52"; // Adjust rate as needed
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
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
        xAxisDisplay = findViewById(R.id.xAxisDisplay);
        yAxisDisplay = findViewById(R.id.yAxisDisplay);
        zAxisDisplay = findViewById(R.id.zAxisDisplay);

        // Initialize Rx Subscriptions
        subscriptions = new CompositeDisposable();

        // Button click listeners
        startExerciseSessionButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, ExerciseSessionActivity.class)));

        postWorkoutResultsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, WorkoutResultsActivity.class)));

        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(MainViewActivity.this, SettingsActivity.class)));

        signOutButton.setOnClickListener(view -> {
            startActivity(new Intent(MainViewActivity.this, LoginActivity.class));
            finish();
        });

        // Connect Movesense Sensor Button
        connectSensorButton.setOnClickListener(view -> connectMovesenseSensor());

        // Auto-subscribe if sensor is connected
        checkSensorConnection();
    }

    private void connectMovesenseSensor() {
        Intent intent = new Intent(MainViewActivity.this, MovesenseActivity.class);
        startActivity(intent);
    }

    private void checkSensorConnection() {
        if (MovesenseConnectedDevices.getConnectedDevices().size() > 0) {
            String serial = MovesenseConnectedDevices.getConnectedDevice(0).getSerial();
            sensorStatusTv.setText("Connected to: " + serial);
            connectSensorButton.setVisibility(View.GONE);

            // Subscribe to Heart Rate & Linear Acceleration
            subscribeToHeartRate(serial);
            subscribeToLinearAcceleration(serial);

        } else {
            sensorStatusTv.setText("No sensor connected.");
            connectSensorButton.setVisibility(View.VISIBLE);
        }

        // Monitor connection status
        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Consumer<MdsConnectedDevice>() {
                    @Override
                    public void accept(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() != null) {
                            sensorStatusTv.setText("Connected to: " + mdsConnectedDevice.getSerial());
                            connectSensorButton.setVisibility(View.GONE);

                            // Re-subscribe when reconnected
                            subscribeToHeartRate(mdsConnectedDevice.getSerial());
                            subscribeToLinearAcceleration(mdsConnectedDevice.getSerial());
                        } else {
                            sensorStatusTv.setText("No sensor connected.");
                            connectSensorButton.setVisibility(View.VISIBLE);
                            unsubscribeHeartRate();
                            unsubscribeLinearAcceleration();
                        }
                    }
                }, throwable -> Log.e(TAG, "Error observing sensor connection", throwable)));
    }

    private void subscribeToHeartRate(String serial) {
        if (heartRateSubscription != null) {
            heartRateSubscription.unsubscribe();
        }

        heartRateSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                String.format(Locale.US, "{\"Uri\": \"%s/%s\"}", serial, HEART_RATE_PATH),
                new MdsNotificationListener() {
                    @Override
                    public void onNotification(String data) {
                        Log.d(TAG, "Heart Rate Data Received: " + data);
                        HeartRate heartRate = new Gson().fromJson(data, HeartRate.class);
                        if (heartRate != null) {
                            runOnUiThread(() -> heartRateDisplay.setText(
                                    String.format(Locale.US, "Heart Rate: %.2f bpm", heartRate.body.average)));
                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "Heart rate subscription error", error);
                    }
                });
    }

    private void subscribeToLinearAcceleration(String serial) {
        if (linearAccelerationSubscription != null) {
            linearAccelerationSubscription.unsubscribe();
        }

        linearAccelerationSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                String.format(Locale.US, "{\"Uri\": \"%s/%s\"}", serial, LINEAR_ACCELERATION_PATH),
                new MdsNotificationListener() {
                    @Override
                    public void onNotification(String data) {
                        Log.d(TAG, "Linear Acceleration Data Received: " + data);
                        LinearAcceleration linearAccelerationData = new Gson().fromJson(data, LinearAcceleration.class);
                        if (linearAccelerationData != null && linearAccelerationData.body.array.length > 0) {
                            LinearAcceleration.Array arrayData = linearAccelerationData.body.array[0];

                            runOnUiThread(() -> {
                                xAxisDisplay.setText(String.format(Locale.US, "X: %.6f", arrayData.x));
                                yAxisDisplay.setText(String.format(Locale.US, "Y: %.6f", arrayData.y));
                                zAxisDisplay.setText(String.format(Locale.US, "Z: %.6f", arrayData.z));
                            });
                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "Linear Acceleration subscription error", error);
                    }
                });
    }

    private void unsubscribeHeartRate() {
        if (heartRateSubscription != null) {
            heartRateSubscription.unsubscribe();
            heartRateSubscription = null;
        }
    }

    private void unsubscribeLinearAcceleration() {
        if (linearAccelerationSubscription != null) {
            linearAccelerationSubscription.unsubscribe();
            linearAccelerationSubscription = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSensorConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeHeartRate();
        unsubscribeLinearAcceleration();
        subscriptions.clear();
    }
}
