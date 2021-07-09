package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreenActivity extends AppCompatActivity {

    // Declaring the required variables
    FirebaseAuth fAuth;
    EditText mEmail, mPassword;
    Button mLoginBtn, mForgotPasswordBtn, mRegistrationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // initialising the variables required
        fAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.login);
        mForgotPasswordBtn = findViewById(R.id.forgotPassword);
        mRegistrationBtn = findViewById(R.id.newRegistration);

        // if the user already logged in then we go to MainActivity
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // On Click Listener for Login Button which makes the user login and go to MainActivity
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                // email address cannot be empty
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }

                // password cannot be empty
                if(TextUtils.isEmpty(password)){
                    mEmail.setError("Password is Required");
                    return;
                }

                // authenticating the user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // logged in Successfully!
                            mEmail.setText("");
                            mPassword.setText("");
                            Toast.makeText(LoginScreenActivity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                        else{
                            // Login isn't successful
                            Toast.makeText(LoginScreenActivity.this, "Error! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /* On Click Listener for Forgot Password Button which provides resetting
         the password by sending a reset password mail */
        mForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // An EditText Dialog is displayed.
                // When the user enter the email and click 'yes', we send an reset password email to the user
                // if user click 'cancel', we just remove the EditText Dialog

                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // successfully sent the reset password email
                                Toast.makeText(LoginScreenActivity.this, "Reset Link sent!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error sending the reset password email
                                Toast.makeText(LoginScreenActivity.this, "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                // We have created the dialog. This line will show the dialog
                passwordResetDialog.create().show();

            }
        });

        // On Click Listener for "Go to Registration Page" Button which redirects the user to to registration activity when clicked
        mRegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmail.setText("");
                mPassword.setText("");
                Intent intent = new Intent(LoginScreenActivity.this, RegisterScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}