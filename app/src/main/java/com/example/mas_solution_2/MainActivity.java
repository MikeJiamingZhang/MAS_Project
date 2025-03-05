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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private Button addLocationButton; // click to add anther location to vote list
    private EditText msgInput; // input message
    private ListenerRegistration messageListener;
    private ListenerRegistration locationListener;
    private List<Message> messages = new ArrayList<Message>();
    private List<voteItem> locationList = new ArrayList<voteItem>();
    private RecyclerView chatView;
    private RecyclerView voteView;
    private chatAdapter adapter;
    private voteAdapter voteadapter;


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
        addLocationButton = findViewById(R.id.addLocation);
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

        //Click this to take what's in the input and add location
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msgInput.getText().toString();
                if(!text.equals("")){
                    addLocation("001", text);
                    msgInput.setText("");
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


        // set up the vote view screen
        voteView = findViewById(R.id.voteRecyclerView);
        voteadapter = new voteAdapter(locationList);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        voteView.setLayoutManager(layoutManager2);
        voteView.setItemAnimator(new DefaultItemAnimator());
        voteView.setAdapter(voteadapter);

        // calling to receive updates on votes
        receiveVoteLocations("001");
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
        final DocumentReference docRef = firestore.collection("chatHistory").document(room);
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
                }

            }
        });
    }

    // vote message on this location
    // reference https://stackoverflow.com/questions/51054114/firebase-cloud-firestore-query-whereequalto-for-reference
    public void vote(String room, String location){
        // does location exist?
        CollectionReference voteLocation = firestore.collection("chatHistory").document(room).collection("votes");
        voteLocation.whereEqualTo("location", location).get().addOnSuccessListener(querySnapshot -> {
            if(querySnapshot.size() != 0){
                DocumentReference ref = querySnapshot.getDocuments().get(0).getReference(); // there should only be 1
                ref.update("vote", FieldValue.increment(1));
            } else {
                Toast.makeText(getApplicationContext(), "Location doesn't exist yet", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Click the button to add a location to firefase if not in firestore already
    public void addLocation(String room, String location){
        CollectionReference voteLocation = firestore.collection("chatHistory").document(room).collection("votes");
        voteLocation.whereEqualTo("location", location).get().addOnSuccessListener(querySnapshot -> {
            if(querySnapshot.size() == 0){
                Map<String, Object> data = new HashMap<>();
                data.put("location", location);
                data.put("vote", 0);
                voteLocation.add(data);
            } else {
                Toast.makeText(getApplicationContext(), "Location exists, please vote!", Toast.LENGTH_LONG).show();
            }
        });
    }

    // retrieve change in locations, add to voting list
    public void receiveVoteLocations(String room){
        final DocumentReference docRef = firestore. collection("chatHistory").document(room);
        locationListener = docRef.collection("votes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(getApplication(), "FireStore Error", Toast.LENGTH_LONG).show();
                    return;
                }
                if(value != null){
                    locationList = new ArrayList<voteItem>();
                    for (QueryDocumentSnapshot doc : value) {
                        locationList.add(new voteItem(doc.getString("location"), Math.toIntExact(doc.getLong("vote"))));
                        voteadapter.updateData(locationList);
                    }
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