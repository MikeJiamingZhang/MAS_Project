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

public class AllHangoutsActivity extends Activity {

    private WearableRecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView titleView;
    private List<Hangout> hangouts = new ArrayList<>();
    private HangoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_hangouts); // Reuse the same layout

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

        // Set up adapter
        adapter = new HangoutAdapter(this, hangouts);
        recyclerView.setAdapter(adapter);

        // Load all hangouts
        loadAllMockHangouts();
    }

    private void loadAllMockHangouts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        // Simulate network delay
        recyclerView.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            // Create mock data for all groups
            hangouts.clear();

            // Family group hangouts
            Calendar cal = Calendar.getInstance();

            cal.add(Calendar.DAY_OF_MONTH, 3);
            hangouts.add(new Hangout(
                    "101",
                    "Family Dinner",
                    "Mom's House",
                    new Timestamp(cal.getTime()),
                    "1" // Family group
            ));

            cal.add(Calendar.DAY_OF_MONTH, 7);
            hangouts.add(new Hangout(
                    "102",
                    "Weekend Getaway",
                    "Mountain Cabin",
                    new Timestamp(cal.getTime()),
                    "1" // Family group
            ));

            // College Friends group hangouts
            cal = Calendar.getInstance();

            cal.add(Calendar.DAY_OF_MONTH, 1);
            hangouts.add(new Hangout(
                    "201",
                    "Game Night",
                    "Jake's Apartment",
                    new Timestamp(cal.getTime()),
                    "2" // College Friends group
            ));

            cal.add(Calendar.DAY_OF_MONTH, 5);
            hangouts.add(new Hangout(
                    "202",
                    "Beach Day",
                    "Venice Beach",
                    new Timestamp(cal.getTime()),
                    "2" // College Friends group
            ));

            cal.add(Calendar.DAY_OF_MONTH, 14);
            hangouts.add(new Hangout(
                    "203",
                    "Reunion Party",
                    "Downtown Lounge",
                    new Timestamp(cal.getTime()),
                    "2" // College Friends group
            ));

            // Work Colleagues group hangouts
            cal = Calendar.getInstance();

            cal.add(Calendar.DAY_OF_MONTH, 2);
            hangouts.add(new Hangout(
                    "301",
                    "Team Lunch",
                    "Italian Restaurant",
                    new Timestamp(cal.getTime()),
                    "3" // Work Colleagues group
            ));

            cal.add(Calendar.DAY_OF_MONTH, 10);
            hangouts.add(new Hangout(
                    "302",
                    "Quarterly Celebration",
                    "Company HQ",
                    new Timestamp(cal.getTime()),
                    "3" // Work Colleagues group
            ));

            // Create a custom comparator to sort by date
            hangouts.sort((h1, h2) -> h1.getDate().compareTo(h2.getDate()));

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