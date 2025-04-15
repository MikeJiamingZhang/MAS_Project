package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HangoutsFragment extends Fragment implements HangoutAdapter.HangoutClickListener {

    private static final String TAG = "HangoutsFragment";
    private static final String ARG_IS_UPCOMING = "isUpcoming";
    private static final String ARG_GROUP_ID = "groupId";

    private boolean isUpcoming;
    private String groupId = "";
    private RecyclerView recyclerView;
    private TextView noHangoutsText;
    private HangoutAdapter adapter;
    private List<Hangout> hangoutList;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private boolean dataLoaded = false;

    public HangoutsFragment() {
        // Required empty public constructor
        hangoutList = new ArrayList<>();
    }

    public static HangoutsFragment newInstance(boolean isUpcoming, String groupId) {
        HangoutsFragment fragment = new HangoutsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_UPCOMING, isUpcoming);
        if (groupId != null) {
            args.putString(ARG_GROUP_ID, groupId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                isUpcoming = getArguments().getBoolean(ARG_IS_UPCOMING, true);
                groupId = getArguments().getString(ARG_GROUP_ID, "");
            }

            // Initialize Firebase
            firestore = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_hangouts, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            e.printStackTrace();
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            recyclerView = view.findViewById(R.id.recycler_hangouts);
            noHangoutsText = view.findViewById(R.id.text_no_hangouts);

            // Safety checks
            if (recyclerView == null || noHangoutsText == null) {
                Log.e(TAG, "Views not found in layout");
                return;
            }

            // Initialize RecyclerView
            adapter = new HangoutAdapter(requireContext(), hangoutList, this, isUpcoming);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);

            // Start with demo data immediately to prevent blank screen
            if (!dataLoaded) {
                addDemoHangouts();
                updateVisibility();
            }

            // Then try to load real data
            loadHangouts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // Refresh data when fragment becomes visible, but only if already loaded once
            if (dataLoaded) {
                refreshHangouts();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshHangouts() {
        try {
            // Only reload if attached to activity
            if (isAdded() && getContext() != null) {
                loadHangouts();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in refreshHangouts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHangouts() {
        try {
            // Check if fragment is still attached
            if (!isAdded() || getContext() == null) {
                Log.e(TAG, "Fragment not attached in loadHangouts");
                return;
            }

            // Check if user is logged in
            if (currentUser == null) {
                Log.e(TAG, "User not logged in in loadHangouts");
                if (noHangoutsText != null && recyclerView != null) {
                    noHangoutsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                return;
            }

            // Simple query - just get all hangouts
            firestore.collection("hangouts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            if (!isAdded() || getContext() == null) return; // Check if fragment is still attached

                            List<Hangout> fetchedHangouts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                try {
                                    Hangout hangout = document.toObject(Hangout.class);
                                    hangout.setId(document.getId());

                                    // Filter manually based on groupId and isPast
                                    boolean matchesGroup = groupId == null || groupId.isEmpty() || hangout.getGroupId().equals(groupId);
                                    boolean matchesStatus = (isUpcoming && !hangout.isPast()) || (!isUpcoming && hangout.isPast());

                                    if (matchesGroup && matchesStatus) {
                                        fetchedHangouts.add(hangout);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing hangout: " + e.getMessage());
                                }
                            }

                            // Mark as loaded
                            dataLoaded = true;

                            // Update the list if we have adapter
                            if (adapter != null) {
                                hangoutList.clear();
                                hangoutList.addAll(fetchedHangouts);
                                adapter.notifyDataSetChanged();

                                // If no real data, add demo data
                                if (hangoutList.isEmpty()) {
                                    addDemoHangouts();
                                }

                                // Update UI visibility
                                updateVisibility();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing hangouts: " + e.getMessage());
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            if (!isAdded() || getContext() == null) return; // Check if fragment is still attached

                            Log.e(TAG, "Error loading hangouts: " + e.getMessage());

                            // If error, make sure we have demo data
                            if (hangoutList.isEmpty()) {
                                addDemoHangouts();
                            }

                            // Update UI visibility
                            updateVisibility();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error handling hangout load failure: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in loadHangouts: " + e.getMessage());
            e.printStackTrace();

            // Ensure we have demo data
            if (hangoutList == null || hangoutList.isEmpty()) {
                hangoutList = new ArrayList<>();
                addDemoHangouts();
            }

            // Update UI visibility
            updateVisibility();
        }
    }

    private void updateVisibility() {
        try {
            if (!isAdded() || getContext() == null || recyclerView == null || noHangoutsText == null) return; // Check if fragment is still attached

            if (hangoutList == null || hangoutList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                noHangoutsText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noHangoutsText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateVisibility: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addDemoHangouts() {
        try {
            // Initialize hangoutList if null
            if (hangoutList == null) {
                hangoutList = new ArrayList<>();
            }

            // Only add demo data if the list is empty
            if (!hangoutList.isEmpty()) {
                return;
            }

            // Define the current time for comparison
            Date now = new Date();

            // For upcoming tab
            if (isUpcoming) {
                // Add a future event
                Date futureDate1 = new Date(now.getTime() + 86400000); // Tomorrow
                Hangout hangout1 = new Hangout(
                        "demo1",
                        "Demo: Karaoke Night",
                        "Duluth, GA",
                        "",
                        new Timestamp(futureDate1),
                        groupId == null || groupId.isEmpty() ? "demo" : groupId
                );

                // Add another future event
                Date futureDate2 = new Date(now.getTime() + 172800000); // Day after tomorrow
                Hangout hangout2 = new Hangout(
                        "demo2",
                        "Demo: Bowling Night",
                        "Stone Mountain, GA",
                        "",
                        new Timestamp(futureDate2),
                        groupId == null || groupId.isEmpty() ? "demo" : groupId
                );

                hangoutList.add(hangout1);
                hangoutList.add(hangout2);
            } else {
                // For past tab
                // Add a past event
                Date pastDate1 = new Date(now.getTime() - 86400000); // Yesterday
                Hangout hangout1 = new Hangout(
                        "demo3",
                        "Demo: Movie Night",
                        "Atlanta, GA",
                        "",
                        new Timestamp(pastDate1),
                        groupId == null || groupId.isEmpty() ? "demo" : groupId
                );

                hangoutList.add(hangout1);
            }

            // Notify adapter if available
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in addDemoHangouts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onHangoutClick(Hangout hangout) {
        try {
            if (!isAdded() || getContext() == null) return;

            Intent intent = new Intent(requireContext(), HangoutDetailActivity.class);
            intent.putExtra("HANGOUT_ID", hangout.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error in onHangoutClick: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelClick(Hangout hangout) {
        try {
            if (!isAdded() || getContext() == null) return;

            // Show confirmation dialog
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Cancel Hangout")
                    .setMessage("Are you sure you want to cancel this hangout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the hangout from Firestore
                        firestore.collection("hangouts").document(hangout.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Hangout cancelled", Toast.LENGTH_SHORT).show();
                                    loadHangouts(); // Refresh the list
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Error cancelling hangout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCancelClick: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onVoteClick(Hangout hangout) {
        try {
            if (!isAdded() || getContext() == null) return;

            // Navigate to the voting screen (MainActivity)
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.putExtra("HANGOUT_ID", hangout.getId());
            intent.putExtra("GROUP_NAME", hangout.getName() + " Voting");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error in onVoteClick: " + e.getMessage());
            e.printStackTrace();
        }
    }
}