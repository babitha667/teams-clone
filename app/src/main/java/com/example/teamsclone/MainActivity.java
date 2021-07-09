package com.example.teamsclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    // Declaring the variables required
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    Button joinRoomBtn;
    Button createRoomBtn;
    ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialising the variables required
        joinRoomBtn = findViewById(R.id.joinRoom);
        createRoomBtn = findViewById(R.id.createRoom);
        profileIcon = findViewById(R.id.profileIcon);

        // Initialising variables with firebase authentication, firestore, storage instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Retrieving the profile picture from the storage
        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profilePicture.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("uriVal: ", uri+" in");
                if(uri==null){
                    // There is no profile picture in the storage. Hence default picture is displayed
                }
                else{
                    // There is profile picture in the storage and we have retrieved it successfully
                    // and loading it into the ImageView for profile picture
                    Picasso.get().load(uri).into(profileIcon);
                }
            }
        });

        // Couldn't implement the email verification for a new user
        /*

        fAuth = FirebaseAuth.getInstance();

        FirebaseUser user = fAuth.getCurrentUser();
        if(!user.isEmailVerified()){
            Toast.makeText(MainActivity.this, "Email Verification Not Completed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), EmailVerification.class));
        }

        */

        // On Click Listener for Join Room Button (before adapt feature I named it Join Meeting)
        // which when clicked takes the user to Join Meeting(Room) Activity
        joinRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JoinRoomActivity.class);
                startActivity(intent);
            }
        });

        // On Click Listener for Create Room Button(before adapt feature I named it Create Meeting)
        // Which when clicked takes the user to Create Meeting(Room) Activity
        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                startActivity(intent);
            }
        });

        // When profile picture is clicked we take the user to Profile Activity
        // In which user can edit and view their details
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Couldn't implement chat history feature due to time constraint
    public void chats (View view) {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}