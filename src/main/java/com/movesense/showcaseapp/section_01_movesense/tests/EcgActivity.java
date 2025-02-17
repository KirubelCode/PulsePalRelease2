package com.movesense.showcaseapp.section_01_movesense.tests;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
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
import com.movesense.showcaseapp.model.EcgInfoResponse;
import com.movesense.showcaseapp.model.EcgModel;
import com.movesense.showcaseapp.model.HeartRate;
import com.movesense.showcaseapp.utils.FormatHelper;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class EcgActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.switchSubscription) SwitchCompat mSwitchSubscription;
    @BindView(R.id.ecg_lineChart) GraphView mGraphView;
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.spinner) Spinner mSpinner;
    @BindView(R.id.ecg_note) TextView mEcgNote;
    @BindView(R.id.ecg_spinnerText) TextView mEcgSpinnerText;
    @BindView(R.id.ecg_switchContainer) LinearLayout mEcgSwitchContainer;

    private static final String TAG = EcgActivity.class.getSimpleName();

    private final int MS_IN_SECOND = 1000;
    private final String ECG_VELOCITY_PATH = "Meas/ECG/";
    private final String ECG_VELOCITY_INFO_PATH = "/Meas/ECG/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final String HEART_RATE_PATH = "Meas/Hr";

    @BindView(R.id.heart_rate_textView) TextView mHeartRateTextView;
    @BindView(R.id.rr_textView) TextView mRrTextView;

    private MdsSubscription mdsSubscriptionHr;
    private MdsSubscription mdsSubscriptionEcg;

    private CsvLogger mCsvLogger;
    private final List<Integer> spinnerRates = new ArrayList<>();
    private AlertDialog alertDialog;
    private LineGraphSeries<DataPoint> mSeriesECG;
    private int mDataPointsAppended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        ButterKnife.bind(this);

        mCsvLogger = new CsvLogger(getApplicationContext());

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        // Init Empty Chart
        mSeriesECG = new LineGraphSeries<DataPoint>();
        mGraphView.addSeries(mSeriesECG);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(500);

        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(-15000);
        mGraphView.getViewport().setMaxY(15000);

        mGraphView.getViewport().setScrollable(false);
        mGraphView.getViewport().setScrollableY(false);

        mGraphView.setTitleColor(Color.WHITE);
        //mGraphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        mGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setHighlightZeroLines(false);
        mSeriesECG.setColor(Color.GREEN);

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

        final ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

        mSpinner.setAdapter(spinnerAdapter);

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading_information)
                .create();

        // Display dialog
        alertDialog.show();

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX
                        + MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + ECG_VELOCITY_INFO_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(TAG, "onSuccess(): " + data);

                        // Hide dialog
                        alertDialog.dismiss();

                        EcgInfoResponse infoResponse = new Gson().fromJson(data, EcgInfoResponse.class);

                        if (infoResponse != null) {

                            for (Integer inforate : infoResponse.mContent.ranges) {
                                  spinnerRates.add(inforate);
                            }


                            spinnerAdapter.notifyDataSetChanged();

                            mSpinner.setSelection(spinnerAdapter.getPosition(infoResponse.mContent.currentSampleRate));

                            if (mSpinner.getChildCount() > 0) {
                                ((TextView) mSpinner.getChildAt(0)).setTextColor(Color.WHITE);
                            }
                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "onError(): ", error);

                        // Hide dialog
                        alertDialog.dismiss();
                    }
                });
    }

    @OnClick(R.id.ecg_changeScreenOrientation)
    public void onScreenOrientationChangeClick() {

        int orientation = getResources().getConfiguration().orientation;

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                mEcgNote.setVisibility(View.GONE);
                mConnectedDeviceNameTextView.setVisibility(View.GONE);
                mConnectedDeviceSwVersionTextView.setVisibility(View.GONE);
                mEcgSpinnerText.setVisibility(View.GONE);
                mSpinner.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mEcgSwitchContainer.getLayoutParams();
                p.setMargins(0, 0, 0, 0);
                mEcgSwitchContainer.requestLayout();
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                mEcgNote.setVisibility(View.VISIBLE);
                mConnectedDeviceNameTextView.setVisibility(View.VISIBLE);
                mConnectedDeviceSwVersionTextView.setVisibility(View.VISIBLE);
                mEcgSpinnerText.setVisibility(View.VISIBLE);
                mSpinner.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams p1 = (ViewGroup.MarginLayoutParams) mEcgSwitchContainer.getLayoutParams();
                p1.setMargins(10, 20, 0, 0); // restore margins from default xml values
                mEcgSwitchContainer.requestLayout();
                break;
        }
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            disableSpinner();
            mDataPointsAppended = 0;
            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);
            int width = 128 * 3;
            mGraphView.getViewport().setMaxX(width);

            mSeriesECG.resetData(new DataPoint[0]);

            mdsSubscriptionHr = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0).getSerial(),
                            HEART_RATE_PATH)
                    , new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.e(TAG, "Heart rate onNotification() : " + data);
                            HeartRate heartRate = new Gson().fromJson(data, HeartRate.class);

                            if (heartRate != null) {

                                mHeartRateTextView.setText(String.format(Locale.getDefault(),
                                        "Heart rate: %.0f [bpm]", (60.0 / heartRate.body.rrData[0]) * 1000));

                                mRrTextView.setText(String.format(Locale.getDefault(),
                                        "Beat interval: %d [ms]", heartRate.body.rrData[0]));
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(TAG, "Heart rate error", error);
                        }
                    });


            String subscribedSampleRate = String.valueOf(spinnerRates.get(mSpinner.getSelectedItemPosition()));
            mdsSubscriptionEcg = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), ECG_VELOCITY_PATH + subscribedSampleRate), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(TAG, "onSuccess(): " + data);

                            final EcgModel ecgModel = new Gson().fromJson(
                                    data, EcgModel.class);

                            final int[] ecgSamples = ecgModel.getBody().getData();
                            final int sampleCount = ecgSamples.length;
                            final int ecgSampleRate = spinnerRates.get(mSpinner.getSelectedItemPosition());
                            final float sampleInterval = (float) MS_IN_SECOND / ecgSampleRate;

                            if (ecgModel.getBody() != null) {

                                for (int i = 0; i < sampleCount; i++) {
                                    try {
                                        mCsvLogger.appendHeader("Timestamp,Count");

                                        if (ecgModel.mBody.timestamp != null) {
                                            mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                                    "%d,%s", ecgModel.mBody.timestamp + Math.round(sampleInterval * i),
                                                    String.valueOf(ecgSamples[i])));
                                        } else {
                                            mCsvLogger.appendLine("," + String.valueOf(ecgSamples[i]));
                                        }

                                        mSeriesECG.appendData(
                                                new DataPoint(mDataPointsAppended, ecgSamples[i]), false,
                                                width);
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "GraphView error ", e);
                                    }
                                    mDataPointsAppended++;

                                    if (mDataPointsAppended == 400) {
                                        mDataPointsAppended = 0;
                                        mSeriesECG.resetData(new DataPoint[0]);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(TAG, "onError(): ", error);

                            Toast.makeText(EcgActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            buttonView.setChecked(false);
                        }
                    });
        } else {
            unSubscribe();
            enableSpinner();
        }
    }

    private void disableSpinner() {
        mSpinner.setEnabled(false);
    }

    private void enableSpinner() {
        mSpinner.setEnabled(true);
    }

    private void unSubscribe() {
        if (mdsSubscriptionHr != null) {
            mdsSubscriptionHr.unsubscribe();
            mdsSubscriptionHr = null;
        }

        if (mdsSubscriptionEcg != null) {
            mdsSubscriptionEcg.unsubscribe();
            mdsSubscriptionEcg = null;
        }

        mCsvLogger.finishSavingLogs(this, TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
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
        Log.d(TAG, "onDisconnect: " + s);
        if (!isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(EcgActivity.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
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
