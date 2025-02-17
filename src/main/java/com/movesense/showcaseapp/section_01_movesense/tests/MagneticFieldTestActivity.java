package com.movesense.showcaseapp.section_01_movesense.tests;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import com.movesense.showcaseapp.model.InfoResponse;
import com.movesense.showcaseapp.model.LinearAcceleration;
import com.movesense.showcaseapp.model.MagneticField;
import com.movesense.showcaseapp.utils.FormatHelper;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;

public class MagneticFieldTestActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.switchSubscription) SwitchCompat switchSubscription;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.x_axis_textView) TextView xAxisTextView;
    @BindView(R.id.y_axis_textView) TextView yAxisTextView;
    @BindView(R.id.z_axis_textView) TextView zAxisTextView;
    @BindView(R.id.magneticField_lineChart) LineChart mChart;

    private final String MAGNETIC_FIELD_PATH = "Meas/Magn/";
    private final String MAGNETIC_FIELD_INFO_PATH = "/Meas/Magn/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";

    private final String LOG_TAG = MagneticFieldTestActivity.class.getSimpleName();
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;

    private AlertDialog alertDialog;

    private final List<String> spinnerRates = new ArrayList<>();
    private String rate;
    private MdsSubscription mdsSubscription;
    private CsvLogger mCsvLogger;
    private boolean isLogSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic_field_test);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Magnetic Field");
        }

        mCsvLogger = new CsvLogger(getApplicationContext());

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading_information)
                .create();

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        xAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        yAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        zAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Init Empty Chart
        mChart.setData(new LineData());
        mChart.getDescription().setText("Magnetic Field");
        mChart.setTouchEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.invalidate();

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

        spinner.setAdapter(spinnerAdapter);

        // Display dialog
        alertDialog.show();

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX
                        + MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + MAGNETIC_FIELD_INFO_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(LOG_TAG, "onSuccess(): " + data);

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
                        Log.e(LOG_TAG, "onError(): ", error);

                        // Hide dialog
                        alertDialog.dismiss();
                    }
                });

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            disableSpinner();

            isLogSaved = false;

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            final LineData mLineData = mChart.getData();

            ILineDataSet xSet = mLineData.getDataSetByIndex(0);
            ILineDataSet ySet = mLineData.getDataSetByIndex(1);
            ILineDataSet zSet = mLineData.getDataSetByIndex(2);

            if (xSet == null) {
                xSet = createSet("Data x", getResources().getColor(android.R.color.holo_red_dark));
                ySet = createSet("Data y", getResources().getColor(android.R.color.holo_green_dark));
                zSet = createSet("Data z", getResources().getColor(android.R.color.holo_blue_dark));
                mLineData.addDataSet(xSet);
                mLineData.addDataSet(ySet);
                mLineData.addDataSet(zSet);
            }

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), MAGNETIC_FIELD_PATH + rate), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(LOG_TAG, "onSuccess(): " + data);

                            MagneticField magneticField = new Gson().fromJson(
                                    data, MagneticField.class);

                            if (magneticField != null) {
                                final int sampleRate = Integer.parseInt(rate);
                                final float sampleInterval = 1000.0f / (float)sampleRate;

                                mCsvLogger.appendHeader("Timestamp (ms),X: (microtesla),Y: (microtesla),Z: (microtesla)");

                                MagneticField.Array arrayData = null;
                                for (int i=0; i<magneticField.body.array.length; i++) {
                                    arrayData = magneticField.body.array[i];

                                    mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                            "%d,%.6f,%.6f,%.6f, ",
                                            magneticField.body.timestamp + Math.round(sampleInterval * i),
                                            arrayData.x, arrayData.y, arrayData.z));
                                }

                                xAxisTextView.setText(String.format(Locale.getDefault(),
                                        "x: %.6f", arrayData.x));
                                yAxisTextView.setText(String.format(Locale.getDefault(),
                                        "y: %.6f", arrayData.y));
                                zAxisTextView.setText(String.format(Locale.getDefault(),
                                        "z: %.6f", arrayData.z));

                                mLineData.addEntry(new Entry(magneticField.body.timestamp / 100, (float) arrayData.x), 0);
                                mLineData.addEntry(new Entry(magneticField.body.timestamp / 100, (float) arrayData.y), 1);
                                mLineData.addEntry(new Entry(magneticField.body.timestamp / 100, (float) arrayData.z), 2);
                                mLineData.notifyDataChanged();

                                // let the chart know it's data has changed
                                mChart.notifyDataSetChanged();

                                // limit the number of visible entries
                                mChart.setVisibleXRangeMaximum(50);

                                // move to the latest entry
                                mChart.moveViewToX(magneticField.body.timestamp / 100);
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "onError(): ", error);

                            Toast.makeText(MagneticFieldTestActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            buttonView.setChecked(false);
                        }
                    });
        } else {
            enableSpinner();
            unSubscribe();
        }
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rate = spinnerRates.get(position);
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

    private void disableSpinner() {
        spinner.setEnabled(false);
    }

    private void enableSpinner() {
        spinner.setEnabled(true);
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
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(MagneticFieldTestActivity.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(LOG_TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
        mChart.getData().clearValues();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }

    private LineDataSet createSet(String name, int color) {
        LineDataSet set = new LineDataSet(null, name);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(0f);

        return set;
    }
}
