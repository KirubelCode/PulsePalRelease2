package com.movesense.showcaseapp.section_02_multi_connection.connection;

import com.movesense.showcaseapp.BasePresenter;
import com.movesense.showcaseapp.BaseView;
import com.polidea.rxandroidble2.RxBleDevice;

public interface MultiConnectionContract {

    interface Presenter extends BasePresenter {
        void scanFirstDevice();
        void scanSecondDevice();
        void connect(RxBleDevice rxBleDevice);
        void disconnect(RxBleDevice rxBleDevice);

    }

    interface View extends BaseView<MultiConnectionContract.Presenter> {
        void onFirsDeviceSelectedResult(RxBleDevice rxBleDevice);
        void onSecondDeviceSelectedResult(RxBleDevice rxBleDevice);
        void displayErrorMessage(String message);
    }
}
