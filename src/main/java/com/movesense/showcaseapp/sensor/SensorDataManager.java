// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Integral part ~ used to ensure a smooth data flow between sensor connection and data subscriptions
// throughout the app and while not in app directly.

package com.movesense.showcaseapp.sensor;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.showcaseapp.model.HeartRate;
import com.movesense.showcaseapp.model.LinearAcceleration;
import java.util.Locale;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.Observable;

public class SensorDataManager {
    private static SensorDataManager instance;
    private BehaviorSubject<HeartRate> heartRateSubject = BehaviorSubject.create();
    // We only publish processed step data to the UI.
    private BehaviorSubject<StepData> stepDataSubject = BehaviorSubject.create();

    private MdsSubscription heartRateSubscription, accelerationSubscription;
    private SimpleStepCounter simpleStepCounter = new SimpleStepCounter();

    // Flag indicating if an exercise session is active.
    private boolean exerciseActive = false;

    // Singleton used to safely stay connected in the session globally(accessible)
    public static SensorDataManager getInstance(){
        if(instance == null) instance = new SensorDataManager();
        return instance;
    }

    public void startSubscriptions(Mds mds, String sensorSerial) {
        // Increase heart rate update frequency to 2Hz.
        if (heartRateSubscription == null) {
            String hrJson = String.format(Locale.US, "{\"Uri\": \"%s/Meas/Hr\", \"Frequency\":4}", sensorSerial);
            heartRateSubscription = mds.subscribe("suunto://MDS/EventListener",
                    hrJson,
                    new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            HeartRate hr = new Gson().fromJson(data, HeartRate.class);
                            heartRateSubject.onNext(hr);
                        }
                        @Override
                        public void onError(MdsException error) { }
                    });
        }
        // Lower acceleration update frequency to 2Hz.
        if (accelerationSubscription == null) {
            String accJson = String.format(Locale.US, "{\"Uri\": \"%s/Meas/Acc/13\", \"Frequency\":1}", sensorSerial);
            accelerationSubscription = mds.subscribe("suunto://MDS/EventListener",
                    accJson,
                    new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            LinearAcceleration la = new Gson().fromJson(data, LinearAcceleration.class);
                            if (la != null && la.body.array.length > 0) {
                                // Process acceleration data internally for step counting only when session is active.
                                if (exerciseActive) {
                                    LinearAcceleration.Array arr = la.body.array[0];
                                    simpleStepCounter.processAcceleration((float) arr.x, (float) arr.y, (float) arr.z);
                                    StepData stepData = new StepData(simpleStepCounter.getStepCount(), simpleStepCounter.getDistance());
                                    stepDataSubject.onNext(stepData);
                                }
                            }
                        }
                        @Override
                        public void onError(MdsException error) { }
                    });
        }
    }

    public void stopSubscriptions() {
        if (heartRateSubscription != null) {
            heartRateSubscription.unsubscribe();
            heartRateSubscription = null;
        }
        if (accelerationSubscription != null) {
            accelerationSubscription.unsubscribe();
            accelerationSubscription = null;
        }
    }

    public Observable<HeartRate> getHeartRateObservable(){
        return heartRateSubject;
    }

    public Observable<StepData> getStepDataObservable(){
        return stepDataSubject;
    }


    public int getLatestStepCount() {
        return simpleStepCounter.getStepCount();
    }

    public float getLatestDistance() {
        return simpleStepCounter.getDistance();
    }

    public void setUserStrideLength(float strideLength) {
        simpleStepCounter.setStrideLength(strideLength);
    }

    // Pause/resume step counting.
    public void pauseStepCounter() {
        simpleStepCounter.pause();
    }

    public void resumeStepCounter() {
        simpleStepCounter.resume();
    }

    // Called when starting an exercise session.
    public void startExerciseSession() {
        exerciseActive = true;
        resumeStepCounter();
        // Reset step counter baseline.
        simpleStepCounter.reset();
    }

    // Called when stopping an exercise session.
    public void stopExerciseSession() {
        exerciseActive = false;
        pauseStepCounter();
        resetStepCounter();
    }

    // Reset the step counter and publish new data.
    public void resetStepCounter() {
        simpleStepCounter.reset();
        StepData stepData = new StepData(simpleStepCounter.getStepCount(), simpleStepCounter.getDistance());
        stepDataSubject.onNext(stepData);
    }
}
