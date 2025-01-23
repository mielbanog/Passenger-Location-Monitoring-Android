package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.LocationData;
import com.example.locationmonitoring.model.LocationForegroundService;
import com.example.locationmonitoring.model.LocationUpdateManager;
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.model.UserLocation;
import com.example.locationmonitoring.model.addDriverHelper;
import com.example.locationmonitoring.model.myDestination;
import com.example.locationmonitoring.util.GPSUtil;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.SmsUtils;
import com.example.locationmonitoring.util.UpdateLocationUtil;
import com.example.locationmonitoring.util.UserClient;
import com.example.locationmonitoring.util.myDestinationUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DisplayLocation extends AppCompatActivity implements OnMapReadyCallback {

    AppCompatButton cancelBtn;
    Button startBtn,stopBtn,refreshBtn, arrivedBtbn;
    private final int FINE_PERMISSION_CODE = 1;

    private static final String TAG = "DisplayLocation";
    private Context coordinatesContext;
    private WeakReference<Activity> weakActivity;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;
    private boolean canGetLocation = false;
    private Location lastLocation = null;
    private  addDriverHelper driverobjModel;
    LatLng locLatLng;
    Geocoder geocoder;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    //retrieve userlocation var
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000; /* 3 secs */

    private GeoApiContext mGeoApiContext;
    GoogleMap googleMap;
    Marker marker;
    private Intent serviceIntent;
    private Polyline polyline;
    private LocationUpdateManager locationUpdateManager;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private final static long UPDATE_INTERVAL = 30 * 1000;  /* 30 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    private myDestinationUtil mdestinationUtil = null;
    private  myDestination mDestination = null;
    private UserLocation mUserPosition;
    LocationData locDestination;
    private LatLngBounds mMapBoundary;
    private List<Marker> destinationMarkerlist = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentuser;
    FirebaseDatabase firebaseDatabase;
    TextView driverTxtvw,plateTxtvw,bodyTxtvw,loc1,loc2,contactDisp,addressDisp,operatorDisp,arrivedLabel;
    private double dest1Lat = 6.758026;
    private double dest1Long = 125.309154;
    private static final LatLng dest1Latlng = new LatLng(6.758026, 125.309154);
    private static final LatLng dest2Latlng = new LatLng(6.749117, 125.355493);

    private String driverName,driverPlate,driverBody,driverOperator,driverContact,driverAddress;
    private String userUID;
    private String parentContact;
    private String PassengerName;
    private static int SMS_status = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_location);

        initWidg();
        //initialize userUID
        auth  = FirebaseAuth.getInstance();
        currentuser = auth.getCurrentUser();
        userUID = currentuser.getUid();

        weakActivity = new WeakReference<>(this);

        //Check if gps is enabled
        if (!GPSUtil.isGPSEnabled(this)) {
            //GPSAlertDialog.showGPSAlertDialog(this);
            GPSDialog();
        } else {
            //TODO
            //startLocationUpdates();
        }

        setdestinationLoc1();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getLastLocation();
        }
        loadDriverdetails();


        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeMap();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopLocationUpdate();
                stopService(serviceIntent);
                serviceIntent = null;
                mHandler.removeCallbacks(updatelocRunnable);
                if(userUID != null){
                    deleteUserLocation(userUID);
                }
            }
        });
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastLocation != null) {

                    //Check if gps is enabled
                    if (!GPSUtil.isGPSEnabled(DisplayLocation.this)) {
                        //GPSAlertDialog.showGPSAlertDialog(this);
                        GPSDialog();
                    }
                    else {
                        //TODO
                        //startLocationUpdates();

                        //use the foregroundservice to continue running the update when the app runs in background
                        //startService(serviceIntent);

                        if (serviceIntent==null){
                            serviceIntent = new Intent(DisplayLocation.this, LocationForegroundService.class);
                            startLocationService();
                            mHandler.postDelayed(updatelocRunnable,UPDATE_INTERVAL);
                            startBtn.setText(R.string.stop);

                           // getparentContact();
                            if(SMS_status==0){
                                //Run getparentContact and if parentContact is not null, send SMS
                                getparentContact();

                            }
                        }else {
                            startBtn.setText(R.string.start);
                            stopService(serviceIntent);
                            serviceIntent = null;
                            mHandler.removeCallbacks(updatelocRunnable);
                            if(userUID != null){
                                deleteUserLocation(userUID);
                            }
                        }

                    }

                    //sendSMS("09186691010",defaultMSG);
                    //start location update and save the location data to firebase
                    //startLocationUpdates();

                    //Toast.makeText(DisplayLocation.this, "", Toast.LENGTH_SHORT).show();

                } else {


                }

            }
        });

        arrivedBtbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Btnstatus = arrivedBtbn.getText().toString();

                Log.d(TAG,"Get Button Status: " +Btnstatus);

                if (serviceIntent!=null){

                    if (Btnstatus.equals("Arrived")){

                        MyUtil myUtil = (MyUtil) getApplication();
                        //pass the object to UserClient
                        UserClient userClient = myUtil.getUserClient();
                        User currentUser  = userClient.getUser();
                        String passengerNme = currentUser.getName();

                        String arrivedMessage = "Your studen/passenger "+ passengerNme + " has arrived to the destination";
                        //TODO retrieve address  latlng
                        sendSMS(parentContact,arrivedMessage);
                        Log.d(TAG,"Arrived Button CLick: " +arrivedMessage);

                        startBtn.setText(R.string.start);
                        startBtn.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.GONE);
                       // arrivedBtbn.setVisibility(View.GONE);
                        arrivedLabel.setVisibility(View.VISIBLE);
                        arrivedBtbn.setText(R.string.arrivedTextbtn);
                        stopService(serviceIntent);
                        serviceIntent = null;
                        mHandler.removeCallbacks(updatelocRunnable);
                    }else if(arrivedLabel.getVisibility() == View.VISIBLE){
                        Intent home = new Intent(DisplayLocation.this,HomePageActivity.class);
                        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(home);
                        finish();
                    }
                }else {
                    Toast.makeText(DisplayLocation.this,"You haven`t started yet",Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(serviceIntent!=null ){
                   // serviceIntent = null;
                    //stopService(serviceIntent);
                   // mHandler.removeCallbacks(updatelocRunnable);
                    showExitConfirmationDialog();

                }

            }
        });

    }
    private void initWidg(){
         startBtn =  findViewById(R.id.refBtn);
         driverTxtvw =findViewById(R.id.driverDispname);
         bodyTxtvw = findViewById(R.id.bodyDisp);
         plateTxtvw = findViewById(R.id.plateDisp);
         stopBtn = findViewById(R.id.stopBtn);
         loc1 = findViewById(R.id.loc1);
         loc2 = findViewById(R.id.loc2);
         contactDisp = findViewById(R.id.contactDisp);
         addressDisp = findViewById(R.id.addressDisp);
         operatorDisp = findViewById(R.id.operatorDisp);
         refreshBtn = findViewById(R.id.refreshBtn);
         arrivedBtbn = findViewById(R.id.arrivedLocBtn);
         cancelBtn = findViewById(R.id.cancelLocBtn);
         arrivedLabel = findViewById(R.id.arrivedlbl);
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS notification has been sent succesfully", Toast.LENGTH_SHORT).show();

            Log.d("SMS", "SMS notification has been sent succesfully: " + parentContact);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to send SMS notification", Toast.LENGTH_SHORT).show();

            Log.d("SMS", "Failed to send SMS notification" + parentContact);
            e.printStackTrace();
        }
    }
    private void getparentContact(){

        databaseReference = FirebaseDatabase.getInstance().getReference("Passenger")
                .child(userUID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the value of parentContact
                 parentContact = snapshot.child("parentContact").getValue(String.class);
                 PassengerName = snapshot.child("name").getValue(String.class);

                //TODO send sms
                if(parentContact!= null){

                    Log.d("ParentContact", "Value: " + parentContact);
                    driverName = driverobjModel.getName();
                    driverPlate = driverobjModel.getPlate();
                    driverBody = driverobjModel.getBody();
                    driverOperator = driverobjModel.getOperator();
                    driverContact = driverobjModel.getContact();
                    driverAddress = driverobjModel.getAddress();

                    //TODO retrieve address  latlng
//                    SmsUtils smsUtils = new SmsUtils();
//                    SmsUtils.sendSms(getApplicationContext(), parentContact, defaultMSG);
                    //sendSMS(parentContact,arrivedMessageget);
                    String defaultMSG1 = "Location monitoring of your Student/Passenger: " + PassengerName + " Driver: " + driverName + " Plate no: " + driverPlate + " Body no: " + driverBody + " Contact:" + driverContact;
                    String defaultMSG2 = "Location monitoring of your Student/Passenger: " + PassengerName  + " Operator: " + driverOperator + " Address: " + driverAddress;
                    sendSMS(parentContact,defaultMSG1);
                    Log.d("SENDSMS:", "Value: " + defaultMSG1);
                    SMS_status =1;
                   // sendSMS(parentContact,arrivedMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void setdestinationLoc1(){
        MyUtil myUtil = (MyUtil) getApplicationContext();
        //TODO add condition select destination
        mDestination = new myDestination(dest1Lat,dest1Long);
        //set destination
        mdestinationUtil = myUtil.getmDestinationUtil();
        mdestinationUtil.setmDestionation(mDestination);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void getLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    lastLocation = location;
                   // LocationData locationData = new LocationData(lastLocation.getLatitude(),lastLocation.getLongitude());

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert mapFragment != null;
                    mapFragment.getMapAsync(DisplayLocation.this);

                    //---NOT USED GEOAPI Directions Api--- NEED BILLING
                    /*if(mGeoApiContext == null){
                        mGeoApiContext = new GeoApiContext.Builder()
                                .apiKey(getString(R.string.Api_Key_googleMapsDirection))
                                .build();
                    }*/
                    //setUserPosition();
                    //Toast.makeText(DisplayLocation.this,lastLocation.getLatitude()+":"+lastLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onMapReady(@NonNull GoogleMap googleMaps) {
        try {
            googleMap = googleMaps;

            // Set new LatLng with the last location update
            LatLng locLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

            // Add destination marker if available
            if (mDestination != null) {

                Bitmap destimg = BitmapFactory.decodeResource(getResources(), R.drawable.destination_icon);
                Bitmap destIcon = Bitmap.createScaledBitmap(destimg, 130, 130, false);

                LatLng destLatLng = new LatLng(mDestination.getLatitude(), mDestination.getLongitude());
                Marker destinationMarker = googleMaps.addMarker(new MarkerOptions()
                        .position(destLatLng)
                        .title("Destination")
                        .icon(BitmapDescriptorFactory.fromBitmap(destIcon)));
               // destinationMarkerlist.add(destinationMarker);

                //Requires Billing to Use Directions API from Google Cloud
                //calculateDirections(locLatLng,destLatLng);
                //drawPolyline(locLatLng,destLatLng);
            }

            // Add your location marker
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.custommarker2);
            Bitmap iconimg = Bitmap.createScaledBitmap(image, 130, 130, false);

            MarkerOptions myLocationMarkerOptions = new MarkerOptions()
                    .position(locLatLng)
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.fromBitmap(iconimg));

            Marker myLocationMarker = googleMap.addMarker(myLocationMarkerOptions);


            // Move camera to the location with zoom value 15
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 15));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawPolyline(LatLng origin, LatLng destination) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origin, destination)
                .width(12)
                .color(Color.BLUE);

        googleMap.addPolyline(polylineOptions);
    }
    private void updatePolyline(LatLng newLocation) {
        if (polyline == null) {
            // Create polyline if it doesn't exist
            PolylineOptions polylineOptions = new PolylineOptions()
                    .width(5)
                    .color(Color.RED);
            polyline = googleMap.addPolyline(polylineOptions);
        }

        // Add the new location to the polyline
        List<LatLng> points = polyline.getPoints();
        points.add(newLocation);
        polyline.setPoints(points);
    }


    // permission result if OK then run the getLastLocation which calls a new location update
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //start location updates
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getLastLocation();
                }
            }else {

                Toast.makeText(this,"Location permission denied, please allow the permission",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //update location using runnable with Interval
    private Runnable updatelocRunnable = new Runnable() {
        @Override
        public void run() {
            // Update marker position
            MyUtil myUtil = (MyUtil) getApplication();
            UpdateLocationUtil updateLocationUtil = myUtil.getUpdateLocationUtil();
            if (updateLocationUtil.getLocationData() == null){
                return;
            }
            double newLatitude = updateLocationUtil.getLocationData().getLatitude() + 0.001;
            double newLongitude = updateLocationUtil.getLocationData().getLongitude() + 0.001;

            //double newLatitude = locationUpdate.getLatitude() + 0.001;
            //double newLongitude = locationUpdate.getLongitude() + 0.001;
            locLatLng = new LatLng(newLatitude, newLongitude);

            // Update marker position on the map
            marker.setPosition(locLatLng);

            // Move camera to the updated marker position
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng,14));

            //Toast.makeText(DisplayLocation.this,"retrieving updated locations", Toast.LENGTH_SHORT).show();

            // Schedule the next update
            mHandler.postDelayed(this, UPDATE_INTERVAL);
            // retrieveUserLocations();
            //mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
        }
    };


    private void loadDriverdetails(){

        Bundle bundle = getIntent().getExtras();
        driverobjModel = new addDriverHelper();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //String email = currentUser.getEmail();
        DatabaseReference userRef = firebaseDatabase.getReference("Passenger")
                .child(userUID);
        if (bundle !=null){
            //integrate data from  bundle to objModel
            driverobjModel.setValuesFromBundle(bundle);

            String name = bundle.getString("name");
            String body = bundle.getString("body");
            String plate = bundle.getString("plate");
            String driverID = bundle.getString("qrCode");
            String operator = bundle.getString("operator");
            String contact = bundle.getString("contact");
            String address = bundle.getString("address");
            //retrieve user data then update the UserClientUtil includes driverID
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        //retrieve userdetails
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                       // String uid = snapshot.child("uid").getValue(String.class);
                        //create a user object with the retrieved user data
                        User user = new User(email,userUID,name,driverID);
                        Log.d(TAG,"Driver UID:" + driverID);
                        MyUtil myUtil = (MyUtil) getApplication();
                        //pass the object to UserClient
                        UserClient userClient = myUtil.getUserClient();
                        userClient.setUser(user);
                    }
                    //Toast.makeText(DisplayLocation.this,"Failed to retrieve user details", Toast.LENGTH_SHORT).show();

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            //display to textview
            driverTxtvw.setText(name);
            bodyTxtvw.setText(body);
            plateTxtvw.setText(plate);
            addressDisp.setText(address);
            contactDisp.setText(contact);
            operatorDisp.setText(operator);
        }else {
            //Toast.makeText(this,"Failed to load Driver`s data",Toast.LENGTH_SHORT).show();
        }

    }
    //GPS Dialog which pop up when the GPS is not Enabled
    public void GPSDialog(){
        //open Dialog layout for adding Driver
        Dialog gpsDialog = new Dialog(this);
        gpsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //set the layout activity view
        gpsDialog.setContentView(R.layout.gps_alertdialog);
        gpsDialog.setCancelable(false);

        Button settings = gpsDialog.findViewById(R.id.settings);
        Button Cancel = gpsDialog.findViewById(R.id.cancel);
        gpsDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        gpsDialog.show();
       settings.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               //redirect to Location device settings
               GPSUtil.showGPSSettings(DisplayLocation.this);
               gpsDialog.dismiss();
           }
       });
       Cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               gpsDialog.dismiss();

           }
       });

    }

    private void initializeMap() {
        // Implement your map initialization logic here
        // For example, move the camera to a specific location or add markers
        // ...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getLastLocation();
        }

    }
    private boolean hasLocationPermission() {
        // Check if the app has location permission
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void deleteUserLocation(String uid) {

        // Get reference to the Userlocation table in Firebase
        DatabaseReference userLocationRef = FirebaseDatabase.getInstance().getReference().child("Userlocation");

        // Reference to the specific UID node
        DatabaseReference uidRef = userLocationRef.child(uid);

        // Remove the node
        uidRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Node deleted successfully
                    // You can perform any additional actions here
                })
                .addOnFailureListener(e -> {
                    // Failed to delete node
                    // Handle the error
                });
    }


    //---- NOT USED YET ----------
    private void getAddressDetails(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Get details from the address object
                String addressLine = address.getAddressLine(0); // Full address
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();

                //used the details
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //---------NOT USED--------- REQUIRES BILLING TO USE DIRECTIONS API
    /*private void calculateDirections(LatLng origin, LatLng destination) {
     *//* GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyAXGaQMwN6bINACi50QIbl8193TCJbcnis")
                .build();*/
    /*
        com.google.maps.model.LatLng destinationDirect = new com.google.maps.model.LatLng(
                destination.latitude,
                destination.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(new com.google.maps.model.LatLng(
                origin.latitude,
                origin.longitude
        ));
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destinationDirect).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
            }

        *//*DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)  // Adjust as needed (DRIVING, WALKING, BICYCLING, TRANSIT)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        // Handle the result and draw the route on the map
                        drawRouteOnMap(result);
                    }*/
    /*

                    @Override
                    public void onFailure(Throwable e) {
                        // Handle failure
                        Log.e(TAG, "onFailure: " + e.getMessage() );
                    }
                });

    }
    private void drawRouteOnMap(DirectionsResult result) {
        if (result != null && result.routes != null && result.routes.length > 0) {
            List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());

            Log.d("Polyline", "Number of points: " + decodedPath.size());

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(decodedPath)
                    .width(12)
                    .color(Color.BLUE);

            googleMap.addPolyline(polylineOptions);
        }
    }
    */

    //display the map
    /*@Override
    public void onMapReady(@NonNull GoogleMap googleMaps) {
        try {

            googleMap = googleMaps;
            //addMapMarkers();
            //Toast.makeText(DisplayLocation.this,"adding map markers on Mapready",Toast.LENGTH_SHORT).show();

            //set new latlang with the lastloaction update
            locLatLng = new LatLng( lastLocation.getLatitude(),lastLocation.getLongitude());
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.custommarker2);
            Bitmap iconimg = Bitmap.createScaledBitmap(image, 130, 130, false);
            if (mDestination != null){

                LatLng destlatlang = new LatLng(mDestination.getLatitude(),mDestination.getLongitude());
                Marker destinationmarker = googleMaps.addMarker(new MarkerOptions().position(destlatlang).title("Destination"));
                destinationMarkerlist.add(destinationmarker);

            }

            //map markers
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(locLatLng)
                    .title("Custom Marker Title")
                    .snippet("Custom Marker Snippet")
                    .icon(BitmapDescriptorFactory.fromBitmap(iconimg));
            //add the marker
            marker = googleMap.addMarker((markerOptions).position(locLatLng).title("My Location"));
            //float zoomlvl = 5.0f;
           // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlvl));
            if(locLatLng != null){
                //move the camera to location with zoom value 15 can be change
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng,15));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    */

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        startService(serviceIntent);
    }
    private void stopLocationService(Intent serviceIntent) {
        stopLocationService(serviceIntent);
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        /*if(serviceIntent==null){

            Intent intent = new Intent( DisplayLocation.this, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }else {
            // showExitConfirmationDialog();
        }*/
    }

    private void showExitConfirmationDialog() {
        Activity activity = weakActivity.get();
        if (activity != null && !activity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Are you sure you want to cancel?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(DisplayLocation.this, HomePageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            stopService(serviceIntent);
                            mHandler.removeCallbacks(updatelocRunnable);
                            startActivity(intent);
                            finish();
                            //  activity.finish(); // Close the activity
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    })
                    .show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Start updating marker position when the activity is resumed
        if (serviceIntent!= null){
            //mHandler.postDelayed(updatelocRunnable, UPDATE_INTERVAL);

            Log.d("onResume:", "Failed to initialize Map, seviceIntent is not null");
        }else {
            //initializeMap();
            Log.d("onResume:", "initialize Map");
        }

        //getLastLocation();

        //TODO -- make a condition to resume updatelocation
       // mHandler.postDelayed(updatelocRunnable,UPDATE_INTERVAL);
        //startUserLocationsRunnable();
     /*   if (requestingLocationUpdates) {
            startLocationUpdates();
        }*/
    }

    private void startLocationUpdates() {
        //call locationupdatemanager to start updating/saving the user location to database
        locationUpdateManager = new LocationUpdateManager(this);
        locationUpdateManager.startLocationUpdates(this);
        //start updating marker in user map


    }
    private void stopLocationUpdate(){
        //mHandler.removeCallbacks(mRunnable);
        if (locationUpdateManager != null){
            locationUpdateManager.stopLocationUpdates();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(updatelocRunnable);
        // Stop location updates when the activity is destroyed
        if (locationUpdateManager != null){
            locationUpdateManager.stopLocationUpdates();

        }
    }
}