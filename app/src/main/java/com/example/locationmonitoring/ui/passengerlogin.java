package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.example.locationmonitoring.model.User;
import com.example.locationmonitoring.util.MyUtil;
import com.example.locationmonitoring.util.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class passengerlogin extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    EditText logEmail, logPassword;
    Button logBtn;
    private TextView createbtn, forgotBtn;
     FirebaseAuth authProfile;
     ProgressBar progressBar;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    public void onStart() {
        super.onStart();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(passengerlogin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();

        FirebaseUser currentUser = authProfile.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d(TAG, "onComplete: USER" + uid);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Passenger").child(uid);

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
                                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(passengerlogin.this, "User data is null. Please login again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(passengerlogin.this, "User does not exist in the 'Passenger'. Please login again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(passengerlogin.this, "Unable to authenticate user. Please login", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        } else {
            dialog.dismiss();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengerlogin);
        initWidget();

        //setupFirebaseAuth();
        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                loginPassenger();
            }
        });

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(passengerlogin.this, ForgotPasswordAct.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        createbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String userUsername = logEmail.getText().toString();
                String userPassword = logPassword.getText().toString();

                Intent intent = new Intent(passengerlogin.this, passengerregistration.class);
                startActivity(intent);
            }
        });
    }

    private void loginPassenger(){

        String userEmail = logEmail.getText().toString();
        String userPassword = logPassword.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(passengerlogin.this, "Please enter your email",
                    Toast.LENGTH_SHORT).show();
            logEmail.setError("Email is required");
            logEmail.requestFocus();
        } else if (TextUtils.isEmpty(userPassword)) {
            Toast.makeText(passengerlogin.this, "Please enter your password",
                    Toast.LENGTH_SHORT).show();
            logPassword.setError("Password is required");
            logPassword.requestFocus();
        } else {
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Passenger");
            if (userEmail.contains("@gmail.com")){
                Query checkCred = databaseReference.orderByChild("email").equalTo(userEmail);
                checkCred.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            logEmail.setError(null);
                            authProfile.signInWithEmailAndPassword(userEmail, userPassword)
                                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                //TODO verification if current login is a passenger

                                                //save the userdetails to UserClient class
                                                FirebaseUser currentUser = authProfile.getCurrentUser();
                                                String uid = currentUser.getUid();
                                                DatabaseReference userRef = firebaseDatabase.getReference("Passenger")
                                                        .child(uid);
                                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            //retrieve userdetails
                                                            String name = snapshot.child("name").getValue(String.class);
                                                            String email = snapshot.child("email").getValue(String.class);
                                                            String uid = snapshot.child("uid").getValue(String.class);
                                                            //create a user object with the retrieved user data
                                                            User user = new User(email,uid,name);

                                                            MyUtil myUtil = (MyUtil) getApplication();
                                                            //pass the object to UserClient
                                                            UserClient userClient = myUtil.getUserClient();
                                                            userClient.setUser(user);
                                                            //((UserClient)(getApplicationContext())).setUser(user);
                                                            progressBar.setVisibility(View.GONE);

                                                            // Sign up success, update UI with the signed-in user's information
                                                            Toast.makeText(passengerlogin.this, "Authentication Success.",
                                                                    Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(passengerlogin.this,
                                                                    HomePageActivity.class);
                                                            intent.putExtra("UID",currentUser);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else {

                                                            Toast.makeText(passengerlogin.this,"Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        //handle cancel
                                                    }
                                                });
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                // If sign up fails, display a message to the user.
                                                Toast.makeText(passengerlogin.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }else {
                            progressBar.setVisibility(View.GONE);
                            logEmail.setError("No access");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(),"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Query checkPassengerDB = databaseReference.orderByChild("username").equalTo(userEmail);
                checkPassengerDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            logEmail.setError(null);
                            String passfromDB = snapshot.child(userEmail).child("password").getValue(String.class);
                            String namefromDB = snapshot.child(userEmail).child("name").getValue(String.class);
                            assert passfromDB != null;
                            if(passfromDB.equals(userPassword)){
                                logEmail.setError(null);
                                progressBar.setVisibility(View.GONE);
                                // Sign up success, update UI with the signed-in user's information
                                Toast.makeText(getApplicationContext(), "Authentication Success.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),HomePageActivity.class);
                                   /* assert user != null;
                                    String uid = user.getUid();*/
                                intent.putExtra("name",namefromDB);
                                startActivity(intent);
                                finish();
                            }else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            progressBar.setVisibility(View.GONE);
                            logEmail.setError("User does not exist");
                            logEmail.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(getApplicationContext(),"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    //----------------------------- Firebase setup ---------------------------------
    /*private void setupFirebaseAuth(){

        Log.d(TAG, "setupFirebaseAuth: started.");

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(passengerlogin.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                    FirebaseDatabase db = FirebaseDatabase.getInstance();

                    DatabaseReference userRef = db.getReference("Passenger")
                            .child(user.getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: successfully set the user client.");
                                User user = task.getResult().getValue(User.class);
                                ((UserClient)(getApplicationContext())).setUser(user);
                            }
                        }
                    });

                    Intent intent = new Intent(passengerlogin.this, HomePageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }
*/

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
          //  FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(passengerlogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(passengerlogin.this,
                            HomePageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(passengerlogin.this, "Something went wrong",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
        }
    private  void initWidget(){

        logEmail = findViewById(R.id.email_login);
        logPassword = findViewById(R.id.password_login);
        logBtn = findViewById(R.id.login_btn);
        createbtn = findViewById(R.id.createbtn);
        progressBar = findViewById(R.id.loginProgressbar);
        authProfile = FirebaseAuth.getInstance();
        forgotBtn =findViewById(R.id.forgotBtn);

    }
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(passengerlogin.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*public  void onStart(){
        super.onStart();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(passengerlogin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();
       // FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if (currentUser != null){
            //TODO verification if current login is a passenger
            String uid = currentUser.getUid();

            Log.d(TAG, "onComplete: USER"+ uid);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Passenger")
                    .child(uid);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        User user = task.getResult().getValue(User.class);
                        MyUtil myUtil = (MyUtil) getApplicationContext();
                        UserClient userClient = myUtil.getUserClient();
                        userClient.setUser(user);

                        Log.d(TAG, "onComplete: successfully set the user client."+ user);
                        //((UserClient)(getApplicationContext())).setUser(user);
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(passengerlogin.this,"Unable to authenticate user Please login",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

        }
        else {
            dialog.dismiss();
        }
    }*/

}