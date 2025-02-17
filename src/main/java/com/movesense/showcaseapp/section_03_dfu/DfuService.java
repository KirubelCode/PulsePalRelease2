package com.movesense.showcaseapp.section_03_dfu;

import android.app.Activity;
import android.util.Log;

import no.nordicsemi.android.dfu.BuildConfig;
import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * DfuService used with Nordic Library for flashing dfu.zip files
 */

public class DfuService extends DfuBaseService {

    private final String LOG_TAG = DfuService.class.getSimpleName();

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        Log.d(LOG_TAG, "getNotificationTarget() ");
        return NotificationActivity.class;
    }

    @Override
    protected boolean isDebug() {
        Log.d(LOG_TAG, "isDebug() ");
        return BuildConfig.DEBUG;
    }
}
