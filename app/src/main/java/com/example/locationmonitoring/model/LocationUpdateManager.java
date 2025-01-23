package com.example.locationmonitoring.model;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UpdateLocationUtil;
import com.example.locationmonitoring.util.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LocationUpdateManager {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    private static final String TAG = "LocationUpdateManager";
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private static final long FASTEST_UPDATE_INTERVAL = 1000; // 1 second

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentuser;

    public LocationUpdateManager(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
                .child(FirebaseAuth.getInstance().getUid());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                //LocationData locationData = new LocationData(location.getLatitude(),location.getLongitude());
                Log.d(TAG, "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                //handle location updates
                if (location != null) {
                    MyUtil myUtil = (MyUtil) context.getApplicationContext();
                    User user = myUtil.getUserClient().getUser();
                    //set the locationdata with the new location
                    LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());

                    UpdateLocationUtil updateLocationUtil = myUtil.getUpdateLocationUtil();
                    updateLocationUtil.setLocationData(locationData);

                    //pass the user,locationdata to Userlocation object
                    UserLocation userLocation = new UserLocation(user, locationData);

                    //Log.d(TAG, "USER: " + user );
                    //save to UserLocation node in Firebase
                    saveLocationToFirebase(userLocation);



                }
            }
        };
    }

    public void startLocationUpdates(Context context) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(10000)
                .build();
        // Check if the app has permission to access the device's location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void saveLocationToFirebase(final UserLocation userLocation) {
        try {

            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("UserLocation")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            databaseReference.setValue(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: \ninserted user location into database." +
                                "\n latitude: " + userLocation.getLocation().getLatitude() +
                                "\n longitude: " + userLocation.getLocation().getLongitude());
                    }
                }
            });
        }catch (NullPointerException e){
            //handle nullpointer error
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );

        }
    }

    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
