package com.example.watch_gatherers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements GroupAdapter.GroupClickListener {

    private static final String TAG = "MainActivity";
    // Hardcoded user ID from your account
    private static final String HARDCODED_USER_ID = "RWmRbh838Tf1u0u8BsatsqFj1m02";

    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private Button allHangoutsButton;
    private List<Group> groups = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Log screen view event
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Main Groups Screen");
        params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        allHangoutsButton = findViewById(R.id.btn_all_hangouts);

        // Set up the All Hangouts button
        allHangoutsButton.setOnClickListener(v -> {
            // Track button click
            mFirebaseAnalytics.logEvent("all_hangouts_click", null);
            
            Intent intent = new Intent(this, AllHangoutsActivity.class);
            startActivity(intent);
        });

        // Set up recycler view with curved layout
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setBezelFraction(0.5f);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Load groups directly
        loadGroups();
    }

    private void loadGroups() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        
        // Use the hardcoded user ID to query groups
        firestore.collection("groups")
                .whereArrayContains("members", HARDCODED_USER_ID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    
                    groups.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Group group = document.toObject(Group.class);
                        group.setId(document.getId());
                        groups.add(group);
                    }
                    
                    // Setup adapter with data
                    GroupAdapter adapter = new GroupAdapter(this, groups, this);
                    recyclerView.setAdapter(adapter);
                    
                    if (groups.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setText("No groups found for this user");
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        
                        // Log groups loaded event
                        Bundle params = new Bundle();
                        params.putInt("group_count", groups.size());
                        mFirebaseAnalytics.logEvent("groups_loaded", params);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading groups", e);
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText("Error loading groups: " + e.getMessage());
                    emptyView.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onGroupClick(Group group) {
        // Track group click
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, group.getId());
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, group.getName());
        mFirebaseAnalytics.logEvent("group_selected", params);
        
        Intent intent = new Intent(this, GroupHangoutsActivity.class);
        intent.putExtra("GROUP_ID", group.getId());
        intent.putExtra("GROUP_NAME", group.getName());
        startActivity(intent);
    }
}