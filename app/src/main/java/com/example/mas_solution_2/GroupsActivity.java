package com.example.mas_solution_2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
            showCreateGroupDialog();
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
                // Navigate directly to the hangouts page without requiring group selection
                startActivity(new Intent(this, HangoutsActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_join_group) {
            showJoinGroupDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null);
        builder.setView(dialogView);

        EditText groupNameEditText = dialogView.findViewById(R.id.edit_group_name);
        TextView joinCodeTextView = dialogView.findViewById(R.id.text_join_code);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button createButton = dialogView.findViewById(R.id.button_create);

        // Generate a random 6-character join code
        String joinCode = generateJoinCode();
        joinCodeTextView.setText("Join Code: " + joinCode);

        AlertDialog dialog = builder.create();
        dialog.show();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        createButton.setOnClickListener(v -> {
            String groupName = groupNameEditText.getText().toString().trim();

            if (groupName.isEmpty()) {
                groupNameEditText.setError("Please enter a group name");
                return;
            }

            createGroup(groupName, joinCode);
            dialog.dismiss();
        });
    }

    private void showJoinGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_join_group, null);
        builder.setView(dialogView);

        EditText joinCodeEditText = dialogView.findViewById(R.id.edit_join_code);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button joinButton = dialogView.findViewById(R.id.button_join);

        AlertDialog dialog = builder.create();
        dialog.show();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        joinButton.setOnClickListener(v -> {
            String joinCode = joinCodeEditText.getText().toString().trim();

            if (joinCode.isEmpty()) {
                joinCodeEditText.setError("Please enter a join code");
                return;
            }

            joinGroup(joinCode);
            dialog.dismiss();
        });
    }

    private String generateJoinCode() {
        String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder builder = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            builder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return builder.toString();
    }

    private void createGroup(String groupName, String joinCode) {
        // Get current user info
        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        groupData.put("joinCode", joinCode);
        groupData.put("imageUrl", "https://via.placeholder.com/150");
        groupData.put("createdBy", userId);
        groupData.put("members", new ArrayList<String>() {{ add(userId); }});
        groupData.put("createdAt", FieldValue.serverTimestamp());

        // Add a map to store member emails
        Map<String, String> memberEmails = new HashMap<>();
        if (userEmail != null && !userEmail.isEmpty()) {
            memberEmails.put(userId, userEmail);
        }
        groupData.put("memberEmails", memberEmails);

        firestore.collection("groups")
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();

                    // Refresh the group list
                    loadGroups();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void joinGroup(String joinCode) {
        firestore.collection("groups")
                .whereEqualTo("joinCode", joinCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Invalid join code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QueryDocumentSnapshot document = queryDocumentSnapshots.iterator().next();

                    // Get current user info
                    String userId = currentUser.getUid();
                    String userEmail = currentUser.getEmail();
                    String displayName = currentUser.getDisplayName();

                    // Updates to make to the group document
                    Map<String, Object> updates = new HashMap<>();

                    // Add user to members list
                    updates.put("members", FieldValue.arrayUnion(userId));

                    // Store user email in memberEmails map
                    if (userEmail != null && !userEmail.isEmpty()) {
                        updates.put("memberEmails." + userId, userEmail);
                    }

                    // Apply all updates
                    document.getReference().update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Successfully joined the group!", Toast.LENGTH_SHORT).show();

                                // Refresh the group list
                                loadGroups();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error joining group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error finding group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGroups() {
        // For demo purposes, add some example groups
        if (groupList.isEmpty()) {
            addDemoGroups();
        }

        // Load groups from Firestore
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
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave " + group.getName() + "?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    // Remove user from group members
                    firestore.collection("groups").document(group.getId())
                            .update("members", FieldValue.arrayRemove(currentUser.getUid()))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Left group successfully", Toast.LENGTH_SHORT).show();
                                loadGroups(); // Refresh the list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error leaving group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
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