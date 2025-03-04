package com.example.mas_solution_2;

import com.google.firebase.Timestamp;

public class Message {
    String message;
    String sender;
    Timestamp time;

    public Message(String message, String sender, Timestamp time){
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }



}
