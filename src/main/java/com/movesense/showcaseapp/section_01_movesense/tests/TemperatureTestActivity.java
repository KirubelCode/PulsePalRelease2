package com.movesense.showcaseapp.section_01_movesense.tests;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.BaseActivity;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.ConnectionLostDialog;
import com.movesense.showcaseapp.csv.CsvLogger;
import com.movesense.showcaseapp.model.Temperature;
import com.movesense.showcaseapp.model.TemperatureSubscribeModel;
import com.movesense.showcaseapp.utils.FormatHelper;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class TemperatureTestActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    private final String LOG_TAG = TemperatureTestActivity.class.getSimpleName();
    private final String TEMPERATURE_PATH_GET = "/Meas/Temp";
    private final String TEMPERATURE_PATH_SUBSCRIBE = "Meas/Temp";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    public static final String URI_SCHEME_PREFIX = "suunto://";
    private final double KELVIN_ABSOLUTE_ZERO = 274.15;
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    private MdsSubscription mdsSubscription;
    @BindView(R.id.temperature_switch) SwitchCompat temperatureSwitch;
    @BindView(R.id.temperature_switch_layout) LinearLayout temperatureSwitchLayout;
    @BindView(R.id.temperature_get_button) Button temperatureGetButton;
    @BindView(R.id.temperature_kelvin_textView) TextView temperatureKelvinTextView;
    @BindView(R.id.temperature_celsius_textView) TextView temperatureCelsiusTextView;
    private AlertDialog alertDialog;
    private CsvLogger mCsvLogger;
    private boolean isLogSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_test);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Temperature");
        }

        mCsvLogger = new CsvLogger(getApplicationContext());

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading_information)
                .create();

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());
    }


    @OnCheckedChanged(R.id.temperature_switch)
    public void onCheckedChange(final CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {

            isLogSaved = false;

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0).getSerial(),
                            TEMPERATURE_PATH_SUBSCRIBE), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(LOG_TAG, "Temperature onNotification() : " + data);

                            TemperatureSubscribeModel temperature = new Gson().fromJson(data, TemperatureSubscribeModel.class);

                            if (temperature.getBody() != null) {
                                temperatureKelvinTextView.setText(String.format(getString(R.string.kelvin) + " %.6f", temperature.getBody().measurement));
                                temperatureCelsiusTextView.setText(String.format(getString(R.string.celsius) + " %.6f", (temperature.getBody().measurement - KELVIN_ABSOLUTE_ZERO)));

                                mCsvLogger.appendHeader("Timestamp (ms), Temp (Kelvin)");

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "%d,%.6f", temperature.getBody().getTimestamp(),
                                        temperature.getBody().measurement));
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "Temperature onError(", error);

                            Toast.makeText(TemperatureTestActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            compoundButton.setChecked(false);
                        }
                    });
        } else {
            unSubscribe();
        }
    }

    @OnClick(R.id.temperature_get_button)
    public void onClick() {

        alertDialog.show();

        Mds.builder().build(this).get(URI_SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + TEMPERATURE_PATH_GET,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        alertDialog.dismiss();
                        Log.d(LOG_TAG, "Temperature onSuccess(): " + data);

                        Temperature temperature = new Gson().fromJson(data, Temperature.class);

                        if (temperature != null) {
                            temperatureKelvinTextView.setText(String.format(getString(R.string.kelvin) + " %.6f", temperature.content.measurement));
                            temperatureCelsiusTextView.setText(String.format(getString(R.string.celsius) + " %.6f", (temperature.content.measurement - KELVIN_ABSOLUTE_ZERO)));
                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        alertDialog.dismiss();
                        Log.e(LOG_TAG, "Temperature onError(", error);
                    }
                });
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
//
//                }
//            }
//        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(LOG_TAG, "onDisconnect: " + s);
        if (isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(TemperatureTestActivity.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.d(LOG_TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
