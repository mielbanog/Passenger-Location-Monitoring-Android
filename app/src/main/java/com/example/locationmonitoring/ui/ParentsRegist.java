package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.regParentsHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParentsRegist extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";
    TextView loginBtnLayout;
    AppCompatButton signupBtn;
    EditText passengerCode,uploadEmail,uploadName,uploadPass,uploadPhone;
    LinearLayout signupLayout;
    ProgressBar progressBar;
    regParentsHelper regparentshelper;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_regist);
        initWidget();
        loginBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ParentsRegist.this, Parents.class);
                startActivity(intent);
                finish();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpAdmin();
            }

        } );
    }



    public void signUpAdmin(){
        progressBar.setVisibility(View.VISIBLE);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Admin");

        String psngrCode, pass,name,email,phone,uniqID;
        psngrCode = String.valueOf(passengerCode.getText());
        pass = String.valueOf(uploadPass.getText());
        name = String.valueOf(uploadName.getText());
        email = String.valueOf(uploadEmail.getText());
        phone = String.valueOf(uploadPhone.getText());
        uniqID = databaseReference.push().getKey();

        String phoneReg = "^(09|\\+639|\\+6399|\\+63-9|\\+63-09)\\d{9}$";
        Matcher phoneMatcher;
        Pattern phonePattern = Pattern.compile(phoneReg);
        phoneMatcher = phonePattern.matcher(phone);

        if (TextUtils.isEmpty(phone)) {
            progressBarOff();
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            uploadPhone.setError("Phone Number is Required");
            uploadPhone.requestFocus();
        }
        else if (!phoneMatcher.find()) {
            progressBarOff();
            Toast.makeText(this, "Please re-enter your phone number", Toast.LENGTH_SHORT).show();
            uploadPhone.setError("Phone Number is not valid");
            uploadPhone.requestFocus();
        }
        else if (TextUtils.isEmpty(pass)){
            Toast.makeText(this,"Enter password", Toast.LENGTH_SHORT).show();
            uploadPass.setError("Password is Required");
            uploadPass.requestFocus();
        }
        else if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Enter email", Toast.LENGTH_SHORT).show();
            uploadEmail.setError("Email is Required");
            uploadEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(name)){
            Toast.makeText(this,"Enter name", Toast.LENGTH_SHORT).show();
            uploadName.setError("Name is Required");
            uploadName.requestFocus();
        }else {
            signUpResolver(name,pass,email,phone,uniqID,psngrCode);
        }

    }
    private void progressBarOff(){
        progressBar.setVisibility(View.GONE);
    }

    private  void signUpResolver( String name, String password, String email,String phone, String UniqId, String stdcode){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String UID = user.getUid();
                            regparentshelper = new regParentsHelper(name,password,email,phone,UniqId,stdcode,UID);

                            firebaseDatabase = FirebaseDatabase.getInstance();
                            databaseReference = firebaseDatabase.getReference("Parents");

                            databaseReference.child(UID).setValue(regparentshelper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBarOff();
                                    Toast.makeText(ParentsRegist.this, "Account created", Toast.LENGTH_SHORT).show();
                                    assert user != null;
                                    String uid = user.getUid();
                                    Intent intent = new Intent(ParentsRegist.this, ParentsPage.class);
                                    intent.putExtra("UID",uid);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            // If sign up fails, display a message to the user.
                            progressBarOff();
                            Toast.makeText(ParentsRegist.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                uploadPass.setError("Your password is too weak. Kindly use a " +
                                        "mix of alphabets, numbers and special characters");
                                uploadPass.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                uploadEmail.setError("User is already registered with this email");
                                uploadEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(ParentsRegist.this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }

    private void initWidget(){
        progressBar =findViewById(R.id.progressbarsignup);
        uploadEmail = findViewById(R.id.editEmail);
        uploadPass = findViewById(R.id.editPass);
        passengerCode = findViewById(R.id.passengerCode);
        uploadName = findViewById(R.id.editName);
        uploadPhone = findViewById(R.id.phone);
        loginBtnLayout=  findViewById(R.id.loginBtnlayout);
        signupBtn=  findViewById(R.id.signupbtn);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( ParentsRegist.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK |
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}