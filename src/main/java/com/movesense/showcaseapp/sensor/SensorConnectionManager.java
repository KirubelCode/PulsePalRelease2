package com.movesense.showcaseapp.sensor;

import com.movesense.mds.Mds;

public class SensorConnectionManager {
    private static SensorConnectionManager instance;
    private Mds mds;
    private String sensorSerial;
    public static SensorConnectionManager getInstance(){
        if(instance==null) instance = new SensorConnectionManager();
        return instance;
    }
    public void init(Mds mds, String sensorSerial){
        this.mds = mds;
        this.sensorSerial = sensorSerial;
    }
    public Mds getMds(){ return mds; }
    public String getSensorSerial(){ return sensorSerial; }
    public void clear(){ mds = null; sensorSerial = null; instance = null; }
}
