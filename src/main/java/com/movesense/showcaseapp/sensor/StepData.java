// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Simple step data object.

package com.movesense.showcaseapp.sensor;

public class StepData {
    public final int stepCount;
    public final float distance;

    public StepData(int stepCount, float distance) {
        this.stepCount = stepCount;
        this.distance = distance;
    }
}
