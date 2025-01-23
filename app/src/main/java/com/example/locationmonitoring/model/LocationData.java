package com.example.locationmonitoring.model;

public class LocationData {
    private double latitude;
    private double longitude;

    // Default constructor required for calls to DataSnapshot.getValue(LocationData.class)
    public LocationData() {
    }

    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
