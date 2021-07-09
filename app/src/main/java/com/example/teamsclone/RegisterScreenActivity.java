package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreenActivity extends AppCompatActivity {
    public static final String TAG = "USER CREATED? ";

    // Declaring the variables required
    EditText mName, mEmail, mPassword;
    Button mRegisterBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        // Initialising the variables declared
        mName = findViewById(R.id.setName);
        mEmail = findViewById(R.id.setEmail);
        mPassword = findViewById(R.id.setPassword);
        mRegisterBtn = findViewById(R.id.register);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // On Click Listener for registration button when clicked registers the user
        // and adds the details of the user to the firestore and firebase authentication
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String name = mName.getText().toString();
                String password = mPassword.getText().toString().trim();

                // email address cannot be empty
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }

                // email address should be valid if not we return and don't register the user
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Invalid Email Address");
                    return;
                }

                // password cannot be empty
                if(TextUtils.isEmpty(password)){
                    mEmail.setError("Password is Required");
                    return;
                }

                // Here for security purpose we have set a constraint
                // that length of the password should be atleast 6
                if(password.length() < 6){
                    mPassword.setError("Password should contain atleast 6 characters");
                    return;
                }

                // creating the user in firebase authentication with entered email and password values
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            // couldn't implement the email verification while registration
                            // sending the verification link

                            /* FirebaseUser fUser = fAuth.getCurrentUser();
                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterScreenActivity.this, "Verification Email Has Been Sent!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Failed to send the Email: "+ e.getMessage());
                                }
                            }); */

                            Toast.makeText(RegisterScreenActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("email",email);

                            // adding user details(name and email) object to the firestore 'users' collection
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Successfully added the user data to firestore
                                    Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Couldn't add the user data to firestore due to internal issue
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            mName.setText("");
                            mEmail.setText("");
                            mPassword.setText("");

                            // This is to make user to login through login page even after registration
                            FirebaseAuth.getInstance().signOut();

                            // Displaying the login page
                            startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                            finish();
                        }
                        else{
                            // Registration failed due to internal issues
                            Toast.makeText(RegisterScreenActivity.this, "Error! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}