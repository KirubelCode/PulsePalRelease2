package com.movesense.showcaseapp.section_00_mainView;

import com.google.gson.annotations.SerializedName;

public class SessionDataRequest {
    @SerializedName("session_id")
    public int session_id;

    public SessionDataRequest(int session_id) {
        this.session_id = session_id;
    }
}
