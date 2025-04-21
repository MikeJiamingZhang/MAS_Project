package com.example.mas_solution_2;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
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

import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.*;

// Firestore structure
/*
    chatHistory
        /Room Name/
            messages
                message
                    message
                    sender
                    time
            photos (not implemented)
            votes
                vote
                    location
                    vote
                    voters (arraylist)
 */
public class MainActivity extends AppCompatActivity implements voteAdapter.voteListener{

    private Toolbar toolbar;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // the storage we will be using
    private ImageButton sendMessageButton; // click to send message
    private ImageButton addLocationButton; // click to add another location to vote list
    private EditText msgInput; // input message
    private ListenerRegistration messageListener;
    private ListenerRegistration locationListener;
    private List<Message> messages = new ArrayList<Message>();
    private List<voteItem> locationList = new ArrayList<voteItem>();
    private RecyclerView chatView;
    private RecyclerView voteView;
    private chatAdapter adapter;
    private voteAdapter voteadapter;
    private String hangoutId;
    private String groupId;
    private String roomId = "001"; // Default room
    private String groupName;
    private boolean sendingLocation = false;
    private String me = FirebaseAuth.getInstance().getUid();


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
            finish();
            return;
        }

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Group Chat");

        // Get hangout ID from intent if available
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hangoutId = extras.getString("HANGOUT_ID", "");
            groupId = extras.getString("GROUP_ID", "");
            groupName = extras.getString("GROUP_NAME", "");

            if (!hangoutId.isEmpty()) {
                roomId = hangoutId; // Use hangout ID as room ID
                if (!groupName.isEmpty()) {
                    getSupportActionBar().setTitle(groupName);
                } else {
                    loadHangoutDetails();
                }
            } else if (!groupId.isEmpty()) {
                roomId = groupId; // Use group ID as room ID
                isMyRoomAllVoted(roomId);
                if (!groupName.isEmpty()) {
                    getSupportActionBar().setTitle(groupName);
                } else {
                    loadGroupDetails();
                }
            }
        }

        sendMessageButton = findViewById(R.id.sendMessage);
        addLocationButton = findViewById(R.id.addLocation);
        msgInput = findViewById(R.id.msgInput);

        // Click the button, send the text in the text field
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msgInput.getText().toString();
                if(!sendingLocation){
                    if(!text.equals("")){
                        sendMessage(roomId, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), text);
                        msgInput.setText("");
                    }
                }
                else{
                    if(!text.equals("")){
                        addLocation(roomId, text);
                        msgInput.setText("");
                    }
                }
            }
        });

        //Click this to take what's in the input and add location
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendingLocation){
                    sendingLocation = false;
                    msgInput.setHint("Send a Message");
                    Toast.makeText(getApplicationContext(), "Sending Messages via Input", Toast.LENGTH_SHORT).show();
                } else{
                    sendingLocation = true;
                    msgInput.setHint("Add a Location");
                    Toast.makeText(getApplicationContext(), "Adding Locations via Input", Toast.LENGTH_SHORT).show();
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
        receiveMessage(roomId);

        // set up the vote view screen
        voteView = findViewById(R.id.voteRecyclerView);
        voteadapter = new voteAdapter(locationList, this);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        voteView.setLayoutManager(layoutManager2);
        voteView.setItemAnimator(new DefaultItemAnimator());
        voteView.setAdapter(voteadapter);

        // calling to receive updates on votes
        receiveVoteLocations(roomId);
    }

    private void loadHangoutDetails() {
        if (hangoutId == null || hangoutId.isEmpty()) return;

        // Load hangout details to set the toolbar title
        firestore.collection("hangouts").document(hangoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hangoutName = documentSnapshot.getString("name");
                        if (hangoutName != null && !hangoutName.isEmpty()) {
                            getSupportActionBar().setTitle(hangoutName + " Chat");
                        }
                    }
                });
    }

    private void loadGroupDetails() {
        if (groupId == null || groupId.isEmpty()) return;

        // Load group details to set the toolbar title
        firestore.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String groupName = documentSnapshot.getString("name");
                        if (groupName != null && !groupName.isEmpty()) {
                            getSupportActionBar().setTitle(groupName + " Chat");
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
        String uid = FirebaseAuth.getInstance().getUid();
        // does location exist?
        CollectionReference voteLocation = firestore.collection("chatHistory").document(room).collection("votes");
        voteLocation.whereEqualTo("location", location).get().addOnSuccessListener(querySnapshot -> {
            if(querySnapshot.size() != 0){
                DocumentReference ref = querySnapshot.getDocuments().get(0).getReference(); // there should only be 1
                ref.get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        List<String> voters = (List<String>) documentSnapshot.get("voters");
                        if (voters != null && voters.contains(uid)) {
                            ref.update(
                                    "vote", FieldValue.increment(-1),
                                    "voters", FieldValue.arrayRemove(uid)
                            ).addOnSuccessListener(aVoid ->
                                    Toast.makeText(getApplicationContext(), "Retracted!", Toast.LENGTH_SHORT).show()
                            ).addOnFailureListener(e ->
                                    Toast.makeText(getApplicationContext(), "Retract failed!", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            ref.update(
                                    "vote", FieldValue.increment(1),
                                    "voters", FieldValue.arrayUnion(uid)
                            ).addOnSuccessListener(aVoid ->
                                    Toast.makeText(getApplicationContext(), "Vote counted!", Toast.LENGTH_SHORT).show()
                            ).addOnFailureListener(e ->
                                    Toast.makeText(getApplicationContext(), "Vote failed!", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Location doesn't exist yet", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Remove location from database
    public void removeLocation(String room, String location) {
        CollectionReference voteLocation = firestore.collection("chatHistory").document(room).collection("votes");
        voteLocation.whereEqualTo("location", location).get().addOnSuccessListener(querySnapshot -> {
            if(querySnapshot.size() != 0) {
                DocumentReference ref = querySnapshot.getDocuments().get(0).getReference(); // there should only be 1
                ref.delete()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getApplicationContext(), "Location removed!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(), "Failed to remove location!", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
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
                data.put("voters", new ArrayList<String>());
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
                    }
                    // sort to include the higehst vote
                    // reference https://stackoverflow.com/questions/4066538/sort-an-arraylist-based-on-an-object-field
                    Collections.sort(locationList, new Comparator<voteItem>(){
                        public int compare(voteItem o1, voteItem o2){
                            if(o1.vote == o2.vote)
                                return 0;
                            return o1.vote < o2.vote ? 1 : -1;
                        }
                    });
                    voteadapter.updateData(locationList);
                }
            }
        });
    }


    // unregister listeners
    @Override
    protected void onDestroy() {
        if (messageListener != null){
            messageListener.remove();
        }
        if(locationListener != null){
            locationListener.remove();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(String locName) {
        vote(roomId, locName);
    }

    @Override
    public void onRemove(String locName) {
        removeLocation(roomId, locName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // When back button is pressed in chat, go to appropriate screen based on where we came from
            if (hangoutId != null && !hangoutId.isEmpty()) {
                // If we came from a hangout, go back to hangout detail
                Intent intent = new Intent(this, HangoutDetailActivity.class);
                intent.putExtra("HANGOUT_ID", hangoutId);
                startActivity(intent);
            } else {
                // Otherwise go to groups activity
                startActivity(new Intent(this, GroupsActivity.class));
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Override back button behavior
        super.onBackPressed();
        if (hangoutId != null && !hangoutId.isEmpty()) {
            // If we came from a hangout, go back to hangout detail
            Intent intent = new Intent(this, HangoutDetailActivity.class);
            intent.putExtra("HANGOUT_ID", hangoutId);
            startActivity(intent);
        } else {
            // Otherwise go to groups activity
            startActivity(new Intent(this, GroupsActivity.class));
        }
        finish();
    }

    public void isMyRoomAllVoted(String roomId){
        firestore.collection("groups").document(groupId).get().addOnSuccessListener(groupDoc -> {
            List<String> members = (List<String>) groupDoc.get("members");
            String owner = groupDoc.getString("createdBy");
            if (members == null || owner == null || !me.equals(owner)) return; // return if they don't exist or I am not the owner
            // edit hangouts
            firestore.collection("hangouts").whereEqualTo("groupID", groupId).whereEqualTo("confirmed",true).get().addOnSuccessListener(existing->{
                if (!existing.isEmpty()) return; // The group already has a hangout planned
                firestore.collection("chatHistory").document(roomId).collection("votes").get().addOnSuccessListener(voteDocs -> {
                            Set<String> allVoters = new HashSet<>();
                            for (QueryDocumentSnapshot doc : voteDocs) {
                                List<String> voters = (List<String>) doc.get("voters");
                                if (voters != null) allVoters.addAll(voters);
                            }
                            if (allVoters.containsAll(members)) {
                                showPopup(voteDocs);
                            }
                        });
            });
        });
    }

    public void showPopup(QuerySnapshot voteDocs){
        // Add vote counts. Else, prompt the user to select locations (which should not happen).
        Map<String, Integer> voteCounts = new HashMap<>();
        for (QueryDocumentSnapshot doc : voteDocs) {
            String location = doc.getString("location");
            Long votes = doc.getLong("vote");
            if (location != null && votes != null) {
                voteCounts.put(location, votes.intValue());
            }
            else{
                Toast.makeText(getApplicationContext(), "Please select/vote a location!", Toast.LENGTH_LONG).show();
            }
        }
        if (voteCounts.isEmpty()) return;
        String topLocation = Collections.max(voteCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
        // Use popup to get name and dates
        View dialogView = getLayoutInflater().inflate(R.layout.activity_hangoutpopup, null);
        EditText nameInput = dialogView.findViewById(R.id.hangout_name);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
        TextView setHangoutTime = dialogView.findViewById(R.id.editTextTime);
        Calendar calendar = Calendar.getInstance();
        setHangoutTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                        setHangoutTime.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );
            timePickerDialog.show();
        });
        // Finally show the dialog
        new AlertDialog.Builder(this).setTitle("Create Hangout!").setMessage("Everyone has voted! Let's finalize your hangout at: " + topLocation).setView(dialogView).setPositiveButton("Create", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            //Calendar calendar = Calendar.getInstance();
            //String hourAndMinute = setHangoutTime.getText();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            createFinalizedHangout(name, topLocation, calendar.getTime()); // Actually create the entry
        }).setNegativeButton("Cancel", null).show();
    }

    // create the hangout and add to firestore, just like before
    public void createFinalizedHangout(String name, String location, Date date) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("location", location);
        data.put("date", new Timestamp(date));
        data.put("groupId", groupId);
        data.put("createdBy", FirebaseAuth.getInstance().getUid());
        data.put("confirmed", true);
        firestore.collection("groups").document(groupId).get().addOnSuccessListener(groupDoc -> {
            List<String> members = (List<String>) groupDoc.get("members");
            if (members != null) {
                data.put("participants", members);
            }
            firestore.collection("hangouts").add(data).addOnSuccessListener(docRef -> {Toast.makeText(this, "Hangout created!", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Hangout created!", Toast.LENGTH_SHORT).show();
                firestore.collection("chatHistory").document(roomId).collection("votes").get().addOnSuccessListener(voteDocs -> { // delte locations after hangout created
                    for (QueryDocumentSnapshot doc : voteDocs) {
                        doc.getReference().delete();
                    }
                });
                sendMessage(roomId, "Admin", "Hangout Created!");
            });
        });
    }
}