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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Parents extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";
    AppCompatButton loginBtn;
    TextView signupBtnlayout;
    LinearLayout loginLayout;
    regAdminHelper regadminhelper;
    EditText username, password;
    ProgressBar progressBar;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    public void onStart() {
        super.onStart();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Parents.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading);
        AlertDialog dialog = builder.create();
        dialog.show();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d(TAG, "onComplete: USER" + uid);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Parents").child(uid);

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
                                Intent intent = new Intent(getApplicationContext(), ParentsPage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Parents.this, "User data is null. Please login again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(Parents.this, "User does not exist in the 'Parents'. Please login again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(Parents.this, "Unable to authenticate user. Please login", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        } else {
            dialog.dismiss();
        }
    }


    /* public  void onStart(){
        super.onStart();

        user = firebaseAuth.getCurrentUser();
       // Bundle bundle = getIntent().getExtras();
        //TODO Verify user

        if (user != null){
          //if current user is not null then proceed to parents page
            Intent intent = new Intent(getApplicationContext(), ParentsPage.class);
            startActivity(intent);
            finish();

        } else {
        }
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents);

        init();

        try {
            signupBtnlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Parents.this, ParentsRegist.class);
                    startActivity(intent);
                    finish();
                }
            });


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

        progressBar = findViewById(R.id.progressbar);
        loginBtn=  findViewById(R.id.loginbtn);
        signupBtnlayout=  findViewById(R.id.signbtnlayout);
        loginLayout=  findViewById(R.id.loginlayout);

        username = findViewById(R.id.username);
        password = findViewById(R.id.pass);

    }

    public void signInAdmin(){
        String user, pass;
        user = String.valueOf(username.getText());
        pass = String.valueOf(password.getText());
        if (TextUtils.isEmpty(user)){
            ProgressBarOff();
            Toast.makeText(this,"Enter username", Toast.LENGTH_SHORT).show();
            username.setError("Username is Required");
            username.requestFocus();
        }
        else if (TextUtils.isEmpty(pass)){
            ProgressBarOff();
            Toast.makeText(this,"Enter password", Toast.LENGTH_SHORT).show();
            password.setError("Password is Required");
            password.requestFocus();
        }
        else {
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Parents");
            //Query query = databaseReference.orderByChild("parentEmail").equalTo(user);
            if (user.contains("@gmail.com")){
                Query checkCred = databaseReference.orderByChild("parentEmail").equalTo(user);
                checkCred.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            username.setError(null);
                            firebaseAuth.signInWithEmailAndPassword(user, pass)
                                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                // Sign up success, update UI with the signed-in user's information
                                                Toast.makeText(Parents.this, "Authentication Success.",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Parents.this, ParentsPage.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                // If sign up fails, display a message to the user.
                                                Toast.makeText(Parents.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }else {
                            progressBar.setVisibility(View.GONE);
                            username.setError("No access");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ProgressBarOff();
                        Toast.makeText(Parents.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                DatabaseReference parentRef = databaseReference.child(String.valueOf(user));
                Query checkAdminDB = parentRef.orderByChild("parentPhone").equalTo(user);
                checkAdminDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            username.setError(null);
                            String passfromDB = snapshot.child(pass).child("parentPass").getValue(String.class);
                            String namefromDB = snapshot.child(user).child("parentPhone").getValue(String.class);
                            if(passfromDB!=null){
                                if(passfromDB.equals(pass)){
                                    username.setError(null);
                                    progressBar.setVisibility(View.GONE);
                                    // Sign up success, update UI with the signed-in user's information
                                    Toast.makeText(Parents.this, "Authentication Success.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Parents.this,ParentsPage.class);
                                   /* assert user != null;
                                    String uid = user.getUid();*/
                                    intent.putExtra("name",namefromDB);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    ProgressBarOff();
                                    Toast.makeText(Parents.this,"Invalid Credentials", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                ProgressBarOff();
                                Toast.makeText(Parents.this,"Authentication failed",Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            ProgressBarOff();
                            username.setError("User does not exist");
                            username.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ProgressBarOff();
                        Toast.makeText(Parents.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    private void ProgressBarOff(){
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent( Parents.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//        Intent.FLAG_ACTIVITY_CLEAR_TASK
//        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}