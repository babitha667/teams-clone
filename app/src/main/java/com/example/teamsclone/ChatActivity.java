package com.example.teamsclone;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    // Declaring the variables required
    RecyclerView listChat;
    String roomId;
    ArrayList<ChatMessage> messageArrayList;
    ChatAdapter chatAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    EditText sendingMessage;
    ImageView sendMessageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialising the variables required
        sendingMessage = findViewById(R.id.edittext_chat);
        sendMessageBtn = findViewById(R.id.button_send);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        // Initialising the RecyclerView
        listChat = findViewById(R.id.list_chat);
        listChat.setHasFixedSize(true);
        listChat.setLayoutManager(new LinearLayoutManager(this));
        // fixing the first message so that the messages don't disappear
        listChat.getRecycledViewPool().setMaxRecycledViews(0, 0);

        db = FirebaseFirestore.getInstance();
        roomId = getIntent().getStringExtra("ROOM_ID");
        // Initialising the array for storing messages locally
        messageArrayList = new ArrayList<ChatMessage>();
        // Initialising chat adapter
        chatAdapter = new ChatAdapter(ChatActivity.this, messageArrayList);

        // Setting up the chat adapter to the RecyclerView
        listChat.setAdapter(chatAdapter);

        // Fetching the data from firestore
        EventChangeListener();

        // On click listener for send message button which when clicked adds new message to the firestore
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessageText = sendingMessage.getText().toString();
                Map<String, Object> data = new HashMap<>();
                data.put("text", newMessageText);
                data.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                data.put("timestamp", FieldValue.serverTimestamp());
                db.collection("rooms").document(roomId).collection("messages").add(data);
                sendingMessage.setText("");
            }
        });

        /*
        Query query = FirebaseFirestore.getInstance()
                .collection("rooms")
                .document(roomId)
                .collection("messages")
                .orderBy("timestamp");

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();*/
    }

    // method for fetching the messages in the current room
    // and also updating the screen with latest messages
    private void EventChangeListener() {
        db.collection("rooms")
                .document(roomId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value,
                                        @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            return;
                        }

                        // Iterating on the list of messages(documents).
                        // if new message found we add it to the local array and notify adapter
                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType()==DocumentChange.Type.ADDED){
                                messageArrayList.add(dc.getDocument().toObject(ChatMessage.class));
                            }

                            // notifying adaptor
                            chatAdapter.notifyDataSetChanged();
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }
}