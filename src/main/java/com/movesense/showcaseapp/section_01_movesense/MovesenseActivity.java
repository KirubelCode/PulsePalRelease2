package com.movesense.showcaseapp.section_01_movesense;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.ConnectingDialog;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.bluetooth.RxBle;
import com.movesense.showcaseapp.model.MdsConnectedDevice;
import com.movesense.showcaseapp.model.MdsDeviceInfoNewSw;
import com.movesense.showcaseapp.model.MdsDeviceInfoOldSw;
import com.movesense.showcaseapp.model.RxBleDeviceWrapper;
import com.movesense.showcaseapp.section_01_movesense.sensors.sensors_list.SensorListActivity;
import com.movesense.showcaseapp.utils.ThrowableToastingAction;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleScanResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MovesenseActivity extends AppCompatActivity implements MovesenseContract.View, View.OnClickListener {

    @BindView(R.id.movesense_recyclerView) RecyclerView mMovesenseRecyclerView;
    @BindView(R.id.movesense_infoTv) TextView mMovesenseInfoTv;
    @BindView(R.id.movesense_progressBar) ProgressBar mMovesenseProgressBar;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;

    private MovesenseContract.Presenter mMovesensePresenter;
    private ArrayList<RxBleDeviceWrapper> mMovesenseModels;
    private CompositeDisposable scanningSubscriptions;
    private CompositeDisposable connectedDevicesSubscriptions;

    private final String TAG = MovesenseActivity.class.getSimpleName();
    private MovesenseAdapter mMovesenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movesense);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Movesense Connection");
        }

        scanningSubscriptions = new CompositeDisposable();
        connectedDevicesSubscriptions = new CompositeDisposable();

        mMovesensePresenter = new MovesensePresenter(this,
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE));

        mMovesensePresenter.onCreate();

        mMovesenseModels = new ArrayList<>();

        mMovesenseAdapter = new MovesenseAdapter(mMovesenseModels, this);
        mMovesenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMovesenseRecyclerView.setAdapter(mMovesenseAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }

        startScanning();

    }

    @Override
    public void displayScanResult(RxBleDevice bluetoothDevice, int rssi) {
        Log.d(TAG, "displayScanResult: " + bluetoothDevice.getName());
        mMovesenseAdapter.add(new RxBleDeviceWrapper(rssi, bluetoothDevice));

        mMovesenseAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerReceiver(BroadcastReceiver broadcastReceiver) {
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public boolean checkLocationPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(MovesenseActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MovesenseActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(MovesenseActivity.this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MovesenseActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.BLUETOOTH,
                                            Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MovesenseActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setPresenter(MovesenseContract.Presenter presenter) {
        mMovesensePresenter = presenter;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    startScanning();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMovesensePresenter.onBluetoothResult(requestCode, resultCode, data);
    }

    private void startScanning() {
        // Make sure we have location permission
        if (!checkLocationPermission()) {
            return;
        }

        Log.d(TAG, "START SCANNING !!!");
        // Start scanning
        scanningSubscriptions.add(RxBle.Instance.getClient().scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RxBleScanResult>() {
                    @Override
                    public void accept(RxBleScanResult rxBleScanResult) {
                        Log.d(TAG, "call: SCANNED: " + rxBleScanResult.getBleDevice().getName() + " " + rxBleScanResult.getBleDevice().getMacAddress()
                                + " rssi: " + rxBleScanResult.getRssi());
                        RxBleDevice rxBleDevice = rxBleScanResult.getBleDevice();

                        if (rxBleDevice.getName() != null && rxBleDevice.getName().contains("Movesense")
                                && !mMovesenseModels.contains(rxBleDevice)) {

                            Log.d(TAG, "call: Add to list " + rxBleScanResult.getBleDevice().getName());
                            mMovesenseAdapter.add(new RxBleDeviceWrapper(rxBleScanResult.getRssi(), rxBleDevice));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(TAG, "scanBleDevices(): ", throwable);
                    }
                }));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MovesenseActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        final RxBleDevice rxBleDevice = (RxBleDevice) v.getTag();
        Log.d(TAG, "Connecting to : " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());

        mMovesenseProgressBar.setVisibility(View.GONE);

        Mds.builder().build(this).connect(rxBleDevice.getMacAddress(), null);

        // We are in connecting progress we don't need to scan anymore
        scanningSubscriptions.dispose();
        mMovesensePresenter.stopScanning();

        ConnectingDialog.INSTANCE.showDialog(this, rxBleDevice.getMacAddress());

        // Monitor for connected devices
        connectedDevicesSubscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Consumer<MdsConnectedDevice>() {
                    @Override
                    public void accept(MdsConnectedDevice mdsConnectedDevice) {
                        // Stop refreshing
                        if (mdsConnectedDevice.getConnection() != null) {
                            ConnectingDialog.INSTANCE.dismissDialog();
                            Log.e(TAG, "Connected " + mdsConnectedDevice.toString());

                            // Add connected device
                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoNewSw: " + mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress()
                                        + " : " + mdsDeviceInfoNewSw.getDescription() + " : " + mdsDeviceInfoNewSw.getSerial()
                                        + " : " + mdsDeviceInfoNewSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));
                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoOldSw: " + mdsDeviceInfoOldSw.getAddressInfoOld()
                                        + " : " + mdsDeviceInfoOldSw.getDescription() + " : " + mdsDeviceInfoOldSw.getSerial()
                                        + " : " + mdsDeviceInfoOldSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoOldSw.getAddressInfoOld(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));
                            }

                            Log.e(TAG, "List size(): " + MovesenseConnectedDevices.getConnectedDevices().size());
                            connectedDevicesSubscriptions.dispose();

                            // We have a new SdsDevice
                            startActivity(new Intent(MovesenseActivity.this, SensorListActivity.class));
                        } else {
                            Log.e(TAG, "DISCONNECT");
                        }
                    }
                }, new ThrowableToastingAction(this)));
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMovesensePresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        mMovesensePresenter.onDestroy();
        connectedDevicesSubscriptions.dispose();
        scanningSubscriptions.dispose();
    }
}
