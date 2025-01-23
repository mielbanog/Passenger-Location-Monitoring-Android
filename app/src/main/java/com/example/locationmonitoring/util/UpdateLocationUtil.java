package com.example.locationmonitoring.util;

import android.app.Application;

import com.example.locationmonitoring.model.LocationData;

public class UpdateLocationUtil extends Application {
    private LocationData locationData = null;

    public LocationData getLocationData() {
        return locationData;
    }

    public void setLocationData(LocationData locationData) {
        this.locationData = locationData;
    }
}
