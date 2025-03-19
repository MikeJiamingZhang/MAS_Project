package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity implements GroupAdapter.GroupClickListener {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // If user not signed in, redirect to authentication
        if (currentUser == null) {
            startActivity(new Intent(this, authentication.class));
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up buttons
        ImageButton createGroupButton = findViewById(R.id.btn_create_group);
        createGroupButton.setOnClickListener(v -> {
            // TODO: Implement group creation
            Toast.makeText(this, "Create group feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        ImageButton nightModeButton = findViewById(R.id.btn_night_mode);
        nightModeButton.setOnClickListener(v -> {
            // TODO: Implement night mode toggle
            Toast.makeText(this, "Night mode feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_groups);
        groupList = new ArrayList<>();
        adapter = new GroupAdapter(this, groupList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home page
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, HangoutsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // TODO: Implement profile page
                Toast.makeText(this, "Profile page coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Set home as selected
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Load groups data
        loadGroups();
    }

    private void loadGroups() {
        // For demo purposes, add some example groups
        if (groupList.isEmpty()) {
            addDemoGroups();
        }

        // TODO: Load groups from Firestore
        firestore.collection("groups")
                .whereArrayContains("members", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> fetchedGroups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Group group = document.toObject(Group.class);
                        group.setId(document.getId());
                        fetchedGroups.add(group);
                    }

                    if (!fetchedGroups.isEmpty()) {
                        groupList.clear();
                        groupList.addAll(fetchedGroups);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading groups: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void addDemoGroups() {
        // Add example groups for development/demo purposes
        Group group1 = new Group("1", "Childhood Buddies", "https://via.placeholder.com/150");
        Group group2 = new Group("2", "Roommates", "https://via.placeholder.com/150");
        Group group3 = new Group("3", "Family Friends", "https://via.placeholder.com/150");

        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, HangoutsActivity.class);
        intent.putExtra("GROUP_ID", group.getId());
        intent.putExtra("GROUP_NAME", group.getName());
        startActivity(intent);
    }

    @Override
    public void onLeaveClick(Group group) {
        // Show confirmation dialog
        Toast.makeText(this, "Are you sure you want to leave " + group.getName() + "?", Toast.LENGTH_SHORT).show();

        // TODO: Implement leave group functionality with Firestore
    }

    @Override
    public void onChatClick(Group group) {
        // Navigate to MainActivity (Chat) with the group ID
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("GROUP_ID", group.getId());
        intent.putExtra("GROUP_NAME", group.getName());
        startActivity(intent);
    }
}