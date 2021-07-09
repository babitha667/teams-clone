package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailVerification extends AppCompatActivity {
    public static final String TAG = "Email Verification?  ";

    FirebaseAuth fAuth;

    Button resendVerificationEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();


        if(fUser.isEmailVerified()){
            Toast.makeText(EmailVerification.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        resendVerificationEmail = findViewById(R.id.emailVerificationBtn);

        resendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EmailVerification.this, "Verification Email Has Been Sent!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to send the Email: "+ e.getMessage());
                    }
                });
            }
        });

    }
}