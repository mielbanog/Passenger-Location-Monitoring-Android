package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
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
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.model.regAdminHelper;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class admin extends AppCompatActivity {
    private static final String TAG = "CreateAccountActivity";
    AppCompatButton loginBtn,signupBtn;
    TextView signupBtnlayout,loginBtnLayout;
    LinearLayout loginLayout,signupLayout;

    EditText userAdm, passAdm, uploadUser,uploadEmail,uploadName,uploadPass;
    ProgressBar progressBar,progressBar2;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    regAdminHelper regadminhelper;


    public void onStart() {
        super.onStart();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(admin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getDisplayName();
            Log.d(TAG, "onComplete: USER " + uid);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Admin").child(uid);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();

                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);

                            if (user != null) {
                                MyUtil myUtil = (MyUtil) getApplicationContext();
                                UserClient userClient = myUtil.getUserClient();
                                userClient.setUser(user);

                                Log.d(TAG, "onComplete: successfully set the user client." + user);

                                dialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), adminpage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(admin.this, "User data is null. Please login again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(admin.this, "User does not exist in the 'Admin'. Please login again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(admin.this, "Unable to authenticate user. Please login", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        } else {
            dialog.dismiss();
        }
    }

    /*public  void onStart(){
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            Intent intent = new Intent(getApplicationContext(), adminpage.class);
            startActivity(intent);
            finish();
        }
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        init();

        try {
            signupBtnlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginLayout.setVisibility(View.GONE);
                    signupLayout.setVisibility(View.VISIBLE);
                }
            });

            loginBtnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signupLayout.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                }
            });


            signupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   signUpAdmin();
                }

            } );

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    signInAdmin();

                }

            } );
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    private void init(){

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar2 =findViewById(R.id.progressbarsignup);
        progressBar = findViewById(R.id.progressbar);
        loginBtn=  findViewById(R.id.loginbtnAdm);
        signupBtn=  findViewById(R.id.signupbtnAdm);
        signupBtnlayout=  findViewById(R.id.signbtnlayout);
        loginLayout=  findViewById(R.id.loginlayout);
        signupLayout=  findViewById(R.id.signuplayout);
        loginBtnLayout=  findViewById(R.id.loginBtnlayout);
        uploadEmail = findViewById(R.id.uploademailAdm);
        uploadPass = findViewById(R.id.uploadpassAdm);
        uploadUser = findViewById(R.id.uploaduserAdm);
        userAdm = findViewById(R.id.userAdm);
        passAdm = findViewById(R.id.passAdm);
        uploadName = findViewById(R.id.uploadnameAdm);

    }

    private void signInAdmin(){
        String user, pass;
            user = String.valueOf(userAdm.getText());
            pass = String.valueOf(passAdm.getText());
        if (TextUtils.isEmpty(user)){
            ProgressBarOff();
            Toast.makeText(admin.this,"Enter username", Toast.LENGTH_SHORT).show();
            userAdm.setError("Username is Required");
            userAdm.requestFocus();
        }
        else if (TextUtils.isEmpty(pass)){
            ProgressBarOff();
            Toast.makeText(admin.this,"Enter password", Toast.LENGTH_SHORT).show();
            passAdm.setError("Password is Required");
            passAdm.requestFocus();
        }
        else {
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Admin");
            if (user.contains("@gmail.com")){
                Query checkCred = databaseReference.orderByChild("email").equalTo(user);
                checkCred.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            userAdm.setError(null);
                            firebaseAuth.signInWithEmailAndPassword(user, pass)
                                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                // Sign up success, update UI with the signed-in user's information
                                                Toast.makeText(admin.this, "Authentication Success.",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(admin.this,adminpage.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                // If sign up fails, display a message to the user.
                                                Toast.makeText(admin.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }else {
                            progressBar.setVisibility(View.GONE);
                            userAdm.setError("No access");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ProgressBarOff();
                        Toast.makeText(admin.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Query checkAdminDB = databaseReference.orderByChild("username").equalTo(user);
                checkAdminDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            userAdm.setError(null);
                            String passfromDB = snapshot.child(user).child("password").getValue(String.class);
                            String namefromDB = snapshot.child(user).child("name").getValue(String.class);
                            assert passfromDB != null;
                            if(passfromDB.equals(pass)){
                                userAdm.setError(null);
                                progressBar.setVisibility(View.GONE);
                                // Sign up success, update UI with the signed-in user's information
                                Toast.makeText(admin.this, "Authentication Success.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(admin.this,adminpage.class);
                                   /* assert user != null;
                                    String uid = user.getUid();*/
                                intent.putExtra("name",namefromDB);
                                startActivity(intent);
                                finish();
                            }else {
                                ProgressBarOff();
                                Toast.makeText(admin.this,"Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            ProgressBarOff();
                            userAdm.setError("User does not exist");
                            userAdm.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(admin.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    private void ProgressBarOff(){
        progressBar.setVisibility(View.GONE);
    }
    public void signUpAdmin(){

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Admin");

        progressBar2.setVisibility(View.VISIBLE);
        String user, pass,name,email,uniqID;
        user = String.valueOf(uploadUser.getText());
        pass = String.valueOf(uploadPass.getText());
        name = String.valueOf(uploadName.getText());
        email = String.valueOf(uploadEmail.getText());
        uniqID = databaseReference.push().getKey();
        if (TextUtils.isEmpty(user)){
            Toast.makeText(admin.this,"Enter username", Toast.LENGTH_SHORT).show();
            uploadUser.setError("Username is Required");
            uploadUser.requestFocus();
        }
        else if (TextUtils.isEmpty(pass)){
            Toast.makeText(admin.this,"Enter password", Toast.LENGTH_SHORT).show();
            uploadPass.setError("Password is Required");
            uploadPass.requestFocus();
        }
        else if (TextUtils.isEmpty(email)){
            Toast.makeText(admin.this,"Enter email", Toast.LENGTH_SHORT).show();
            uploadEmail.setError("Email is Required");
            uploadEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(name)){
            Toast.makeText(admin.this,"Enter name", Toast.LENGTH_SHORT).show();
            uploadName.setError("Name is Required");
            uploadName.requestFocus();
        }else {
                signUpResolver(user,pass,name,email,uniqID);
        }

    }

    private  void signUpResolver(String username, String password, String name, String email, String UniqId){

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar2.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            regadminhelper = new regAdminHelper(name,username,password,email,UniqId);

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            firebaseDatabase = FirebaseDatabase.getInstance();
                            databaseReference = firebaseDatabase.getReference("Admin");
                            databaseReference.child(username).setValue(regadminhelper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar2.setVisibility(View.GONE);
                                    Toast.makeText(admin.this, "Account created", Toast.LENGTH_SHORT).show();
                                    assert user != null;
                                    String uid = user.getUid();
                                    Intent intent = new Intent(admin.this,adminpage.class);
                                    intent.putExtra("UID",uid);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            // If sign up fails, display a message to the user.
                            progressBar2.setVisibility(View.GONE);
                            Toast.makeText(admin.this, "Sign up failed",
                                    Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                passAdm.setError("Your password is too weak. Kindly use a " +
                                        "mix of alphabets, numbers and special characters");
                                passAdm.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                uploadEmail.setError("User is already registered with this email");
                                uploadEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(admin.this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( admin.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK |
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}