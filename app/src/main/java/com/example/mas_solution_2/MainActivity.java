package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.mas_solution_2.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // the storage we will be using
    private Button sendMessageButton; // click to send message
    private EditText msgInput; // input message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // log in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // if user not signed in, then start logging in process
        if(user == null){
            Toast.makeText(getApplicationContext(), "Need to Log In", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, authentication.class);
            startActivity(intent);
        } // continue
        sendMessageButton = findViewById(R.id.sendMessage);
        msgInput = findViewById(R.id.msgInput);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msgInput.getText().toString();
                if(!text.equals("")){
                    sendMessage("001", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), text);
                    msgInput.setText("");
                }
            }
        });
    }

    // put the message and sender into firestore
    // chatHistory -> roomID -> messages -> sender, message, time
    public void sendMessage(String room, String sender, String message){
        Map<String, Object> data = new HashMap<>();
        data.put("sender", sender);
        data.put("message", message);
        data.put("time", FieldValue.serverTimestamp());
        firestore.collection("chatHistory").document(room).collection("messages").add(data);
    }

}