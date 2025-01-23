package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.DBLocationUpdateManager;
import com.example.locationmonitoring.model.LocationUpdateManager;
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.model.UserLocation;
import com.example.locationmonitoring.model.addDriverHelper;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UserClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationMonitoring extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationMonitoring";

    private TextView driverTxtvw,plateTxtvw,bodyTxtvw,passngerName,contactTxt,addressTxt,operatorTxt,passengerName;
    private String uid,name,email;
    private static String driverUID;
    private GoogleMap mMap;
    private Marker marker;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userLocationReference;
    DBLocationUpdateManager dbLocationUpdateManager;

    private static final long UPDATE_INTERVAL = 5000; // 5 seconds
    private Handler handler = new Handler();
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public  void onStart(){
        super.onStart();

        firebaseAuth  = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_monitoring);
        initWidg();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  // Use 'this' as OnMapReadyCallback



        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            name = String.valueOf(bundle.getString("name"));
            email = String.valueOf(bundle.getString("email"));
            uid= String.valueOf(bundle.getString("uid"));

            //TODO
            //userLocationReference = FirebaseDatabase.getInstance().getReference("UserLocation").child(uid);
            //userLocationReference.addListenerForSingleValueEvent();
        }

        // Initialize userLocationReference
        userLocationReference = (uid != null) ? FirebaseDatabase.getInstance().getReference("UserLocation").child(uid) : null;


        /*// Create LocationUpdateManager instance
        dbLocationUpdateManager = (userLocationReference != null) ? new DBLocationUpdateManager(userLocationReference, mMap) : null;

        Toast.makeText(this,"LocationMonitoring:"+uid, Toast.LENGTH_SHORT).show();
        // Check if dbLocationUpdateManager is not null before calling startLocationUpdates
        if (dbLocationUpdateManager != null) {

            dbLocationUpdateManager.startLocationUpdates();
        } else {
            Toast.makeText(this, "Unable to load passenger data", Toast.LENGTH_SHORT).show();
        }*/



        /*//TODO passenger uid
        if (uid!=null){

            userLocationReference = FirebaseDatabase.getInstance().getReference("UserLocation").child(uid);
            // Create LocationUpdateManager instance
            dbLocationUpdateManager = new DBLocationUpdateManager(userLocationReference, mMap);

        }
        else{ Toast.makeText(this,"Unable to load passenger data", Toast.LENGTH_SHORT).show();}*/



    }

    private void initWidg(){
        driverTxtvw = findViewById(R.id.driverDispnameMonitor);
        plateTxtvw = findViewById(R.id.plateDispMonitor);
        bodyTxtvw = findViewById(R.id.bodyDispMonitor);
        addressTxt = findViewById(R.id.addressDispMonitor);
        contactTxt = findViewById(R.id.contactDispMonitor);
        operatorTxt = findViewById(R.id.operatorDispMonitor);
        passengerName = findViewById(R.id.nametxtvwMonitor);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates();
        /*userLocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
        });*/




       /* // Start location updates
        if (uid != null) {
            userLocationReference = FirebaseDatabase.getInstance().getReference("UserLocation").child(uid);
            dbLocationUpdateManager = new DBLocationUpdateManager(userLocationReference, mMap);
            dbLocationUpdateManager.startLocationUpdates();
        } else {
            Toast.makeText(this, "Unable to load passenger data", Toast.LENGTH_SHORT).show();
        }*/
    }

   /* private void loadDriverdetails(){

        addDriverHelper driverobjModel = new addDriverHelper();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference driverRef = firebaseDatabase.getReference("Drivers")
                .child(driverUID);
        driverRef.
        //display to textview
        passengerName.setText(name);
        driverTxtvw.setText(name);
        bodyTxtvw.setText(body);
        plateTxtvw.setText(plate);
        addressTxt.setText(address);
        contactTxt.setText(contact);
        operatorTxt.setText(operator);

    }*/

    // Update the marker position based on the user's location
    public void updateMarker(UserLocation userLocation) {
        if (marker == null) {
            if (userLocation != null) {
                Log.d(TAG, "UserLocation: Retrieved loc" + userLocation.getLocation().getLongitude());

                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.custommarker2);
                Bitmap iconimg = Bitmap.createScaledBitmap(image, 130, 130, false);
                //retrieve location and update marker to the map
                LatLng initialLatLng = new LatLng(userLocation.getLocation().getLatitude(), userLocation.getLocation().getLongitude());
                marker = mMap.addMarker(new MarkerOptions().position(initialLatLng).title("Passenger Location").icon(BitmapDescriptorFactory.fromBitmap(iconimg)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng,14));
            } else {
                Log.d(TAG, "UserLocation: Unable to retrieve userlocation or NULL");
            }
        } else {
            // Update marker position on the map
            LatLng updatedLatLng = new LatLng(userLocation.getLocation().getLatitude(), userLocation.getLocation().getLongitude());
            marker.setPosition(updatedLatLng);
        }
    }
    //run location update using Runnable
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


    //run location update using valueEventLister
    private ValueEventListener locationUpdateEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                if (userLocation!= null){
                    Log.d(TAG, "Monitoring: \n Updated Location." +
                            "\n latitude: " + userLocation.getLocation().getLatitude() +
                            "\n longitude: " + userLocation.getLocation().getLongitude());

                    updateMarker(userLocation);
                    driverUID = userLocation.getUser().getCurrentDriver_Id();
                    retrieveDriverData(driverUID);
                    Log.d(TAG,"UserLocation: Driver UID: "+ driverUID);
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
    private void retrieveDriverData(String driverId) {

        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("Drivers").child(driverId);

        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve data from the snapshot and map it to the addDriverHelper model
                    addDriverHelper driverHelper = snapshot.getValue(addDriverHelper.class);

                    if (driverHelper != null) {
                        // Now you can use the retrieved data as needed
                        // For example, update your UI with the retrieved data
                        updateUI(driverHelper);
                    } else {
                        // Handle the case where the driver data is not properly mapped
                        showError("Error mapping driver data");
                    }
                } else {
                    // Handle the case where the driver data does not exist
                    showError("Driver data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
                showError("Error retrieving driver data");
            }
        });
    }

    // Example method to update UI with retrieved data
    private void updateUI(addDriverHelper driverHelper) {
        // Update your UI components with the retrieved data
        String driverName = driverHelper.getName();
        String body = driverHelper.getBody();
        String plate = driverHelper.getPlate();
        String address = driverHelper.getAddress();
        String contact = driverHelper.getContact();
        String operator = driverHelper.getOperator();
        String qrCode = driverHelper.getQrCode();

        // Example: Update TextViews
        passengerName.setText(name);
        driverTxtvw.setText(driverName);
        bodyTxtvw.setText(body);
        plateTxtvw.setText(plate);
        addressTxt.setText(address);
        contactTxt.setText(contact);
        operatorTxt.setText(operator);
    }

    // Example method to handle errors
    private void showError(String message) {
        // Show an error message to the user or log the error
    }

    public void startLocationUpdates() {
        //call updates using ValueEventlistener
        userLocationReference.addValueEventListener(locationUpdateEventListener);

        //call updates using runnable
        //handler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);
    }

}