package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.addDriverHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {
    FirebaseAuth auth;
    View logoutBtn;
    TextView userEmail,txtQR;

    IntentIntegrator integrator;
    Button ScanBtn,GenerateQRBtn,copyBtn;
    LinearLayoutCompat qrCodeTextLayout;
    ImageView DisplayQRCODE;
    String scannedData;
    private String resqrcode;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    List<addDriverHelper> loadDriver;
    ValueEventListener eventListener;

    //TODO authenticate
    //TODO verification, if user exist in passenger database
    public  void onStart(){
        super.onStart();
        firebaseAuth  = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        Bundle bundle = getIntent().getExtras();

        if (user != null){
            //userEmail.setText(user.getEmail());

        } else if (bundle != null) {
            String name = String.valueOf(bundle.getString("name"));
            //userEmail.setText(name);
        } else {
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Passenger");
        }
        //initialize textview,button,etc
        initWidget();
        //authenticate should be declared in onStart()
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(HomePageActivity.this, passengerlogin.class);
            startActivity(intent);
            finish();
        } else {
            userEmail.setText("Welcome!" + " " + user.getEmail());
        }

        //generate QRCODE button
        GenerateQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qenerateQR();
            }
        });

        //Scan QRCODE button
        ScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    scanQR();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qrcodeVal = txtQR.getText().toString();
                if (qrcodeVal != null){

                    copyToClipboard(qrcodeVal);
                }else {
                    Toast.makeText(HomePageActivity.this,"Unable to retrieve QrCode",Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), passengerlogin.class);
            startActivity(intent);
            Toast.makeText(HomePageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            finish();

        });
    }

    private  void initWidget(){
        logoutBtn = findViewById (R.id.logout_btn);
        userEmail = findViewById(R.id.user_email);
        ScanBtn = findViewById(R.id.ScanBtn);
        //txtQR = findViewById(R.id.txtqr);
        GenerateQRBtn = findViewById(R.id.GenerateBtn);
        DisplayQRCODE = findViewById(R.id.qrDisp);
        copyBtn = findViewById(R.id.copyBtn);
        qrCodeTextLayout = findViewById(R.id.qrcodeViewLayout);
        txtQR = findViewById(R.id.qrCodeTxt);
    }

    public void scanQR(){
        integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setCameraId(0); // Use a specific camera of the device
        integrator.setOrientationLocked(false);

        // Start the QR code scanner
        integrator.initiateScan();
    }

    //load Driver data
    public void loadDriverData(){
        try {
            //Loading animation using AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.loading);
            AlertDialog dialog = builder.create();
            dialog.show();
            //create a list to store driver data, however no need for arraylist since only 1 driver data is retrieved from DB
           // loadDriver = new ArrayList<>();
            //drivers path reference
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers");
            //create a query to find the driver with the specified qrCode which is the scannedData result
            Query query = databaseReference.orderByChild("qrCode").equalTo(scannedData);
            query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Loop through the results to find the matching driver
                        // Loop is not needed since the query is already equal to scanned QRCODE data. Should get the value after task is succesfull
                        for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                            //extract data from the snapshot using driverhelper
                            addDriverHelper driverDataClass = dataSnapshot.getValue(addDriverHelper.class);
                            //add the retrieved data to list, however it is not use so it can be remove.
                            //loadDriver.add(driverDataClass);
                            //create new intent to displaylocation activity and pass the retrieved data
                            Intent intent = new Intent(HomePageActivity.this, DisplayLocation.class);
                            //get the data from driverHelper and send the data to another activity using putExtra
                            intent.putExtra("name",driverDataClass.getName());
                            intent.putExtra("plate",driverDataClass.getPlate());
                            intent.putExtra("body",driverDataClass.getBody());
                            intent.putExtra("qrCode",driverDataClass.getQrCode());
                            intent.putExtra("contact",driverDataClass.getContact());
                            intent.putExtra("address",driverDataClass.getAddress());
                            intent.putExtra("operator",driverDataClass.getOperator());
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                        dialog.dismiss();
                    } else {
                        // Handle errors
                        dialog.dismiss();
                        Toast.makeText(HomePageActivity.this,"Failed to load driver`s", Toast.LENGTH_SHORT);
                        Log.e("FirebaseDatabase", "Error getting driver data", task.getException());
                    }
                }

            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Text", text);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    private void qenerateQR(){
        try {
            //loading animation
            AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.loading);
            AlertDialog dialog = builder.create();
            dialog.show();
            //authenticate user and get UID then encode to barcode format
            FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentuser!= null){
                String UID = currentuser.getUid();
                BitMatrix bitMatrix = new MultiFormatWriter().encode(UID, BarcodeFormat.QR_CODE, 300, 300);
                Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

                for (int x = 0; x < 300; x++) {
                    for (int y = 0; y < 300; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                    }
                }
                //display the barcode
                DisplayQRCODE.setImageBitmap(bitmap);
                txtQR.setText(UID);
                qrCodeTextLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();


            }else {
                Toast.makeText(this,"Unable to authenticate User",Toast.LENGTH_SHORT);
                dialog.dismiss();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Result for Scanned QRCode
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Receive the result from the QR code scanner
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // Handle cancelled scan
                Log.d("Passenger", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // Handle scan result (result.getContents())
                //store scanned QRCode data to string and then run the loadDriverData()
                scannedData = result.getContents();
                loadDriverData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //Inflate Option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.passengermenu, menu);
        return true;
    }
    //Option Menu fun..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.btnLogout:
                //TO DO
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomePageActivity.this, passengerlogin.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.Scan:

                scanQR();
                return true;
            case R.id.myQRCode:
                qenerateQR();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( HomePageActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK |
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}