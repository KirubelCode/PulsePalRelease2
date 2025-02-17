package com.movesense.showcaseapp.section_01_movesense.tests;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.BaseActivity;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.ConnectionLostDialog;
import com.movesense.showcaseapp.csv.CsvLogger;
import com.movesense.showcaseapp.model.HeartRate;
import com.movesense.showcaseapp.model.LinearAcceleration;
import com.movesense.showcaseapp.utils.FormatHelper;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class HeartRateTestActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    private final String LOG_TAG = HeartRateTestActivity.class.getSimpleName();
    private final String HEART_RATE_PATH = "Meas/Hr";
    private final String TIME_GET_PATH = "/Time";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final String LINEAR_ACCELERATION_PATH = "Meas/Acc/13";
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.heart_rate_rr_value_textView) TextView mHeartRateRrValueTextView;
    @BindView(R.id.heart_rate_bpm_value_textView) TextView mHeartRateBpmValueTextView;
    private MdsSubscription mdsSubscriptionForTimestamp;
    private MdsSubscription mdsSubscription;
    private CsvLogger mCsvLogger;

    @BindView(R.id.heart_rate_switch) SwitchCompat heartRateSwitch;
    private boolean isLogSaved = false;
    private long timestamp;
    private final String TAG = HeartRateTestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_test);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Heart Rate");
        }

        mCsvLogger = new CsvLogger(getApplicationContext());

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());
    }

    @OnCheckedChanged(R.id.heart_rate_switch)
    public void onCheckedChange(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {

            isLogSaved = false;

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            mdsSubscriptionForTimestamp = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), LINEAR_ACCELERATION_PATH), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            LinearAcceleration linearAccelerationData = new Gson().fromJson(
                                    data, LinearAcceleration.class);

                            if (linearAccelerationData != null) {
                                timestamp = linearAccelerationData.body.timestamp;
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "onError(): ", error);
                        }
                    });

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0).getSerial(),
                            HEART_RATE_PATH)
                    , new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(LOG_TAG, "Heart rate onNotification() : " + data);
                            HeartRate heartRate = new Gson().fromJson(data, HeartRate.class);

                            if (heartRate != null) {
                                mHeartRateRrValueTextView.setText(String.format(Locale.getDefault(),
                                        "RR [ms]: %d", heartRate.body.rrData[0]));

                                mHeartRateBpmValueTextView.setText(String.format(Locale.getDefault(),
                                        "Beat interval [bpm]: %.2f", heartRate.body.average));

                                mCsvLogger.appendHeader("Timestamp,HR: (Beats per minute), RR: (ms)");

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "%d,%.2f,%d", timestamp,heartRate.body.average, heartRate.body.rrData[0]));
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "Heart rate error", error);
                        }
                    });

        } else {
            unSubscribe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    private void unSubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }

        if (mdsSubscriptionForTimestamp != null) {
            mdsSubscriptionForTimestamp.unsubscribe();
            mdsSubscriptionForTimestamp = null;
        }
        
        if (!isLogSaved) {
            mCsvLogger.finishSavingLogs(this, LOG_TAG);
            isLogSaved = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (requestCode == LogsManager.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
//            // if request is cancelled grantResults array is empty
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(LOG_TAG, "onDisconnect: " + s);
        if (!isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(HeartRateTestActivity.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(LOG_TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
