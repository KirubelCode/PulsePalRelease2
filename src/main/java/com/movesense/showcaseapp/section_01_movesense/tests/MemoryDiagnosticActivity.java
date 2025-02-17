package com.movesense.showcaseapp.section_01_movesense.tests;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.bluetooth.MdsRx;
import com.movesense.showcaseapp.model.ThreadInfoList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemoryDiagnosticActivity extends AppCompatActivity {

    @BindView(R.id.response_textView) TextView responseTextView;

    private final String LOG_TAG = MemoryDiagnosticActivity.class.getSimpleName();
    private final String DIAG_PATH = "/Whiteboard/Metrics/Threads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_diagnostic);
        ButterKnife.bind(this);
        getDiagnostics();
    }

    private void getDiagnostics() {

        // Set waiting status
        responseTextView.setText(R.string.waiting_for_response);
        responseTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX
                        + MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + DIAG_PATH
                , null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(LOG_TAG, "onSuccess: " + data);

                        // Set success result
                        ThreadInfoList threadInfoList = new Gson().fromJson(
                                data, ThreadInfoList.class);

                        responseTextView.setText(threadInfoList.toString());
                        responseTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(LOG_TAG, "onError()", error);

                        // Set error result
                        responseTextView.setText(R.string.error + ": " + error);
                        responseTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                });
    }
}
