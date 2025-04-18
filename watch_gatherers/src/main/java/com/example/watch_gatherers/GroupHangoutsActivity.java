package com.example.watch_gatherers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GroupHangoutsActivity extends Activity {

    private String groupId;
    private String groupName;

    private TextView titleView;
    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private List<Hangout> hangouts = new ArrayList<>();
    private HangoutAdapter adapter;

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
            finish();
            return;
        }

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
        loadMockHangouts();
    }

    private void loadMockHangouts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Simulate network delay
        recyclerView.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            // Create mock data based on group ID
            hangouts.clear();

            // Family group (ID: 1)
            if ("1".equals(groupId)) {
                Calendar cal = Calendar.getInstance();

                // Future hangout
                cal.add(Calendar.DAY_OF_MONTH, 3);
                hangouts.add(new Hangout(
                        "101",
                        "Family Dinner",
                        "Mom's House",
                        new Timestamp(cal.getTime()),
                        groupId
                ));

                // Another future hangout
                cal.add(Calendar.DAY_OF_MONTH, 7);
                hangouts.add(new Hangout(
                        "102",
                        "Weekend Getaway",
                        "Mountain Cabin",
                        new Timestamp(cal.getTime()),
                        groupId
                ));
            }
            // College Friends group (ID: 2)
            else if ("2".equals(groupId)) {
                Calendar cal = Calendar.getInstance();

                // Future hangout
                cal.add(Calendar.DAY_OF_MONTH, 1);
                hangouts.add(new Hangout(
                        "201",
                        "Game Night",
                        "Jake's Apartment",
                        new Timestamp(cal.getTime()),
                        groupId
                ));

                // Another future hangout
                cal.add(Calendar.DAY_OF_MONTH, 5);
                hangouts.add(new Hangout(
                        "202",
                        "Beach Day",
                        "Venice Beach",
                        new Timestamp(cal.getTime()),
                        groupId
                ));

                // One more hangout
                cal.add(Calendar.DAY_OF_MONTH, 14);
                hangouts.add(new Hangout(
                        "203",
                        "Reunion Party",
                        "Downtown Lounge",
                        new Timestamp(cal.getTime()),
                        groupId
                ));
            }
            // Work Colleagues group (ID: 3)
            else if ("3".equals(groupId)) {
                Calendar cal = Calendar.getInstance();

                // Future hangout
                cal.add(Calendar.DAY_OF_MONTH, 2);
                hangouts.add(new Hangout(
                        "301",
                        "Team Lunch",
                        "Italian Restaurant",
                        new Timestamp(cal.getTime()),
                        groupId
                ));

                // Another future hangout
                cal.add(Calendar.DAY_OF_MONTH, 10);
                hangouts.add(new Hangout(
                        "302",
                        "Quarterly Celebration",
                        "Company HQ",
                        new Timestamp(cal.getTime()),
                        groupId
                ));
            }

            adapter.notifyDataSetChanged();

            if (hangouts.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setText("No upcoming hangouts");
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }, 1000); // 1-second delay to simulate loading
    }
}