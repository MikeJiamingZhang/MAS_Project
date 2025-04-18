package com.example.watch_gatherers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupHangoutsActivity extends Activity {

    private static final String TAG = "GroupHangoutsActivity";
    private String groupId;
    private String groupName;

    private TextView titleView;
    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private List<Hangout> hangouts = new ArrayList<>();
    private HangoutAdapter adapter;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_hangouts);

        // Get extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupId = extras.getString("GROUP_ID", "");
            groupName = extras.getString("GROUP_NAME", "");
        }

        if (groupId.isEmpty()) {
            Toast.makeText(this, "Group ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        titleView = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);

        // Set title
        titleView.setText(groupName);

        // Set up recycler view
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setBezelFraction(0.5f);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set up adapter
        adapter = new HangoutAdapter(this, hangouts);
        recyclerView.setAdapter(adapter);

        // Load hangouts
        loadHangouts();
    }

    private void loadHangouts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Get all hangouts for this group without date filtering
        firestore.collection("hangouts")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    hangouts.clear();
                    Date now = new Date();

                    // Filter and sort in your code
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Hangout hangout = document.toObject(Hangout.class);
                        hangout.setId(document.getId());

                        // Only add upcoming hangouts
                        if (hangout.getDate() != null && hangout.getDate().toDate().after(now)) {
                            hangouts.add(hangout);
                        }
                    }

                    // Sort by date
                    hangouts.sort((h1, h2) -> h1.getDate().compareTo(h2.getDate()));

                    adapter.notifyDataSetChanged();

                    if (hangouts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setText("No upcoming hangouts for this group");
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