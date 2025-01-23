package com.example.locationmonitoring.model;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.ui.DisplayLocation;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LocationForegroundService extends Service {
    private static final int LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds

    private final Handler handler = new Handler();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Initialize your Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                    Location location = locationResult.getLastLocation();

                    Log.d("LocationForegroundService :", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                    if (location != null) {


                        saveLocationToFirebase(location);
                    }

            }
        };
        //startLocationUpdates(this);
        handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL);

    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // Perform location-related tasks here
            // For example, save location to Firebase

            startLocationUpdates(LocationForegroundService.this);
            // Schedule the next location update
            handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create a notification channel if running on Android Oreo (API 26) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // Build and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "your_channel_id")
                .setContentTitle("Location Monitoring Service ")
                .setContentText("Location update is running in background")
                .setSmallIcon(R.drawable.notif_location_ic) // Set your notification icon
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();

        // Start the service in the foreground with the notification
        startForeground(1, notification);
        startLocationUpdates(this);

        // Acquire a wake lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "LocationForegroundService::Wakelock");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        return START_STICKY;
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
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void saveLocationToFirebase(Location location) {
        // Create a UserLocation object and save it to Firebase
        try {

            MyUtil myUtil = (MyUtil) getApplication();
            UserClient userClient = myUtil.getUserClient();

            // Assuming UserClient has a method to get the current user
            User user = userClient.getUser();
           // User user = getUserDetails();  // You need to implement a method to get user details
            LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());
            UserLocation userLocation = new UserLocation(user, locationData);

            databaseReference.setValue(userLocation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("LocationForegroundService", "Location saved to Firebase: "
                                    + location.getLatitude() + ", " + location.getLongitude());
                        }else {
                            Log.d("LocationForegroundService", "Unable to saved to Firebase: "
                                    + location.getLatitude() + ", " + location.getLongitude());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User getUserDetails() {
        // Implement logic to get user details
        // For example, you can use FirebaseAuth to get the current user details
        // FirebaseAuth.getInstance().getCurrentUser();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            // User is signed in, you can get user details
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            // Create a User object with the obtained details
            return new User(email, uid, displayName);
        } else {
            // User is not signed in, handle accordingly (e.g., redirect to login)
            return null;
        }

    }
    // Helper method to create a notification channel
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "your_channel_id",
                "Your Channel Name",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        // Release the wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            handler.removeCallbacks(locationUpdateRunnable);
            Log.d("LocationForegroundService: ","Stop location update");
        }
    }
}

