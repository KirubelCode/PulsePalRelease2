package com.movesense.showcaseapp.section_02_multi_connection.sensor_usage;


import com.movesense.showcaseapp.BasePresenter;
import com.movesense.showcaseapp.BaseView;

import io.reactivex.Observable;

public interface MultiSensorUsageContract {

    interface Presenter extends BasePresenter {
        Observable<String> subscribeLinearAcc(String uri);

    }

    interface View extends BaseView<MultiSensorUsageContract.Presenter> {

    }
}
