package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class JoinRoomActivity extends AppCompatActivity {

    String TAG = "joining: ";

    // Declaring the variables required
    EditText roomID;
    EditText roomPassword;
    Button joinRoom;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        // Initialising the variables required
        roomID = findViewById(R.id.roomID);
        roomPassword = findViewById(R.id.roomPassword);
        joinRoom = findViewById(R.id.enter);

        fStore = FirebaseFirestore.getInstance();

        // Shifted from Jitsi to Agora
        /*
        URL serverURL;

        try{
            serverURL = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOptions =
                    new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setWelcomePageEnabled(false)
                            .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        // On Click Listener for Join Room Button(before adapt feature this was for directly joining the meeting)
        // when clicked checks if the entered password is correct
        // if password is correct the user to taken into the room
        joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Data entered
                String documentId = roomID.getText().toString();
                String mPassword = roomPassword.getText().toString();
                // Map<String, Object> meet = (Map<String, Object>)

                // Retrieving the data of the room from firestore using the data entered
                fStore.collection("rooms").document(documentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            // The room ID entered is valid
                            Map<String, Object> meet = documentSnapshot.getData();
                            String correctPassword = meet.get("password").toString();

                            // Checking if the password of the Room is correct
                            if(correctPassword.equals(mPassword)){
                                Intent intent = new Intent(JoinRoomActivity.this, RoomActivity.class);
                                intent.putExtra("ROOM_ID", documentId);
                                startActivity(intent);
                                // Shifted from Jitsi to Agora
                                /*
                                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                        .setRoom(documentId)
                                        .setWelcomePageEnabled(false)
                                        .build();
                                JitsiMeetActivity.launch(JoinMeeting.this, options);
                                 */
                            }
                            else{
                                // The password entered is incorrect
                                // Hence nothing is done
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Entered Room ID is invalid
                        // There is no record of the entered Room ID
                    }
                });
            }
        });

    }
}