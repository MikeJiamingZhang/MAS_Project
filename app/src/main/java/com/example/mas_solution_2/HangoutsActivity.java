package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HangoutsActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangouts);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        setSupportActionBar(toolbar);
        if (!groupName.isEmpty()) {
            toolbar.setTitle(groupName + " Hangouts");
        }

        // Setup Create Hangout button
        ImageButton createHangoutButton = findViewById(R.id.btn_create_hangout);
        createHangoutButton.setOnClickListener(v -> {
            // TODO: Implement hangout creation
            Toast.makeText(this, "Create hangout feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Set up ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        HangoutsPagerAdapter pagerAdapter = new HangoutsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Upcoming" : "Past");
        }).attach();

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, GroupsActivity.class));
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

    private class HangoutsPagerAdapter extends FragmentStateAdapter {

        public HangoutsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Create the appropriate fragment based on position
            return HangoutsFragment.newInstance(position == 0, groupId);
        }

        @Override
        public int getItemCount() {
            return 2; // Upcoming and Past tabs
        }
    }
}