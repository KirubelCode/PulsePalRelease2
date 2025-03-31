package com.movesense.showcaseapp.sensor;

public class SimpleStepCounter {
    // Thresholds for step detection:
    private static final float RUNNING_STEP_THRESHOLD = 10.5f; // for running/jogging
    private static final float WALKING_STEP_THRESHOLD = 9.83f;  // for walking (more sensitive)

    // Debounce intervals:
    private static final long RUNNING_DEBOUNCE_MS = 300; // for running/jogging
    private static final long WALKING_DEBOUNCE_MS = 465; // for walking

    // Low-pass filter smoothing factor
    private static final float ALPHA = 0.5f;

    private int stepCount = 0;
    private long lastStepTime = 0;

    // Personalized stride length (in meters)
    private float strideLength = 0.75f;

    // Low-pass filter state
    private float previousFilteredValue = 0;
    private boolean firstSample = true;

    // Pause flag: if true, processAcceleration will ignore new data.
    private boolean paused = false;

    public void processAcceleration(float x, float y, float z) {
        if (paused) return;  // Do nothing if paused

        // Calculate raw magnitude
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        // Apply a simple low-pass filter to reduce noise
        float filtered;
        if (firstSample) {
            filtered = magnitude;
            previousFilteredValue = magnitude;
            firstSample = false;
        } else {
            filtered = ALPHA * magnitude + (1 - ALPHA) * previousFilteredValue;
            previousFilteredValue = filtered;
        }

        long currentTime = System.currentTimeMillis();

        // Check for a running/jogging step first.
        if (filtered > RUNNING_STEP_THRESHOLD && (currentTime - lastStepTime) > RUNNING_DEBOUNCE_MS) {
            stepCount++;
            lastStepTime = currentTime;
        }
        // Else, if the filtered value is above the walking threshold (but below running)
        // and enough time (decreased cadence) has passed, count as a walking step.
        else if (filtered > WALKING_STEP_THRESHOLD && filtered <= RUNNING_STEP_THRESHOLD &&
                (currentTime - lastStepTime) > WALKING_DEBOUNCE_MS) {
            stepCount++;
            lastStepTime = currentTime;
        }
    }

    public int getStepCount() {
        return stepCount;
    }

    public float getDistance() {
        return stepCount * strideLength;
    }

    public void reset() {
        stepCount = 0;
        lastStepTime = 0;
        firstSample = true;
    }

    public void setStrideLength(float strideLength) {
        this.strideLength = strideLength;
    }

    // Pause processing of new acceleration data
    public void pause() {
        paused = true;
    }

    // Resume processing of new acceleration data
    public void resume() {
        paused = false;
    }
}
