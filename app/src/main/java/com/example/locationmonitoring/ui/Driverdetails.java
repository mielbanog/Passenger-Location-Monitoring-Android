package com.example.locationmonitoring.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.addDriverHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Driverdetails extends AppCompatActivity {
    TextView driverName,plate,body,operatortxt,addresstxt,contactxt;
    ImageView qrImageView;
    Button savebtn;
    Bitmap saveImage;
    private static final int REQUEST_WRITE_STORAGE = 112;

    List<addDriverHelper> loadDriver;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverdetails);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Driver Details");

        }
        init();
        loadDriverDetails();

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = body.getText().toString();
                String desc = driverName.getText().toString();
                Driverdetails.saveQRCodeToGallery(saveImage,title,desc,getContentResolver(),Driverdetails.this);
            }
        });

    }


    private void loadDriverDetails(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Driverdetails.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String driverUID = String.valueOf(extras.getString("qrcode"));

            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("Drivers");

            Query query = driverRef.orderByChild("qrCode").equalTo(driverUID);
            query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@androidx.annotation.NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){

                        // Loop through the results to find the matching driver
                        for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                            //extract data from the snapshot using driverhelper
                            addDriverHelper driverDataClass = dataSnapshot.getValue(addDriverHelper.class);
                            if (driverDataClass!= null){

                                driverName.setText(driverDataClass.getName());
                                plate.setText(driverDataClass.getPlate());
                                body.setText(driverDataClass.getBody());
                                operatortxt.setText(driverDataClass.getOperator());
                                contactxt.setText(driverDataClass.getContact().toString());
                                addresstxt.setText(driverDataClass.getAddress());

                                try {
                                    if (driverUID != null){

                                        BitMatrix bitMatrix = new MultiFormatWriter().encode(driverUID, BarcodeFormat.QR_CODE, 300, 300);
                                        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

                                        for (int x = 0; x < 300; x++) {
                                            for (int y = 0; y < 300; y++) {
                                                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                                            }
                                        }

                                        qrImageView.setImageBitmap(bitmap);
                                        saveImage = bitmap;
                                    }else {
                                        Toast.makeText(Driverdetails.this,"Error loading QrCode",Toast.LENGTH_SHORT).show();
                                    }

                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        dialog.dismiss();

                    }
                    else {
                            Toast.makeText(Driverdetails.this,"Invalid QR Code", Toast.LENGTH_SHORT).show();
                        }
                    }

            });
        }
    }

    private void init(){
        driverName = findViewById(R.id.disDrivername);
        plate = findViewById(R.id.disPlate);
        body = findViewById(R.id.dispBody);
        qrImageView = findViewById(R.id.QrImagevw);
        savebtn = findViewById(R.id.saveqrBtn);
        operatortxt = findViewById(R.id.dispOperator);
        contactxt = findViewById(R.id.dispContact);
        addresstxt = findViewById(R.id.dispAddr);
    }
    public static void saveQRCodeToGallery(Bitmap qrCodeBitmap, String title, String description, ContentResolver contentResolver, Context context) {
        // Create a ContentValues object to store the image details
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png"); // Assuming the QR code is in PNG format

        // Get the external content URI for the Images Media
        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            // Open an OutputStream to the content URI and write the QR code bitmap to it
            if (imageUri != null) {
                OutputStream outputStream = contentResolver.openOutputStream(imageUri);
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                if (outputStream != null) {
                    outputStream.close();

                    // Notify the media scanner about the new image
                    scanFile(context, new String[]{imageUri.getPath()});

                    Toast.makeText(context, "QR Code saved to Gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private static void scanFile(Context context, String[] paths) {
        MediaScannerConnection.scanFile(
                context,
                paths,
                null,
                (path, uri) -> {
                    // Scanned file is available at the 'uri' location
                }
        );
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the image now.
                // You may want to check if the image is already generated before calling saveImageToExternalStorage.
            } else {
                // Permission denied, inform the user.
                Toast.makeText(this, "Permission denied. Cannot save QR Code image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        isDestroyed();
        finish();
    }


   /* private void loadData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String resname = String.valueOf(bundle.getString("name"));
            String resbody = String.valueOf(bundle.getString("body"));
            String resplate = String.valueOf(bundle.getString("plate"));
            String resqrcode = String.valueOf(bundle.getString("qrcode"));

            // Generate QR code and set it to the ImageView
            try {
                if (resqrcode != null){

                    BitMatrix bitMatrix = new MultiFormatWriter().encode(resqrcode, BarcodeFormat.QR_CODE, 300, 300);
                    Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

                    for (int x = 0; x < 300; x++) {
                        for (int y = 0; y < 300; y++) {
                            bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                        }
                    }

                    qrImageView.setImageBitmap(bitmap);
                    saveImage = bitmap;
                }else {
                    Toast.makeText(this,"Error loading QrCode",Toast.LENGTH_SHORT).show();
                }

            } catch (WriterException e) {
                e.printStackTrace();
            }

        }
    }*/
}