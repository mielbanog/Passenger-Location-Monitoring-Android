package com.example.locationmonitoring;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.ui.Parents;
import com.example.locationmonitoring.ui.admin;
import com.example.locationmonitoring.ui.passengerlogin;

public class MainActivity extends AppCompatActivity {
    TextView t1;
    Button b1,b2,b3;
    private static final int MY_PERMISSIONS_REQUEST_ALL = 456; // Choose any unique integer value
    private static final int FINE_PERMISSION_CODE = 789;
    private static final int MY_PERMISSIONS_REQUEST_SMS = 123;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 789;
    private static final int FINE_LOCATION_PERMISSION_CODE = 1;
    private static final int SMS_PERMISSION_CODE = 2;
    private static final int CAMERA_PERMISSION_CODE = 3;
    private static final int REQUEST_CODE = 511;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the permissions are granted
        if (checkPermissions()) {
            // All permissions are granted, proceed with your app logic

        } else {
            // Request permissions
            reqCamPerm();

        }
        t1= (TextView) findViewById(R.id.textView);
        b1= (Button) findViewById(R.id.ADMIN);
        b2= (Button) findViewById(R.id.btnPassenger);
        b3= (Button) findViewById(R.id.btnParent);



        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, admin.class);
                startActivity(intent);
                finish();
            }});


        b2.setOnClickListener(new View.OnClickListener()

            {

                @Override
                public void onClick (View view){

                Intent intent = new Intent(MainActivity.this, passengerlogin.class);
                startActivity(intent);
                finish();
            }
            });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Parents.class);
                startActivity(intent);
                finish();

            }
        });

        }

    private boolean checkPermissions() {
        // Check if all necessary permissions are granted
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void reqSMSPerm(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SMS);
        }
    }
    private void reqCamPerm(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void reqLocationPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED  &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
            Log.d("requestLocation:","run request permission location If Fine");
        }
    }
    // load the permission setting screen
    private void loadPermissionPage(Activity context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivityForResult(intent, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                // Handle location permissions result
                if (areLocationPermissionsGranted()) {
                    // Location permissions granted, proceed with your location-related logic
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //reqLocationPerm();
                        //loadPermissionPage(this);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //reqLocationPerm();
                        //loadPermissionPage(this);
                    }
                    // Location permissions denied, handle accordingly (e.g., show a message to the user)

                }
                break;

            case MY_PERMISSIONS_REQUEST_SMS:
                // Handle SMS permission result
                if (areSmsPermissionsGranted()) {
                    // SMS permission granted, proceed with your SMS-related logic
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //reqLocationPerm();
                        loadPermissionPage(this);
                        Toast.makeText(this,"Please set the location permission to Allow all the time",Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Location PERM:","run request permission location If");
                } else {
                    // SMS permission denied, handle accordingly (e.g., show a message to the user)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                       //reqLocationPerm();

                        loadPermissionPage(this);
                        Toast.makeText(this,"Please set the location permission to Allow all the time",Toast.LENGTH_SHORT).show();
                    }

                    Log.d("Location PERM:","run request permission location");
                }
                break;

            case MY_PERMISSIONS_REQUEST_CAMERA:
                // Handle CAMERA permission result
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // CAMERA permission granted, proceed with your camera-related logic
                    // For example, start the camera preview or capture an image
                    reqSMSPerm();
                } else {
                    // CAMERA permission denied, handle accordingly (e.g., show a message to the user)
                    reqSMSPerm();
                }
                break;

            // Handle other permission requests as needed

            default:
                break;
        }
    }

    private boolean areLocationPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean areSmsPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_ALL) {
            // Check if all requested permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted, proceed with your app logic

            } else {
                // Some permissions were denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}



