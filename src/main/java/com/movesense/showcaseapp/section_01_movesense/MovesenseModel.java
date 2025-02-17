package com.movesense.showcaseapp.section_01_movesense;


public class MovesenseModel {

    private String serial;
    private String address;
    private String rssi;

    public MovesenseModel(String serial, String address, String rssi) {
        this.serial = serial;
        this.address = address;
        this.rssi = rssi;
    }

    public String getSerial() {
        return serial;
    }

    public String getAddress() {
        return address;
    }

    public String getRssi() {
        return rssi;
    }
}
