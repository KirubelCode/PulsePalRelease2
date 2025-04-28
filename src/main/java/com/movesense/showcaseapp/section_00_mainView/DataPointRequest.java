// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Layout for data point requests.

package com.movesense.showcaseapp.section_00_mainView;

public class DataPointRequest {
    public int session_id;
    public long timestamp;
    public float heart_rate;
    public int steps;
    public float distance;
    public float pace;

    public float calories;

    public DataPointRequest(int session_id, long timestamp, float heart_rate, int steps, float distance, float pace, float calories) {
        this.session_id = session_id;
        this.timestamp = timestamp;
        this.heart_rate = heart_rate;
        this.steps = steps;
        this.distance = distance;
        this.pace = pace;
        this.calories = calories;
    }
}
