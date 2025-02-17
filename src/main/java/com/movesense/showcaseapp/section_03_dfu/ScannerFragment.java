package com.movesense.showcaseapp.section_03_dfu;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.RxBle;
import com.movesense.showcaseapp.utils.ThrowableToastingAction;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleScanResult;

import io.reactivex.disposables.Disposable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for scanning a ble device
 */
public class ScannerFragment extends DialogFragment {

    private final String TAG = ScannerFragment.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;

    public interface DeviceSelectionListener {
        void onDeviceSelected(RxBleDevice device);
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private DeviceSelectionListener deviceSelectionListener;
    private RxBleClient rxBleClient;
    private ScannedDevicesAdapter scannedDevicesAdapter;
    private CompositeDisposable subscriptions;

    private final String LOG_TAG = ScannerFragment.class.getSimpleName();

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);

        Activity activity = getActivity();
        if (activity instanceof DeviceSelectionListener) {
            deviceSelectionListener = (DeviceSelectionListener) activity;
        } else {
            throw new IllegalArgumentException("Containing Activity "+ activity +" does not implement DeviceSelectionListener");
        }

        getActivity().registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Ask For Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }

        // Capture instance of RxBleClient to make code look cleaner
        rxBleClient = RxBle.Instance.getClient();

        // Create one composite subscription to hold everything
        subscriptions = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Set up list and adapter for scanned devices
        scannedDevicesAdapter = new ScannedDevicesAdapter(getContext(), false);
        RecyclerView deviceList = view.findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceList.setAdapter(scannedDevicesAdapter);
        deviceList.setItemAnimator(null);

        // Listen for device selection
        Disposable selectionSubscription = scannedDevicesAdapter.deviceSelectionObservable()
                .subscribe(new Consumer<RxBleDevice>() {
                    @Override
                    public void accept(RxBleDevice rxBleDevice) {
                        deviceSelectionListener.onDeviceSelected(rxBleDevice);
                    }
                }, new ThrowableToastingAction(getContext()));
        subscriptions.add(selectionSubscription);

        // Start scanning immediately
        startScanning();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear all subscriptions
        subscriptions.dispose();

        // Unregister BtReceiver
        getActivity().unregisterReceiver(btReceiver);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // Try starting scan again
                    startScanning();
                }
            }
        }
    }

    private void startScanning() {
        // Make sure we have location permission
        if (!checkLocationPermission()) {
            return;
        }

        Log.d(LOG_TAG, "START SCANNING !!!");
        // Start scanning
        subscriptions.add(rxBleClient.scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RxBleScanResult>() {
                    @Override
                    public void accept(RxBleScanResult rxBleScanResult) {
                        Log.d(TAG, "call: ");
                        scannedDevicesAdapter.handleScanResult(rxBleScanResult);
                    }
                }, new ThrowableToastingAction(getContext())));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
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
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
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

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is ready to use.

                    // start scanning again in case of ready Bluetooth
                    startScanning();
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    return;
                }

            }
        }
    };
}
