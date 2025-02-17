package com.movesense.showcaseapp.section_01_movesense.tests;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.csv.CsvLogger;
import com.movesense.showcaseapp.model.ImuModel;
import com.movesense.showcaseapp.model.InfoResponse;
import com.movesense.showcaseapp.utils.FormatHelper;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;

public class ImuActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    private final String TAG = ImuActivity.class.getSimpleName();

    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final String IMU6_PATH = "Meas/IMU6/";
    private final String IMU9_PATH = "Meas/IMU9/";
    private final String IMU_INFO_PATH = "/Meas/IMU/Info";

    @BindView(R.id.imu6_radioBtn) RadioButton mImu6RadioBtn;
    @BindView(R.id.imu9_radioBtn) RadioButton mImu9RadioBtn;


    private AlertDialog alertDialog;

    private final List<String> spinnerRates = new ArrayList<>();
    private String rate;

    private String SELECTED_PATH = IMU6_PATH;

    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.switchSubscription) SwitchCompat mSwitchSubscription;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.linearacc_x_axis_textView) TextView mLinearaccXAxisTextView;
    @BindView(R.id.linearacc_y_axis_textView) TextView mLinearaccYAxisTextView;
    @BindView(R.id.linearacc_z_axis_textView) TextView mLinearaccZAxisTextView;
    @BindView(R.id.gyro_x_axis_textView) TextView mGyroXAxisTextView;
    @BindView(R.id.gyro_y_axis_textView) TextView mGyroYAxisTextView;
    @BindView(R.id.gyro_z_axis_textView) TextView mGyroZAxisTextView;
    @BindView(R.id.magn_x_axis_textView) TextView mMagnXAxisTextView;
    @BindView(R.id.magn_y_axis_textView) TextView mMagnYAxisTextView;
    @BindView(R.id.magn_z_axis_textView) TextView mMagnZAxisTextView;

    private MdsSubscription mMdsSubscription;
    private CsvLogger mCsvLogger;
    private boolean isLogSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imu);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Imu");
        }

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading_information)
                .create();

        mCsvLogger = new CsvLogger(getApplicationContext());

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());


        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

        spinner.setAdapter(spinnerAdapter);

        // Display dialog
        alertDialog.show();

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX
                        + MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + IMU_INFO_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(TAG, "onSuccess(): " + data);

                        // Hide dialog
                        alertDialog.dismiss();

                        InfoResponse infoResponse = new Gson().fromJson(data, InfoResponse.class);

                        for (Integer inforate : infoResponse.content.sampleRates) {
                            spinnerRates.add(String.valueOf(inforate));

                            // Set first rate as default
                            if (rate == null) {
                                rate = String.valueOf(inforate);
                            }
                        }

                        spinnerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "onError(): ", error);

                        // Hide dialog
                        alertDialog.dismiss();
                    }
                });

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);
    }

    long prevUpdateTimestamp = 0;
    @OnCheckedChanged(R.id.switchSubscription)
    public void onSwitchCheckedChange(final CompoundButton button, boolean checked) {
        if (checked) {

            isLogSaved = false;

            mImu6RadioBtn.setEnabled(false);
            mImu9RadioBtn.setEnabled(false);

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            // Same header for all IMU variants
            mCsvLogger.appendHeader("Timestamp,AccX,AccY,AccZ,GyroX,GyroY,GyroZ,MagnX,MagnY,MagnZ");

            // Somewhat ok guess for ms per sample (used on first update)
            final int initialMsPerSample = 1000/Integer.valueOf(rate);

            mMdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), SELECTED_PATH + rate), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(TAG, "onSuccess(): " + data);

                            ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                            long diffTs = imuModel.getBody().getTimestamp()-prevUpdateTimestamp;
                            // Use guess when starting or if timestamp loops
                            if (prevUpdateTimestamp == 0 || diffTs < 0)
                            {
                                diffTs = initialMsPerSample * imuModel.getBody().getArrayAcc().length;
                            }

                            mLinearaccXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayAcc()[0].getX()));
                            mLinearaccYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayAcc()[0].getY()));
                            mLinearaccZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayAcc()[0].getZ()));

                            mGyroXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayGyro()[0].getX()));
                            mGyroYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayGyro()[0].getY()));
                            mGyroZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayGyro()[0].getZ()));

                            if (imuModel.getBody().getArrayMagnl() != null) {
                                mMagnXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayMagnl()[0].getX()));
                                mMagnYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayMagnl()[0].getY()));
                                mMagnZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayMagnl()[0].getZ()));
                            }

                            int len = imuModel.getBody().getArrayAcc().length;
                            for (int i=0;i < len; i++)
                            {
                                // Interpolate timestamp within update
                                long timestamp = imuModel.getBody().getTimestamp() +
                                        (i * diffTs) / len;

                                if (SELECTED_PATH.equals(IMU6_PATH))
                                {
                                    // IMU6
                                    mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                            "%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                                            timestamp,
                                            imuModel.getBody().getArrayAcc()[i].getX(),
                                            imuModel.getBody().getArrayAcc()[i].getY(),
                                            imuModel.getBody().getArrayAcc()[i].getZ(),
                                            imuModel.getBody().getArrayGyro()[i].getX(),
                                            imuModel.getBody().getArrayGyro()[i].getY(),
                                            imuModel.getBody().getArrayGyro()[i].getZ()));
                                }
                                else if (SELECTED_PATH.equals(IMU9_PATH))
                                {
                                    // IMU9
                                    mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                            "%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                                            timestamp,
                                            imuModel.getBody().getArrayAcc()[i].getX(),
                                            imuModel.getBody().getArrayAcc()[i].getY(),
                                            imuModel.getBody().getArrayAcc()[i].getZ(),
                                            imuModel.getBody().getArrayGyro()[i].getX(),
                                            imuModel.getBody().getArrayGyro()[i].getY(),
                                            imuModel.getBody().getArrayGyro()[i].getZ(),
                                            imuModel.getBody().getArrayMagnl()[i].getX(),
                                            imuModel.getBody().getArrayMagnl()[i].getY(),
                                            imuModel.getBody().getArrayMagnl()[i].getZ()));
                                }
                            }
                            // Update prev timestamp
                            prevUpdateTimestamp = imuModel.getBody().getTimestamp();
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(TAG, "onError(): ", error);
                            button.setChecked(false);
                            mImu6RadioBtn.setEnabled(false);
                            mImu9RadioBtn.setEnabled(false);
                        }
                    });
        } else {
            unSubscribe();
            mImu6RadioBtn.setEnabled(true);
            mImu9RadioBtn.setEnabled(true);
        }
    }

    private void unSubscribe() {
        if (mMdsSubscription != null) {
            mMdsSubscription.unsubscribe();
            mMdsSubscription = null;
        }

        if (!isLogSaved) {
            mCsvLogger.finishSavingLogs(this, TAG);
            isLogSaved = true;
        }
    }

    @OnCheckedChanged({R.id.imu6_radioBtn, R.id.imu9_radioBtn})
    public void onImuRadioGroupChange(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.imu6_radioBtn:
                    SELECTED_PATH = IMU6_PATH;
                    break;
                case R.id.imu9_radioBtn:
                    SELECTED_PATH = IMU9_PATH;
                    break;
            }
        }

        mLinearaccXAxisTextView.setText("x:");
        mLinearaccYAxisTextView.setText("y:");
        mLinearaccZAxisTextView.setText("z:");

        mGyroXAxisTextView.setText("x:");
        mGyroYAxisTextView.setText("y:");
        mGyroZAxisTextView.setText("z:");

        mMagnXAxisTextView.setText("x:");
        mMagnYAxisTextView.setText("y:");
        mMagnZAxisTextView.setText("z:");
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rate = spinnerRates.get(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        if (!isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(ImuActivity.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.d(TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
