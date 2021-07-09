package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private static final int GALLERY_INTENT_CODE = 1023;

    // Declaring the variables required
    Button editName;
    TextView profileName;
    TextView emailAddress;
    ImageView profilePicture;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialising the variables required
        editName = findViewById(R.id.buttonEditName);
        profileName = findViewById(R.id.profileNameTextView);
        emailAddress = findViewById(R.id.textViewEmailAddress);
        profilePicture = findViewById(R.id.updateImageView);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Retrieving the current profile picture of the user.
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
                    Picasso.get().load(uri).into(profilePicture);
                }
            }
        });

        // Retrieving the user data from the firestore using user ID
        DocumentReference documentReference = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    // Displaying the retrieved user data
                    profileName.setText(documentSnapshot.getString("name"));
                    emailAddress.setText(documentSnapshot.getString("email"));

                }else {
                    // The current user data isn't present in the firestore
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery and select the profile picture
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });
    }

    // After getting the result from the activities this function is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000){
            // Once we get data from the requestCode: 1000 i.e selecting the profile picture
            // we upload the picture to the firebase storage
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri); // function for uploading the firebase storage
            }
        }
    }

    // This function uploads the data from activity with requestCode:1000 to the firebase storage
    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference fileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profilePicture.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // successfully uploaded the selected image to the firebase storage
                        // uploading the image to the local profile picture ImageView
                        Picasso.get().load(uri).into(profilePicture);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed to upload image hence not updating the local profile picture ImageView
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // On Click Listener for editing the user name which when clicked opens a dialog to submit new name
    public void buttonClickedEditName(View view) {

        // Creating the dialog for updating the name
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUsername = alertLayout.findViewById(R.id.et_username);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Name Edit");
        // this is to set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);


        // Cancel button inside the dialog
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Ok button when clicked updates the name in the firestore and also in the local TextView
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etUsername.getText().toString();
                profileName.setText(name);
                fStore.collection("users")
                        .document(fAuth.getCurrentUser().getUid())
                        .update("name", name);
                etUsername.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    // This function will takes the user to MainActivity
    public void done (View view) {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // when back pressed the current activity is finished(unlike the default onBackPressed)
    // and then taken to MainActivity
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // This function is for logout button which when clicked logout the current user
    // and takes the user to Login Screen Activity
    public void navigateLogOut (View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginScreenActivity.class);
        startActivity(intent);
        finish();
    }
}