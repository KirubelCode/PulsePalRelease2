package com.movesense.showcaseapp.bluetooth;

import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;

/**
 * Singleton wrapper for RxBleClient
 */
public enum RxBle {
    Instance;

    private RxBleClient client;

    public void initialize(Context context) {
        client = RxBleClient.create(context);
    }

    public RxBleClient getClient() {
        return client;
    }
}
