package com.example.teamsclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RoomActivity extends AppCompatActivity {
    String roomId;
    TextView roomIdTextView;
    Button enterVideoCall,enterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        roomId = getIntent().getStringExtra("ROOM_ID");

        roomIdTextView = findViewById(R.id.roomIdText);
        enterVideoCall = findViewById(R.id.enterVideoChat);
        enterChat = findViewById(R.id.enterChat);

        roomIdTextView.setText("Room ID: "+roomId);
        enterVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this, ConversationActivity.class);
                intent.putExtra("ROOM_ID", roomId);
                startActivity(intent);
            }
        });

        enterChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
                intent.putExtra("ROOM_ID", roomId);
                startActivity(intent);
            }
        });

    }
}