package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.RegistrationHelper;
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class passengerregistration extends AppCompatActivity {
    EditText regName, regBirthday, regPhone, regGender, regEmail, regPassword;
    Button regBtn;
    ProgressBar progressBar;
    FirebaseDatabase database;
    DatabaseReference reference;
    private DatePickerDialog picker;
    private static final String TAG = "CreateAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengerregistration);

        initWidget();


        regBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                picker = new DatePickerDialog(passengerregistration.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        regBirthday.setText((month + 1) + "/" + day + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
               regPassenger();
            }

        });


    }

    public void regPassenger(){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Passenger");

        String name = regName.getText().toString();
        String birthday = regBirthday.getText().toString();
        String gender = regGender.getText().toString();
        String phone = regPhone.getText().toString();
        String email = regEmail.getText().toString();
        String password = regPassword.getText().toString();
        String qrCode = reference.push().getKey();

        String nameReg = "^[a-zA-Z]+(\\s[a-zA-Z]+)?(\\s[A-Za-z]{1})?$";
        Pattern namePattern = Pattern.compile(nameReg);
        String phoneReg = "^(09|\\+639|\\+6399|\\+63-9|\\+63-09)\\d{9}$";
        Matcher phoneMatcher;
        Pattern phonePattern = Pattern.compile(phoneReg);
        phoneMatcher = phonePattern.matcher(phone);

        if (TextUtils.isEmpty(name)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your name",
                    Toast.LENGTH_SHORT).show();
            regName.setError("Name is Required");
            regName.requestFocus();
        }
        else if (!namePattern.matcher(name).matches()) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter a valid name (first name, optional last name, optional middle initial)",
                    Toast.LENGTH_SHORT).show();
            regName.setError("Invalid Name");
            regName.requestFocus();
        }
        else if (TextUtils.isEmpty(birthday)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your birthday",
                    Toast.LENGTH_SHORT).show();
            regBirthday.setError("Birthday is Required");
            regBirthday.requestFocus();
        }
        else if (TextUtils.isEmpty(gender)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your gender",
                    Toast.LENGTH_SHORT).show();
            regGender.setError("Gender is Required");
            regGender.requestFocus();
        }
        else if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter 'Male' or 'Female' for gender", Toast.LENGTH_SHORT).show();
            regGender.setError("Gender must be 'Male' or 'Female'");
            regGender.requestFocus();
        }
        else if (TextUtils.isEmpty(phone)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your phone number",
                    Toast.LENGTH_SHORT).show();
            regPhone.setError("Phone Number is Required");
            regPhone.requestFocus();
        }
        else if (!phoneMatcher.find()) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please re-enter your phone number",
                    Toast.LENGTH_SHORT).show();
            regPhone.setError("Phone Number is not valid");
            regPhone.requestFocus();
        }
        else if (TextUtils.isEmpty(email)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your email",
                    Toast.LENGTH_SHORT).show();
            regEmail.setError("Email is Required");
            regEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(password)) {
            progressBarOff();
            Toast.makeText(passengerregistration.this, "Please enter your password",
                    Toast.LENGTH_SHORT).show();
            regPassword.setError("Password is Required");
            regPassword.requestFocus();
        }
        else {
            registerUsers(name, birthday, gender, phone, email, password, qrCode);
        }
    }

    private void progressBarOff(){
        progressBar.setVisibility(View.GONE);
    }
    //registerHelper but
    private void registerUsers(String name, String birthday, String gender, String phone, String email, String password, String Qrcode) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(passengerregistration.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    String UID = user.getUid();
                    //String currentUser = String.valueOf(auth.getCurrentUser());
                    RegistrationHelper registrationHelper = new RegistrationHelper(name,
                            birthday, gender, phone,email, Qrcode, UID);

                    reference = FirebaseDatabase.getInstance().getReference("Passenger");

                    reference.child(UID).setValue(registrationHelper).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //  String uid = firebaseUser.getUid();

                                //save the userdetails to UserClient class
                              /*  FirebaseUser currentUser = auth.getCurrentUser();
                                DatabaseReference userRef = database.getReference("Passenger")
                                        .child(UID);*/
                                reference.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            MyUtil myUtil = (MyUtil) getApplicationContext();
                                            //retrieve userdetails
                                            String name = snapshot.child("name").getValue(String.class);
                                            String email = snapshot.child("email").getValue(String.class);
                                            String uid = snapshot.child("uid").getValue(String.class);
                                            //create a user object with the retrieved user data
                                            User user = new User(email,uid,name);
                                            //pass the object to UserClient
                                            UserClient userClient = myUtil.getUserClient();
                                            userClient.setUser(user);
                                            //((UserClient)(getApplicationContext())).setUser(user);
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(passengerregistration.this, "Registration " + "Successful", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(passengerregistration.this, HomePageActivity.class);
                                            intent.putExtra("UID",uid);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        //handle cancel
                                    }
                                });


                            }


                        }
                    });


                } else {
                    try {
                        progressBar.setVisibility(View.GONE);
                        throw task.getException();
                    }
                    catch (FirebaseAuthWeakPasswordException e) {
                        progressBar.setVisibility(View.GONE);
                        regPassword.setError("Your password is too weak. Kindly use a " +
                                "mix of alphabets, numbers and special characters");
                        regPassword.requestFocus();
                    }
                    catch (FirebaseAuthUserCollisionException e) {
                        progressBar.setVisibility(View.GONE);
                        regEmail.setError("User is already registered with this email");
                        regEmail.requestFocus();
                    }
                    catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(passengerregistration.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }
    private void initWidget(){

        progressBar = findViewById(R.id.progressbar);
        regName = findViewById(R.id.name);
        regBirthday = findViewById(R.id.birthday);
        regGender = findViewById(R.id.gender);
        regPhone = findViewById(R.id.phone);
        regEmail = findViewById(R.id.email);
        regPassword = findViewById(R.id.password);
        regBtn = findViewById(R.id.register);

    }
}