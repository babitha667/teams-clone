package com.example.teamsclone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    // Declaring the variables in the class
    Context context;
    ArrayList<ChatMessage> messageArrayList;

    // Constructor which assigns data to the local variables
    public ChatAdapter(Context context, ArrayList<ChatMessage> messageArrayList) {
        this.context = context;
        this.messageArrayList = messageArrayList;
    }

    // Setting up the individual element layout with list_item_chat
    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.list_item_chat, parent, false);

        return new MyViewHolder(v);
    }

    // Setting up the list_item_chat UI with the current data
    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ChatAdapter.MyViewHolder holder, int position) {
        ChatMessage message = messageArrayList.get(position);

        if(message.user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            // If the message is sent by the current user
            // then the message should be in gray color and to the right of the screen
            holder.sentTextView.setVisibility(View.GONE);
            holder.receivedTextView.setText(message.text);
        }
        else if(message.user.length()>0){
            // If the message is sent by the other user
            // then the message should be in green color and to the left of the screen
            holder.sentTextView.setText(message.text);
            holder.receivedTextView.setVisibility(View.GONE);
        }
        else{
            // If the message is dummy message (default) which is added while creating the
            // sub-collection(messages into rooms collection)then the message should be invisible
            holder.sentTextView.setVisibility(View.GONE);
            holder.receivedTextView.setVisibility(View.GONE);
        }
    }

    // returns the number of messages in the current room
    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    // Initialising the view holder with components of the list_item_chat
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView sentTextView, receivedTextView;

        public MyViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            sentTextView = itemView.findViewById(R.id.textview_chat_sent);
            receivedTextView = itemView.findViewById(R.id.textview_chat_received);
        }
    }
}
