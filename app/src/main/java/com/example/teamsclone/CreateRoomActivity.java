package com.example.teamsclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateRoomActivity extends AppCompatActivity {

    String TAG = "creating the room: ";

    // Declaring the variables required
    EditText roomPassword;
    Button roomCreateBtn;
    TextView roomID;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        // Initialising the variables required
        roomPassword = findViewById(R.id.newRoomPassword);
        roomCreateBtn = findViewById(R.id.createNewRoom);
        roomID = findViewById(R.id.newRoomId);

        fStore = FirebaseFirestore.getInstance();

        // On click listener for create room button which when clicked
        // Create a new room in the firestore with the entered password
        roomCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = roomPassword.getText().toString();

                // The password length should be atleast 6 for security purposes
                // otherwise we return without creating the room
                if(password.length() < 6){
                    roomPassword.setError("Password should contain atleast 6 characters");
                    return;
                }

                // Adding a new room to the rooms collection with one field password
                Map<String,Object> meeting = new HashMap<>();
                meeting.put("password",password);

                String id = fStore.collection("rooms").document().getId();
                fStore.collection("rooms").document(id).set(meeting);


                // Adding a sub-collection messages for the new room created
                Map<String, Object> dummyMessage = new HashMap<>();
                dummyMessage.put("text", "");
                dummyMessage.put("user", "");
                dummyMessage.put("timestamp", FieldValue.serverTimestamp());

                fStore.collection("rooms")
                        .document(id)
                        .collection("messages")
                        .add(dummyMessage);
                roomID.setText("ID: "+id);

            }
        });

    }

    // This function is called when the user clicks share button which gives
    // multiple social media and texting application options to share the Room ID and the Room Password
    public void ShareClicked(View view)
    {
       /* ACTION_SEND: Deliver some data to someone else.
        createChooser (Intent target, CharSequence title): Here, target- The Intent that the user will be selecting an activity to perform.
            title- Optional title that will be displayed in the chooser.
        Intent.EXTRA_TEXT: A constant CharSequence that is associated with the Intent, used with ACTION_SEND to supply the literal data to be sent.
        */
        if(roomID.getText().length()==0){
            return;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,(roomID.getText().toString()+"\nPassword: "+roomPassword.getText().toString()));
        sendIntent.setType("text/plain");
        Intent.createChooser(sendIntent,"Share via");
        startActivity(sendIntent);
    }
}