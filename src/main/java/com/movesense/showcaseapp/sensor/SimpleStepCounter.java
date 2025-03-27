package com.movesense.showcaseapp.sensor;

public class SimpleStepCounter {
    // Increase threshold to avoid noise when stationary.
    private static final float STEP_THRESHOLD = 10.5f; // m/s² (adjust as needed)
    private static final long MIN_STEP_INTERVAL_MS =300 ; // ms debounce

    // Low-pass filter smoothing factor
    private static final float ALPHA = 0.05f;

    private int stepCount = 0;
    private long lastStepTime = 0;

    // Personalised stride length (in meters)
    private float strideLength = 0.75f;

    // Low-pass filter state
    private float previousFilteredValue = 0;
    private boolean firstSample = true;

    // Pause flag: if true, processAcceleration will ignore new data.
    private boolean paused = false;

    public void processAcceleration(float x, float y, float z) {
        if(paused) return;  // Do nothing if paused

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

        // Count a step if the filtered magnitude exceeds the threshold and debounce time has passed
        if (filtered > STEP_THRESHOLD && (currentTime - lastStepTime) > MIN_STEP_INTERVAL_MS) {
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
