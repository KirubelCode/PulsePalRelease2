package com.movesense.showcaseapp.section_00_mainView;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GoalsResponse {
    public boolean success;
    public String  message;
    public List<Goal> goals;

    public static class Goal {
        public int    id;
        public String metric;
        public String operator;
        public float  target_value;

        @SerializedName("duration_sec")
        public int    duration_seconds;
    }
}

