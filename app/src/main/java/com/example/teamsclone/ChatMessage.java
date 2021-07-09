package com.example.teamsclone;

// import java.sql.Timestamp;
import java.util.Date;

public class ChatMessage {

    // Declaring the variables in the class
    String text, user;
    Date timestamp;

    // default constructor
    public ChatMessage(){

    }

    // constructor for initialising the class element with data
    public ChatMessage(String text, String user, Date timestamp) {
        this.text = text;
        this.user = user;
        this.timestamp = timestamp;
    }

    // Getter and Setter for the variables of the class

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
