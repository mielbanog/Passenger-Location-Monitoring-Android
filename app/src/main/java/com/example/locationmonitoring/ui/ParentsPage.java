package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.example.locationmonitoring.adapter.RVAdapterStudent;
import com.example.locationmonitoring.model.StudentModelClass;
import com.example.locationmonitoring.util.ParentStudntCodeUtils;
import com.example.locationmonitoring.util.PassengerDatalistfromStdCodeUtil;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class ParentsPage extends AppCompatActivity {

    private static final String TAG = "ParentsPage";
    private MenuItem addstdBtn;
    Button logoutbtn;
    TextView testname;
    String StudentsQRCODE;
    String ParentContact;
    RecyclerView recyclerView;

    IntentIntegrator integrator;
    ImageView qrTxtvw;
     List<StudentModelClass> userList;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    ValueEventListener eventListener;


    public  void onStart(){
        super.onStart();
        //TODO verification of currentuser login
        firebaseAuth  = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        Bundle bundle = getIntent().getExtras();

        if (user != null){
            //testname.setText(user.getEmail());

        } else if (bundle != null) {
            String name = String.valueOf(bundle.getString("name"));
             //testname.setText(name);
        } else {
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_page);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Dashboard");
        }
        initWidget();
        loadStdPassenger();
        //loadStdnt();
       // retrieveStdnt();


    }

    private void initWidget(){
        recyclerView = findViewById(R.id.StdntrecyclerView);
        
        //----NOT USED----\\
//        logoutbtn = findViewById(R.id.logoutBtn);
//        testname = findViewById(R.id.nametest);
    }

    private void loadStdPassenger(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ParentsPage.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();
        //create instances userList
        userList = new ArrayList<>();
        //create instances of recyclerview with obj from List/userList


        FirebaseUser firebaseAuth1 = FirebaseAuth.getInstance().getCurrentUser();
        ParentStudntCodeUtils.getStudentsFromParent(firebaseAuth1, new ParentStudntCodeUtils.OnStudentListFetchedListener() {
            @Override
            public void onStudentListFetched(List<String> studentList) {

                PassengerDatalistfromStdCodeUtil.getPassengerDataListFromStdCodes(studentList, passengerDataList -> {

                    // Handle the retrieved passenger data list
                    // Do something with each passenger data

                    RVAdapterStudent adapter = new RVAdapterStudent(ParentsPage.this,passengerDataList);
                   // Log.d(TAG,"LoadPassenger:"+ passengerDataList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ParentsPage.this));
                    recyclerView.setAdapter(adapter);

                    dialog.dismiss();
                });
                dialog.dismiss();
                // Handle the retrieved student list
               /* for (String student : studentList) {

                    dialog.dismiss();
                    Log.d(TAG, "Student: " + student);
                }*/
            }
        });

    }


    public void scanQR(){
        integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setCameraId(0); // Use a specific camera of the device
        integrator.setOrientationLocked(false);

        // Start the QR code scanner
        integrator.initiateScan();
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Receive the result from the QR code scanner
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // Handle cancelled scan
                Log.d("Parents", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // Handle scan result (result.getContents())
                String scannQR = result.getContents();
                //Validate qrCode to Passenger database if it exist
                databaseReference = FirebaseDatabase.getInstance().getReference("Passenger")
                        .child(scannQR);
                databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            if (task.getResult().exists()){
                                DataSnapshot dataSnapshot = task.getResult();
                                String qrVal = dataSnapshot.child("uid").getValue(String.class);
                                if (qrVal != null && qrVal.equals(scannQR)){
                                 StudentsQRCODE = result.getContents();
                                 addStudent();
                                }else {
                                    Toast.makeText(ParentsPage.this,"Validation Error", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(ParentsPage.this,"Invalid QR Code", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            //Handle errors
                            Toast.makeText(ParentsPage.this,"Error getting data from database",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void addStudent(){
        //first authenticate parent

        databaseReference = FirebaseDatabase.getInstance().getReference("Parents")
                .child(user.getUid());
        DatabaseReference saveQRpath = databaseReference.child("stdcode").child(StudentsQRCODE);

        DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference().child("Parents");
        DatabaseReference validateRef = parentsRef.child(user.getUid());
        validateRef.child("stdcode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot stdcodeSnapshot : snapshot.getChildren()) {
                        //extract data from snapshop
                        String storedStdCode = stdcodeSnapshot.getValue(String.class);
                        //validate student qrcode if exist
                        if (StudentsQRCODE.equals(storedStdCode)) {
                            Toast.makeText(ParentsPage.this,"Student is already exist",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                   // Toast.makeText(ParentsPage.this,"",Toast.LENGTH_SHORT).show();
                    saveQRpath.setValue(StudentsQRCODE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ParentsPage.this,"Student has been added successfully",Toast.LENGTH_SHORT).show();
                            updatePassengerdetails(StudentsQRCODE);
                            loadStdPassenger();
                        }
                    });
                }
                else {

                   /* saveQRpath.setValue(StudentsQRCODE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ParentsPage.this,"Student has been added",Toast.LENGTH_SHORT).show();
                        }
                    });*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            //handle cancelled
            }
        });


        //
    }

    private void updatePassengerdetails(String userUID){



        databaseReference = FirebaseDatabase.getInstance().getReference("Parents")
                .child(user.getUid());
        DatabaseReference contactRef = databaseReference.child("parentPhone");
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                     ParentContact = snapshot.getValue(String.class);
                    Log.d(TAG,"ParentContact:"+ ParentContact);

                    //add parentContact to Passenger
                    databaseReference = FirebaseDatabase.getInstance().getReference("Passenger")
                            .child(userUID);
                    DatabaseReference parentContactRef = databaseReference.child("parentContact");
                    parentContactRef.setValue(ParentContact).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Log.d(TAG,"ParentContact: successfully inserted "+ ParentContact+ " to :" + userUID);
                            Toast.makeText(ParentsPage.this,"Student successfully updated",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void deleteStd(String uid) {

        // Get reference to the Userlocation table in Firebase
        DatabaseReference userLocationRef = FirebaseDatabase.getInstance().getReference().child("Parents").child("stdcode");

        // Reference to the specific UID node
        DatabaseReference uidRef = userLocationRef.child(uid);

        // Remove the node
        uidRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Node deleted successfully
                    // You can perform any additional actions here

                    Toast.makeText(this,"Succesfully remove User",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to delete node
                    // Handle the error
                    Toast.makeText(this,"Failed to delete User",Toast.LENGTH_SHORT).show();
                });
    }
    private void addstudentCode(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parentsmenu, menu);
        addstdBtn = menu.findItem(R.id.addstdBtn);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.addQR:
               showDialogForAddingDriver();
               return true;
            case R.id.ScanQr:
                //TO DO
                scanQR();
                return true;
            case R.id.parentQR:
                //TO DO
                return true;
            case R.id.parentlogout:
                //TO DO
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ParentsPage.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogForAddingDriver() {
        try {
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Passenger");

            // Open Dialog layout for adding Driver
            Dialog add = new Dialog(this);
            add.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Set the layout activity view
            add.setContentView(R.layout.addstudentcode_layout);
            add.setCancelable(false);
            TextInputEditText studentCode = add.findViewById(R.id.studentCodetxt);
            // ... other views
            AppCompatButton addBtn = add.findViewById(R.id.addstdBtn);
            AppCompatButton cancelBtn = add.findViewById(R.id.cancelBtn);
            add.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            add.show();

            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String qrValue = String.valueOf(studentCode.getText());
                    addStudentviaCode(qrValue);
                    Log.d(TAG,"addSudentviaCode:"+ qrValue);
                    add.dismiss();
                }
            });
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    add.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addStudentviaCode(String studentqr){
        //first authenticate parent
        Log.d(TAG,"ValueStudent: "+ studentqr);
        databaseReference = FirebaseDatabase.getInstance().getReference("Parents")
                .child(user.getUid());
        DatabaseReference saveQRpath = databaseReference.child("stdcode").child(studentqr);

        DatabaseReference parentsRef = FirebaseDatabase.getInstance().getReference().child("Parents");
        DatabaseReference validateRef = parentsRef.child(user.getUid());
        validateRef.child("stdcode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot stdcodeSnapshot : snapshot.getChildren()) {
                        //extract data from snapshop
                        String storedStdCode = stdcodeSnapshot.getValue(String.class);
                        //validate student qrcode if exist
                        if (studentqr.equals(storedStdCode)) {
                            Toast.makeText(ParentsPage.this,"Student is already exist",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    // Toast.makeText(ParentsPage.this,"",Toast.LENGTH_SHORT).show();
                    saveQRpath.setValue(studentqr).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ParentsPage.this,"Student has been added successfully",Toast.LENGTH_SHORT).show();
                            updatePassengerdetails(studentqr);
                            loadStdPassenger();
                        }
                    });
                }
                else {

                   /* saveQRpath.setValue(StudentsQRCODE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ParentsPage.this,"Student has been added",Toast.LENGTH_SHORT).show();
                        }
                    });*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle cancelled
            }
        });


        //
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( ParentsPage.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK |
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}