package com.example.locationmonitoring.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locationmonitoring.MainActivity;
import com.example.locationmonitoring.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordAct extends AppCompatActivity {

    Button sendBtn;
    EditText emailedit;
    TextView lblText;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        init();
        firebaseAuth = FirebaseAuth.getInstance();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordAct.this);
                builder.setCancelable(false);
                builder.setView(R.layout.loading);
                AlertDialog dialog = builder.create();
                dialog.show();
                int accentColor = ContextCompat.getColor(ForgotPasswordAct.this, R.color.button_default_color);

                resetPassword();
                lblText.setText(R.string.lblemailsent);
                lblText.setTextColor(accentColor);
                emailedit.setVisibility(View.GONE);
                sendBtn.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });
    }
    private void init(){
        sendBtn = findViewById(R.id.btnsendForgot);
        emailedit = findViewById(R.id.emailForgot);
        lblText = findViewById(R.id.lbltxt);
    }
    public void resetPassword() {
        String email = emailedit.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordAct.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordAct.this, "Failed to send reset email. Check your email address", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ForgotPasswordAct.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
        Intent.FLAG_ACTIVITY_CLEAR_TASK |
        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}