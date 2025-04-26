package com.movesense.showcaseapp.section_00_mainView;

import com.google.gson.annotations.SerializedName;

public class SessionSummary {
    @SerializedName("id")
    public int session_id;

    public String date;
    public float duration_minutes;
}
