package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mas_solution_2.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    private ListenerRegistration messageListener;
    private List<Message> messages = new ArrayList<Message>();
    private RecyclerView chatView;
    private chatAdapter adapter;


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


        // Click the button, send the text in the text field
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msgInput.getText().toString();
                if(!text.equals("")){
                    sendMessage("001", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), text);
                    msgInput.setText("");
                    //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set up the recycler view screen
        chatView = findViewById(R.id.recyclerView);
        adapter = new chatAdapter(messages);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        chatView.setLayoutManager(layoutManager);
        chatView.setItemAnimator(new DefaultItemAnimator());
        chatView.setAdapter(adapter);

        // Calling to receive messages
        receiveMessage("001");
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

    // Receive message once the firestore is changed (including yourself)
    // Reference: https://firebase.google.com/docs/firestore/query-data/listen (Listen to multiple documents in a collection)
    public void receiveMessage(String room){
        final DocumentReference docRef = firestore. collection("chatHistory").document(room);
        messageListener = docRef.collection("messages").orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(getApplication(), "FireStore Error", Toast.LENGTH_LONG).show();
                    return;
                }
                if(value != null){
                    messages = new ArrayList<Message>();
                    for (QueryDocumentSnapshot doc : value) {
                        messages.add(new Message(doc.getString("message"), doc.getString("sender"), doc.getTimestamp("time")));
                    }
                    // Change the screen
                    adapter.changedData(messages);

                    // Scroll to the bottom
                    chatView.scrollToPosition(messages.size() - 1);

                    // print out for testing
                    String msg = messages.get(messages.size() - 1).getMessage();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        if (messageListener != null){
            messageListener.remove();
        }
        super.onDestroy();
    }
}