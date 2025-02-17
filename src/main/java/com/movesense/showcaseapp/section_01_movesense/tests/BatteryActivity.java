package com.movesense.showcaseapp.section_01_movesense.tests;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.BaseActivity;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.ConnectionLostDialog;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.model.EnergyGetModel;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BatteryActivity extends BaseActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.temperature_get_button) Button mTemperatureGetButton;
    @BindView(R.id.value_textView) TextView mValueTextView;

    private static final String TAG = BatteryActivity.class.getSimpleName();

    private final String BATTERY_PATH_GET = "/System/Energy/Level";
    private final String BATTERY_PATH_SUBSCRIBE = "System/Energy/Level";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    public static final String URI_SCHEME_PREFIX = "suunto://";

    private MdsSubscription mdsSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        ButterKnife.bind(this);

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);
    }

    @OnClick(R.id.temperature_get_button)
    public void onViewClicked() {

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + BATTERY_PATH_GET,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {

                        EnergyGetModel energyGetModel = new Gson().fromJson(s, EnergyGetModel.class);

                        mValueTextView.setText(String.format(Locale.getDefault(), getString(R.string.battery_value) + " %d ", energyGetModel.content));
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        if (!isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(BatteryActivity.this));
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
