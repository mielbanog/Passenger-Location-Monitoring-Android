package com.example.locationmonitoring.model;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.locationmonitoring.ui.LocationMonitoring;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

//---------------NOT USED----------------------\\\
public class DBLocationUpdateManager {
    private static final String TAG = "DBLocationUpdate";
    private static final long UPDATE_INTERVAL = 5000; // 5 seconds
    private Handler handler = new Handler();
    private DatabaseReference userLocationReference;
    private Marker marker;
    private GoogleMap googleMap;
    LatLng initialLatLng;

    public DBLocationUpdateManager(DatabaseReference userLocationReference, GoogleMap googleMap) {
        this.userLocationReference = userLocationReference;
        this.googleMap = googleMap;
    }

    public void startLocationUpdates() {
        //call updates using ValueEventlistener
        //userLocationReference.addValueEventListener(locationUpdateEventListener);

        //call updates using runnable
        handler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);
    }

    public void stopLocationUpdates() {
        userLocationReference.removeEventListener(locationUpdateEventListener);

       // handler.removeCallbacks(locationUpdateRunnable);
    }

    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // Retrieve location data from Firebase
            userLocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                        if (userLocation!= null){

                            updateMarker(userLocation);
                        }else {
                            Log.d(TAG,"UserLocation: No UserLocation retrieved");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error
                }
            });

            // Schedule the next update
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };
    private ValueEventListener locationUpdateEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                if (userLocation!= null){

                    updateMarker(userLocation);
                }else {
                    Log.d(TAG,"UserLocation: Unable to retrieve userlocation or NULL");
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle the error
        }
    };

    //------ NEED FIX ---------
    //---DOESNT FORWARD THE MAP TO LOCATIONMONITORING CLASS-----
    private void updateMarker(UserLocation userLocation) {
        if (marker == null) {
            if (userLocation!= null){

                Log.d(TAG,"UserLocation: Retrieved loc" + userLocation.getLocation().getLongitude());
                // Initialize marker at the initial location
                 initialLatLng = new LatLng(userLocation.getLocation().getLatitude(), userLocation.getLocation().getLongitude());
                marker = googleMap.addMarker(new MarkerOptions().position(initialLatLng).title("Passenger Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(initialLatLng));
            }else {

                Log.d(TAG,"UserLocation: Unable to retrieve userlocation or NULL");
            }
        } else {
            // Update marker position on the map
            LatLng updatedLatLng = new LatLng(userLocation.getLocation().getLatitude(), userLocation.getLocation().getLongitude());
            marker.setPosition(updatedLatLng);
        }
    }
}
