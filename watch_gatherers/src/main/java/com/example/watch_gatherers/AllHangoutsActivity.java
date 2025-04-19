package com.example.watch_gatherers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllHangoutsActivity extends Activity {

    private static final String TAG = "AllHangoutsActivity";
    // Hardcoded user ID from your account
    private static final String HARDCODED_USER_ID = "RWmRbh838Tf1u0u8BsatsqFj1m02";

    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView titleView;
    private List<Hangout> hangouts = new ArrayList<>();
    private HangoutAdapter adapter;
    private FirebaseFirestore firestore;
    private Map<String, String> groupIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_hangouts); // Reuse layout

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        titleView = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);

        // Set title
        titleView.setText("All Upcoming Hangouts");

        // Set up recycler view
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setBezelFraction(0.5f);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set up adapter with showGroupName=true
        adapter = new HangoutAdapter(this, hangouts, true);
        recyclerView.setAdapter(adapter);

        // Load group names first, then hangouts
        loadGroupNames();
    }

    private void loadGroupNames() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Get groups for the hardcoded user
        firestore.collection("groups")
                .whereArrayContains("members", HARDCODED_USER_ID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String groupId = document.getId();
                        String groupName = document.getString("name");
                        if (groupName != null) {
                            groupIdToName.put(groupId, groupName);
                        }
                    }

                    // Now load hangouts with group names
                    loadAllHangouts();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading group names", e);
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText("Error loading groups: " + e.getMessage());
                    emptyView.setVisibility(View.VISIBLE);
                });
    }

    private void loadAllHangouts() {
        // First, just get all hangouts for these groups without date filtering
        firestore.collection("hangouts")
                .whereIn("groupId", new ArrayList<>(groupIdToName.keySet()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    hangouts.clear();
                    Date now = new Date();

                    // Filter and sort in your code instead of in the query
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Hangout hangout = document.toObject(Hangout.class);
                        hangout.setId(document.getId());

                        // Only add upcoming hangouts
                        if (hangout.getDate() != null && hangout.getDate().toDate().after(now)) {
                            hangouts.add(hangout);
                        }
                    }

                    // Sort by date manually
                    hangouts.sort((h1, h2) -> h1.getDate().compareTo(h2.getDate()));

                    // Pass the group name mapping to the adapter
                    ((HangoutAdapter)adapter).setGroupIdToName(groupIdToName);
                    adapter.notifyDataSetChanged();

                    if (hangouts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setText("No upcoming hangouts");
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading hangouts", e);
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText("Error loading hangouts: " + e.getMessage());
                    emptyView.setVisibility(View.VISIBLE);
                });
    }
}