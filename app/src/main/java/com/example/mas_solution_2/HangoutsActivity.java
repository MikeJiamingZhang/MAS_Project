package com.example.mas_solution_2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HangoutsActivity extends AppCompatActivity {

    private static final String TAG = "HangoutsActivity";

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String groupId = "";
    private String groupName = "";
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private List<Group> userGroups = new ArrayList<>();
    private HangoutsPagerAdapter pagerAdapter;
    private ImageButton groupInfoButton;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_hangouts);

            // Initialize Firebase
            firestore = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            // Log screen view event
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Hangouts Screen");
            params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "HangoutsActivity");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);

            // Check if user is logged in
            if (currentUser == null) {
                startActivity(new Intent(this, authentication.class));
                finish();
                return;
            }

            // Get group info if passed
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                groupId = extras.getString("GROUP_ID", "");
                groupName = extras.getString("GROUP_NAME", "");
            }

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    if (!groupName.isEmpty()) {
                        getSupportActionBar().setTitle(groupName + " Hangouts");
                    } else {
                        getSupportActionBar().setTitle("All Hangouts");
                    }
                }
            }

            // Setup Group Info button - only visible when viewing a specific group
            groupInfoButton = findViewById(R.id.btn_group_info);
            if (groupInfoButton != null) {
                if (!groupId.isEmpty()) {
                    groupInfoButton.setVisibility(View.VISIBLE);
                    groupInfoButton.setOnClickListener(v -> {
                        showGroupInfoDialog();
                    });
                } else {
                    groupInfoButton.setVisibility(View.GONE);
                }
            }

            // Setup Create Hangout button
            ImageButton createHangoutButton = findViewById(R.id.btn_create_hangout);
            if (createHangoutButton != null) {
                createHangoutButton.setOnClickListener(v -> {
                    // Track create hangout button click
                    mFirebaseAnalytics.logEvent("create_hangout_button_click", null);
                    loadUserGroups(() -> showCreateHangoutDialog());
                });
            }

            // Set up ViewPager and TabLayout
            viewPager = findViewById(R.id.view_pager);
            tabLayout = findViewById(R.id.tab_layout);

            if (viewPager != null && tabLayout != null) {
                pagerAdapter = new HangoutsPagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);

                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    tab.setText(position == 0 ? "Upcoming" : "Past");
                }).attach();
            }

            // Set up bottom navigation
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(this, GroupsActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_calendar) {
                        // Already on hangouts page
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        Toast.makeText(this, "Profile page coming soon!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });

                // Set calendar as selected
                bottomNav.setSelectedItemId(R.id.nav_calendar);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            e.printStackTrace();

            // If we crash in initialization, go back to the groups activity
            Toast.makeText(this, "There was an error. Please try again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, GroupsActivity.class));
            finish();
        }
    }

    private void showGroupInfoDialog() {
        try {
            // Only show dialog if we have a group ID
            if (groupId.isEmpty()) {
                Toast.makeText(this, "No group selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_group_info, null);
            builder.setView(dialogView);

            // Get references to views
            TextView groupNameView = dialogView.findViewById(R.id.group_name);
            TextView joinCodeView = dialogView.findViewById(R.id.join_code);
            TextView createdDateView = dialogView.findViewById(R.id.created_date);
            RecyclerView membersListView = dialogView.findViewById(R.id.members_list);
            Button copyCodeButton = dialogView.findViewById(R.id.btn_copy_code);
            Button closeButton = dialogView.findViewById(R.id.btn_close);

            // Set group name
            groupNameView.setText(groupName);

            // Initialize members adapter
            List<String> members = new ArrayList<>();
            GroupMemberAdapter membersAdapter = new GroupMemberAdapter(this, members);
            membersListView.setLayoutManager(new LinearLayoutManager(this));
            membersListView.setAdapter(membersAdapter);

            // Create and show the dialog
            AlertDialog dialog = builder.create();

            // Fetch group details from Firestore
            firestore.collection("groups").document(groupId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get join code
                            String joinCode = documentSnapshot.getString("joinCode");
                            if (joinCode != null) {
                                joinCodeView.setText(joinCode);
                            }

                            // Get creation date
                            Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
                            if (createdAt != null) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                                createdDateView.setText(dateFormat.format(createdAt.toDate()));
                            }

                            // Get members
                            List<String> memberIds = (List<String>) documentSnapshot.get("members");
                            if (memberIds != null && !memberIds.isEmpty()) {
                                fetchMemberNames(memberIds, membersAdapter);
                            }

                            // Set up copy button
                            copyCodeButton.setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Join Code", joinCode);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(this, "Join code copied to clipboard", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });

            // Set up close button
            closeButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing group info dialog: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error showing group info", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMemberNames(List<String> memberIds, GroupMemberAdapter adapter) {
        List<String> memberNames = new ArrayList<>();

        // Use the Firebase Auth instance to get the current user's info
        String currentUserDisplayName = currentUser.getDisplayName();
        String currentUserEmail = currentUser.getEmail();
        String currentUserId = currentUser.getUid();

        // For the current user, add their display name with "(You)"
        for (int i = 0; i < memberIds.size(); i++) {
            String memberId = memberIds.get(i);
            if (memberId.equals(currentUserId)) {
                memberNames.add(currentUserDisplayName + " (You)");
            } else {
                // For other users, default to showing "User" + userID prefix
                // (We'll update these as we fetch emails)
                memberNames.add("User " + memberId.substring(0, Math.min(6, memberId.length())));
            }
        }

        // Initial update with placeholder names
        adapter.updateMembers(memberNames);

        // Get user emails from "users" collection where we store authentication info
        for (int i = 0; i < memberIds.size(); i++) {
            final int index = i;
            String memberId = memberIds.get(i);

            // Skip the current user since we already have their name
            if (memberId.equals(currentUserId)) {
                continue;
            }

            // Try to look up this user in the "users" collection
            firestore.collection("users").document(memberId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // If we found the user, get their email
                            String email = documentSnapshot.getString("email");
                            if (email != null && !email.isEmpty()) {
                                // Extract the username part from the email
                                String username = email.split("@")[0];

                                // Update the member name in our list
                                memberNames.set(index, username);

                                // Update the adapter with the new list
                                adapter.updateMembers(memberNames);
                            }
                        }
                    });

            // Also check if we have this info stored in the group document
            firestore.collection("groups").document(groupId)
                    .get()
                    .addOnSuccessListener(groupDocument -> {
                        if (groupDocument.exists()) {
                            // Check if memberEmails field exists
                            Object memberEmailsObj = groupDocument.get("memberEmails");
                            if (memberEmailsObj instanceof Map) {
                                Map<String, String> memberEmails = (Map<String, String>) memberEmailsObj;
                                String email = memberEmails.get(memberId);

                                if (email != null && !email.isEmpty()) {
                                    // Extract username part
                                    String username = email.split("@")[0];

                                    // Update the member name in our list
                                    memberNames.set(index, username);

                                    // Update the adapter
                                    adapter.updateMembers(memberNames);
                                }
                            }

                            // Store current user's email if we have one
                            if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
                                Map<String, Object> updates = new HashMap<>();

                                // Initialize memberEmails map if it doesn't exist
                                if (!(groupDocument.get("memberEmails") instanceof Map)) {
                                    Map<String, String> newMap = new HashMap<>();
                                    newMap.put(currentUserId, currentUserEmail);
                                    updates.put("memberEmails", newMap);
                                } else {
                                    // Just update the current user's entry
                                    updates.put("memberEmails." + currentUserId, currentUserEmail);
                                }

                                // Update the document
                                groupDocument.getReference().update(updates);
                            }
                        }
                    });
        }
    }

    private void loadUserGroups(Runnable onComplete) {
        try {
            firestore.collection("groups")
                    .whereArrayContains("members", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            userGroups.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Group group = document.toObject(Group.class);
                                group.setId(document.getId());
                                userGroups.add(group);
                            }

                            if (userGroups.isEmpty()) {
                                Toast.makeText(this, "You need to create or join a group first", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, GroupsActivity.class));
                                finish();
                            } else {
                                onComplete.run();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing groups: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading groups. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in loadUserGroups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCreateHangoutDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_hangout, null);
            builder.setView(dialogView);

            EditText hangoutNameEditText = dialogView.findViewById(R.id.edit_hangout_name);
            EditText locationEditText = dialogView.findViewById(R.id.edit_hangout_location);
            EditText dateEditText = dialogView.findViewById(R.id.edit_hangout_date);
            EditText timeEditText = dialogView.findViewById(R.id.edit_hangout_time);
            Spinner groupSpinner = dialogView.findViewById(R.id.spinner_group);
            Button cancelButton = dialogView.findViewById(R.id.button_cancel);
            Button createButton = dialogView.findViewById(R.id.button_create);

            // Set up the date picker
            final Calendar calendar = Calendar.getInstance();
            if (dateEditText != null) {
                dateEditText.setOnClickListener(v -> {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            this,
                            (view, year, month, dayOfMonth) -> {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                                dateEditText.setText(dateFormat.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                });
            }

            // Set up the time picker
            if (timeEditText != null) {
                timeEditText.setOnClickListener(v -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                                timeEditText.setText(timeFormat.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                });
            }

            // Set up the group spinner
            List<String> groupNames = new ArrayList<>();
            Map<String, String> groupNameToId = new HashMap<>();

            // If a specific group was selected, preselect it in the spinner
            int selectedGroupPosition = 0;
            int i = 0;

            for (Group group : userGroups) {
                groupNames.add(group.getName());
                groupNameToId.put(group.getName(), group.getId());

                if (group.getId().equals(groupId)) {
                    selectedGroupPosition = i;
                }
                i++;
            }

            if (groupSpinner != null && !groupNames.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, groupNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                groupSpinner.setAdapter(adapter);

                // Preselect the group if one was passed
                if (!groupId.isEmpty() && selectedGroupPosition < groupNames.size()) {
                    groupSpinner.setSelection(selectedGroupPosition);
                }
            }

            AlertDialog dialog = builder.create();
            dialog.show();

            if (cancelButton != null) {
                cancelButton.setOnClickListener(v -> dialog.dismiss());
            }

            if (createButton != null && hangoutNameEditText != null && locationEditText != null &&
                    dateEditText != null && timeEditText != null && groupSpinner != null) {

                createButton.setOnClickListener(v -> {
                    try {
                        // Validate input
                        String hangoutName = hangoutNameEditText.getText().toString().trim();
                        String location = locationEditText.getText().toString().trim();
                        String dateStr = dateEditText.getText().toString().trim();
                        String timeStr = timeEditText.getText().toString().trim();

                        if (userGroups.isEmpty() || groupSpinner.getAdapter() == null ||
                                groupSpinner.getAdapter().getCount() == 0) {
                            Toast.makeText(this, "You need to be in a group to create a hangout", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedGroupName = (String) groupSpinner.getSelectedItem();

                        if (hangoutName.isEmpty()) {
                            hangoutNameEditText.setError("Please enter a hangout name");
                            return;
                        }

                        if (location.isEmpty()) {
                            locationEditText.setError("Please enter a location");
                            return;
                        }

                        if (dateStr.isEmpty()) {
                            dateEditText.setError("Please select a date");
                            return;
                        }

                        if (timeStr.isEmpty()) {
                            timeEditText.setError("Please select a time");
                            return;
                        }

                        // Parse date and time
                        Date hangoutDate = null;
                        try {
                            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                            hangoutDate = dateTimeFormat.parse(dateStr + " " + timeStr);
                        } catch (ParseException e) {
                            Toast.makeText(this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (hangoutDate == null) {
                            Toast.makeText(this, "Error parsing date/time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get selected group ID
                        String selectedGroupId = groupNameToId.get(selectedGroupName);

                        // Create the hangout
                        createHangout(hangoutName, location, hangoutDate, selectedGroupId);
                        dialog.dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in create button click: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error creating hangout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showCreateHangoutDialog: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createHangout(String name, String location, Date date, String groupId) {
        try {
            // Show progress indicator
            Toast.makeText(this, "Creating hangout...", Toast.LENGTH_SHORT).show();

            Map<String, Object> hangoutData = new HashMap<>();
            hangoutData.put("name", name);
            hangoutData.put("location", location);
            hangoutData.put("date", new Timestamp(date));
            hangoutData.put("groupId", groupId);
            hangoutData.put("imageUrl", "");
            hangoutData.put("isPast", date.before(new Date()));
            hangoutData.put("participants", new ArrayList<String>() {{ add(currentUser.getUid()); }});
            hangoutData.put("photoUrls", new ArrayList<String>());
            hangoutData.put("createdAt", FieldValue.serverTimestamp());
            hangoutData.put("createdBy", currentUser.getUid());

            firestore.collection("hangouts")
                    .add(hangoutData)
                    .addOnSuccessListener(documentReference -> {
                        try {
                            // Track hangout creation success
                            Bundle params = new Bundle();
                            params.putString(FirebaseAnalytics.Param.ITEM_ID, documentReference.getId());
                            params.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
                            mFirebaseAnalytics.logEvent("hangout_created", params);
                            
                            Toast.makeText(this, "Hangout created successfully!", Toast.LENGTH_SHORT).show();

                            // Create a chat room for the hangout
                            Map<String, Object> chatData = new HashMap<>();
                            chatData.put("createdAt", FieldValue.serverTimestamp());
                            firestore.collection("chatHistory").document(documentReference.getId()).set(chatData);

                            // Refresh the fragments
                            refreshFragments();
                        } catch (Exception e) {
                            Log.e(TAG, "Error after successful hangout creation: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in createHangout: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error creating hangout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshFragments() {
        try {
            // Get the current fragment
            int currentPosition = viewPager.getCurrentItem();

            // Force reload of the current fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
            if (currentFragment instanceof HangoutsFragment) {
                ((HangoutsFragment) currentFragment).refreshHangouts();
            }

            // Recreate the adapter to force refresh of all fragments
            pagerAdapter = new HangoutsPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(currentPosition);

            // Reattach tab layout
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(position == 0 ? "Upcoming" : "Past");
            }).attach();
        } catch (Exception e) {
            Log.e(TAG, "Error in refreshFragments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class HangoutsPagerAdapter extends FragmentStateAdapter {

        public HangoutsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Create the appropriate fragment based on position
            try {
                return HangoutsFragment.newInstance(position == 0, groupId);
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment: " + e.getMessage());
                e.printStackTrace();
                // Return an empty fragment as fallback
                return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Upcoming and Past tabs
        }
    }
}