package com.example.locationmonitoring.util;

import android.app.Application;
import android.util.Log;

import com.example.locationmonitoring.model.myDestination;

public class MyUtil extends Application {

    private UserClient userClient;
    private UpdateLocationUtil updateLocationUtil;
    private myDestinationUtil mDestinationUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyUtil", "onCreate is called");
        // Initialize your classes here
        userClient = new UserClient();
        updateLocationUtil = new UpdateLocationUtil();
        mDestinationUtil = new myDestinationUtil();

        // Other initialization code
    }

    public myDestinationUtil getmDestinationUtil() {
        return mDestinationUtil;
    }

    public UserClient getUserClient() {
        return userClient;
    }

    public UpdateLocationUtil getUpdateLocationUtil() {
        return updateLocationUtil;
    }
}
