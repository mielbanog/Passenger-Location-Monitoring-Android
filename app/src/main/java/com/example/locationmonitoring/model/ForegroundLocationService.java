package com.example.locationmonitoring.model;

import static android.app.Service.START_STICKY;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.locationmonitoring.ui.DisplayLocation;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UpdateLocationUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ForegroundLocationService extends Service {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private static final long FASTEST_UPDATE_INTERVAL = 1000; // 1 second

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;

    //----------------------------------NOT USED---------------------------------------------------\\
    //LocationForegroundService is already USED

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Your existing location handling logic
                    handleLocationUpdate(location);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(10000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void handleLocationUpdate(Location location) {
        MyUtil myUtil = (MyUtil) getApplicationContext();
        User user = myUtil.getUserClient().getUser();
        LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());

        UpdateLocationUtil updateLocationUtil = myUtil.getUpdateLocationUtil();
        updateLocationUtil.setLocationData(locationData);

        UserLocation userLocation = new UserLocation(user, locationData);

        saveLocationToFirebase(userLocation);
    }

    private void saveLocationToFirebase(final UserLocation userLocation) {
        try {
            databaseReference.setValue(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("LocationForegroundService", "Location saved to Firebase: " +
                                userLocation.getLocation().getLatitude() + ", " +
                                userLocation.getLocation().getLongitude());
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e("LocationForegroundService", "NullPointerException: " + e.getMessage());
        }
    }
}
