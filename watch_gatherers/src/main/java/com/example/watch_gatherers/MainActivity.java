package com.example.watch_gatherers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements GroupAdapter.GroupClickListener {

    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private Button allHangoutsButton;
    private List<Group> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        allHangoutsButton = findViewById(R.id.btn_all_hangouts);

        // Set up the All Hangouts button
        allHangoutsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllHangoutsActivity.class);
            startActivity(intent);
        });

        // Set up recycler view with curved layout
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setBezelFraction(0.5f);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Load mock data
        loadMockGroups();
    }

    private void loadMockGroups() {
        progressBar.setVisibility(View.VISIBLE);

        // Simulate network delay
        recyclerView.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            // Create mock data
            groups.clear();
            groups.add(new Group("1", "Family"));
            groups.add(new Group("2", "College Friends"));
            groups.add(new Group("3", "Work Colleagues"));

            // Setup and display the adapter
            GroupAdapter adapter = new GroupAdapter(this, groups, this);
            recyclerView.setAdapter(adapter);

            if (groups.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setText("No groups found");
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }, 1000); // 1-second delay to simulate loading
    }

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, GroupHangoutsActivity.class);
        intent.putExtra("GROUP_ID", group.getId());
        intent.putExtra("GROUP_NAME", group.getName());
        startActivity(intent);
    }
}