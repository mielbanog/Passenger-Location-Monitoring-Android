package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.example.locationmonitoring.adapter.RVadapterDriver;
import com.example.locationmonitoring.model.addDriverHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class adminpage extends AppCompatActivity {

    FloatingActionsMenu Flmenu;
    FloatingActionButton addDriver,logoutBtn,drivers;
    TextView Name;
    ProgressBar progressBar;
    Toolbar toolbar;
    List<addDriverHelper> dataList;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    FirebaseUser currentUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    addDriverHelper addHelper;

    public  void onStart(){
        super.onStart();
        //TODO verification of currentuser login
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Bundle bundle = getIntent().getExtras();
        
        if (currentUser != null){
            Name.setText(currentUser.getEmail());

        } else if (bundle != null) {
            String name = String.valueOf(bundle.getString("name"));
            Name.setText(name);
        } else {
            Intent intent = new Intent(getApplicationContext(), admin.class);
            startActivity(intent);
            finish();
        }
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpage);
        //
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initialize();
        loadDriverData();

        try {
            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(adminpage.this, admin.class);
                    startActivity(intent);
                    finish();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        drivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDriverData();
            }
        });
        addDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    addDriver();
            }
        });

    }
    private void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        Name = findViewById(R.id.titletxtadm);
        Flmenu = findViewById(R.id.FloatngActionMenu);
        addDriver = findViewById(R.id.AddDriverbtn);
        logoutBtn = findViewById(R.id.LogoutfBtn);
        drivers = findViewById(R.id.driversBtn);
        recyclerView = findViewById(R.id.recyclerView);
    }

    public void loadDriverData(){
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(adminpage.this);
            builder.setCancelable(false);
            builder.setView(R.layout.loading);
            AlertDialog dialog = builder.create();
            dialog.show();
            dataList = new ArrayList<>();
            RVadapterDriver adapter = new RVadapterDriver(adminpage.this,dataList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            databaseReference = FirebaseDatabase.getInstance().getReference("Drivers");
            eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dataList.clear();
                    for(DataSnapshot itemsnapshop: snapshot.getChildren()){
                        addDriverHelper driverDataClass = itemsnapshop.getValue(addDriverHelper.class);

                        dataList.add(driverDataClass);
                    }
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.dismiss();
                    Toast.makeText(adminpage.this,"Failed to load driver`s", Toast.LENGTH_SHORT);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addDriver(){
        //driver references
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Driver");

        //open Dialog layout for adding Driver
        Dialog add = new Dialog(addDriver.getContext());
        add.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //set the layout activity view
        add.setContentView(R.layout.activity_add_driver);
        add.setCancelable(false);
        TextInputEditText Drivername = add.findViewById(R.id.editDrivername);
        TextInputEditText Plate = add.findViewById(R.id.editPlate);
        TextInputEditText Bodynum = add.findViewById(R.id.editBody);
        TextInputEditText driverContact = add.findViewById(R.id.editContact);
        TextInputEditText driverAddress = add.findViewById(R.id.editAddress);
        TextInputEditText operatorName = add.findViewById(R.id.editOperator);
        AppCompatButton saveBtn = add.findViewById(R.id.savebtn);
        AppCompatButton Cancel = add.findViewById(R.id.cancelBtn);
        add.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        add.show();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar2.setVisibility(View.VISIBLE);
                String name, plate,body,qrCode,contact,operator,address;

                name = String.valueOf(Drivername.getText());
                plate = String.valueOf(Plate.getText());
                body = String.valueOf(Bodynum.getText());
                contact = String.valueOf(driverContact.getText());
                operator = String.valueOf(operatorName.getText());
                address = String.valueOf(driverAddress.getText());
                qrCode = databaseReference.push().getKey();
                if (TextUtils.isEmpty(name)){
                    Toast.makeText(adminpage.this,"Enter Fullname", Toast.LENGTH_SHORT).show();
                    Drivername.setError("Fullname is Required");
                    Drivername.requestFocus();
                }
                else if (TextUtils.isEmpty(plate)){
                    Toast.makeText(adminpage.this,"Enter plate number", Toast.LENGTH_SHORT).show();
                    Plate.setError("Plate number is Required");
                    Plate.requestFocus();
                }
                else if (TextUtils.isEmpty(body)){
                    Toast.makeText(adminpage.this,"Enter body number", Toast.LENGTH_SHORT).show();
                    Bodynum.setError("Body number is Required");
                    Bodynum.requestFocus();
                }
                else if (TextUtils.isEmpty(contact)){
                    Toast.makeText(adminpage.this,"Enter contact number", Toast.LENGTH_SHORT).show();
                    driverContact.setError("Contact number is Required");
                    Bodynum.requestFocus();
                }
                else if (TextUtils.isEmpty(address)){
                    Toast.makeText(adminpage.this,"Enter body number", Toast.LENGTH_SHORT).show();
                    driverAddress.setError("Address is Required");
                    Bodynum.requestFocus();
                }
                else if (TextUtils.isEmpty(operator)){
                    Toast.makeText(adminpage.this,"Enter body number", Toast.LENGTH_SHORT).show();
                    operatorName.setError("Operator is Required");
                    Bodynum.requestFocus();
                }
                else {
                    //run the helper method with the driver data / pass the value to helper method
                    driverResolver(name,plate,body,qrCode,contact,address,operator);
                    add.dismiss();
                }
            }
        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.dismiss();
            }
        });
        //Cancellable dialog
              /*  add.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        add.dismiss();
                    }
                });*/

    }

    //adding the driver data to Database using this helper method
    private void driverResolver(String name, String plate, String body, String qrcode, String contact, String address, String operator){
        AlertDialog.Builder builder = new AlertDialog.Builder(adminpage.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progressbar);
        AlertDialog dialog = builder.create();
        dialog.show();
        //model class for driver data
        addHelper = new addDriverHelper(name,plate,body,qrcode,operator,address,contact);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //path to store
        databaseReference = FirebaseDatabase.getInstance().getReference("Drivers");
        //adding the driver data(addHelper) to Drivers database
        databaseReference.child(qrcode).setValue(addHelper)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //close dialog
                dialog.dismiss();
                Toast.makeText(adminpage.this, "Driver has been added succesfully", Toast.LENGTH_SHORT).show();


            }
        });

    }
    private void setProgressBarOff(){

    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.manageAccount:
               //TODO
                return true;
            case R.id.Settings:
                //TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( adminpage.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK |
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}