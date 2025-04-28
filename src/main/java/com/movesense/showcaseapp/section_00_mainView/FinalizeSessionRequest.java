// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Layout for final session data.
package com.movesense.showcaseapp.section_00_mainView;

public class FinalizeSessionRequest {
    public int session_id;
    public float duration_minutes;
    public int total_steps;
    public float total_distance;
    public float avg_pace;
    public float avg_heart_rate;
    public float total_calories;

    public FinalizeSessionRequest(int session_id, float duration_minutes, int total_steps, float total_distance, float avg_pace, float avg_heart_rate, float total_calories) {
        this.session_id = session_id;
        this.duration_minutes = duration_minutes;
        this.total_steps = total_steps;
        this.total_distance = total_distance;
        this.avg_pace = avg_pace;
        this.avg_heart_rate = avg_heart_rate;
        this.total_calories = total_calories;
    }
}
