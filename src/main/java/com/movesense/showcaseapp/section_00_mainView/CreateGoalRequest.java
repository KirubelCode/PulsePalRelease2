package com.movesense.showcaseapp.section_00_mainView;

// Goal Retrieval class
public class CreateGoalRequest {
    public int    session_id;
    public String metric;
    public String operator;
    public float  target_value;
    public int    duration_sec;      // <-- renamed

    public CreateGoalRequest(int session_id,
                             String metric,
                             String operator,
                             float target_value,
                             int duration_sec) {
        this.session_id   = session_id;
        this.metric       = metric;
        this.operator     = operator;
        this.target_value = target_value;
        this.duration_sec = duration_sec;
    }
}
