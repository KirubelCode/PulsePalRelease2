package com.movesense.showcaseapp.section_01_movesense;


import android.content.BroadcastReceiver;
import android.content.Intent;

import com.movesense.showcaseapp.BasePresenter;
import com.movesense.showcaseapp.BaseView;
import com.polidea.rxandroidble2.RxBleDevice;

public interface MovesenseContract {

    interface Presenter extends BasePresenter {
        void startScanning();

        void stopScanning();

        void onBluetoothResult(int requestCode, int resultCode, Intent data);
    }

    interface View extends BaseView<Presenter> {
        void displayScanResult(RxBleDevice bluetoothDevice, int rssi);

        void displayErrorMessage(String message);

        void registerReceiver(BroadcastReceiver broadcastReceiver);

        void unregisterReceiver(BroadcastReceiver broadcastReceiver);

        boolean checkLocationPermissionIsGranted();
    }
}
