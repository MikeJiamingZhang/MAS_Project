package com.example.mas_solution_2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_hangouts);

            // Initialize Firebase
            firestore = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

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

            // Setup Create Hangout button
            ImageButton createHangoutButton = findViewById(R.id.btn_create_hangout);
            if (createHangoutButton != null) {
                createHangoutButton.setOnClickListener(v -> {
                    // Load user's groups before showing the dialog
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
                        // TODO: Implement profile page
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
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading groups: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating hangout: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error creating hangout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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