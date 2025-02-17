package com.movesense.showcaseapp.bluetooth;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.movesense.mds.Logger;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.showcaseapp.model.MdsConnectedDevice;
import com.movesense.showcaseapp.model.MdsDeviceInfoNewSw;
import com.movesense.showcaseapp.model.MdsDeviceInfoOldSw;
import com.movesense.showcaseapp.model.MdsResponse;
import com.movesense.showcaseapp.model.MdsUri;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Singleton to hold MDS
 */
public enum MdsRx {
    Instance;

    // Set of MDS constants
    public static final String EMPTY_CONTRACT = "";
    public static final String SCHEME_PREFIX = "suunto://";
    public static final String URI_WHITEBOARD_INFO = "suunto://MDS/whiteboard/info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    public static final String URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices";

    private static final String TAG = "MDS";

    private final Charset utf8Charset;
    private final Gson gson;


    private BleManager bleManager;
    private Mds mMds;

    MdsRx() {
        utf8Charset = Charset.forName("UTF-8"); // NON-NLS
        gson = new GsonBuilder().create();
    }

    public void initialize(Context context) {
        mMds = Mds.builder().build(context);

        // Create BleManager
        bleManager = BleManager.INSTANCE;

        // Allow logging
        Logger.setPipeToOSLoggingEnabled(true);
    }

//    public void connect(RxBleDevice bleDevice) {
//        this.rxBleDevice = bleDevice;
//        bleManager.connect(bleDevice);
//    }

//    public void reconnect() {
//        Log.e(TAG, "reconnect() : " + rxBleDevice.getMacAddress());
//        bleManager.reconnect();
//    }

    public Single<String> get(String uri) {
        return get(uri, EMPTY_CONTRACT);
    }

    public Single<String> delete(String uri) {
        return delete(uri, EMPTY_CONTRACT);
    }

    public Single<String> get(final String uri, final String contract) {
        return Single.create((SingleEmitter<String> emitter) -> {
            mMds.get(uri, contract, new MdsResponseListener() {
                @Override
                public void onSuccess(String s) {
                    emitter.onSuccess(s);
                }

                @Override
                public void onError(MdsException e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public Single<String> post(final String uri, final String contract) {
        return Single.create((SingleEmitter<String> emitter) -> {
            mMds.post(uri, contract, new MdsResponseListener() {
                @Override
                public void onSuccess(String s) {
                    emitter.onSuccess(s);
                }

                @Override
                public void onError(MdsException e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public Single<String> delete(final String uri, final String contract) {
        return Single.create((SingleEmitter<String> emitter) -> {
            mMds.delete(uri, contract, new MdsResponseListener() {
                @Override
                public void onSuccess(String s) {
                    emitter.onSuccess(s);
                }

                @Override
                public void onError(MdsException e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public Observable<MdsConnectedDevice> connectedDeviceObservable() {
        return subscribe(URI_CONNECTEDDEVICES)
                .map(new Function<String, MdsConnectedDevice>() {
                    @Override
                    public MdsConnectedDevice apply(String s) {
                        Log.e(TAG, "connectedDeviceObservable(): " + s );
                        try {
                            Type type = new TypeToken<MdsResponse<MdsConnectedDevice<MdsDeviceInfoNewSw>>>() {
                            }.getType();
                            MdsResponse<MdsConnectedDevice<MdsDeviceInfoNewSw>> response = gson.fromJson(s, type);
                            Log.e(TAG, "=== RETURN: NEW");

                            if (response.getBody().getDeviceInfo().getAddressInfoNew() == null) {
                                throw new Exception("Null address info.");
                            }
                            return response.getBody();
                        } catch (Exception e) {
                            Type type = new TypeToken<MdsResponse<MdsConnectedDevice<MdsDeviceInfoOldSw>>>() {
                            }.getType();
                            MdsResponse<MdsConnectedDevice<MdsDeviceInfoOldSw>> response = gson.fromJson(s, type);
                            Log.e(TAG, "=== RETURN: OLD");
                            return response.getBody();
                        }
                    }
                })
                .filter((MdsConnectedDevice mdsConnectedDevice) -> {
                    return mdsConnectedDevice != null;
                });
    }

    public Observable<String> subscribe(final String uri) {
        Log.e(TAG, "subscribe: " + uri);
        return Observable.create((ObservableEmitter<String> emitter) -> {
            final MdsSubscription subscription = mMds.subscribe(URI_EVENTLISTENER, gson.toJson(new MdsUri(uri)),
                    new MdsNotificationListener() {
                        @Override
                        public void onNotification(String s) {
                            emitter.onNext(s);
                        }

                        @Override
                        public void onError(MdsException e) {
                            emitter.onError(e);
                        }
                    });

            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    subscription.unsubscribe();
                }
            });
        });
    }
}
