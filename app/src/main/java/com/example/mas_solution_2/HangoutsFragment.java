package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HangoutsFragment extends Fragment implements HangoutAdapter.HangoutClickListener {

    private static final String ARG_IS_UPCOMING = "isUpcoming";
    private static final String ARG_GROUP_ID = "groupId";

    private boolean isUpcoming;
    private String groupId;
    private RecyclerView recyclerView;
    private TextView noHangoutsText;
    private HangoutAdapter adapter;
    private List<Hangout> hangoutList;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    public HangoutsFragment() {
        // Required empty public constructor
    }

    public static HangoutsFragment newInstance(boolean isUpcoming, String groupId) {
        HangoutsFragment fragment = new HangoutsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_UPCOMING, isUpcoming);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isUpcoming = getArguments().getBoolean(ARG_IS_UPCOMING, true);
            groupId = getArguments().getString(ARG_GROUP_ID, "");
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hangouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_hangouts);
        noHangoutsText = view.findViewById(R.id.text_no_hangouts);

        // Initialize RecyclerView
        hangoutList = new ArrayList<>();
        adapter = new HangoutAdapter(requireContext(), hangoutList, this, isUpcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Load hangouts
        loadHangouts();
    }

    private void loadHangouts() {
        // For demo purposes, add some example hangouts
        if (hangoutList.isEmpty()) {
            addDemoHangouts();
        }

        // TODO: Load real hangouts from Firestore
        Query query = firestore.collection("hangouts");

        // Filter by group if specified
        if (!groupId.isEmpty()) {
            query = query.whereEqualTo("groupId", groupId);
        }

        // Filter by date (upcoming or past)
        Date now = new Date();
        if (isUpcoming) {
            query = query.whereGreaterThanOrEqualTo("date", now);
        } else {
            query = query.whereLessThan("date", now);
        }

        // Order by date
        query = isUpcoming
                ? query.orderBy("date", Query.Direction.ASCENDING)
                : query.orderBy("date", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Hangout> fetchedHangouts = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Hangout hangout = document.toObject(Hangout.class);
                hangout.setId(document.getId());
                fetchedHangouts.add(hangout);
            }

            if (!fetchedHangouts.isEmpty()) {
                hangoutList.clear();
                hangoutList.addAll(fetchedHangouts);
                adapter.notifyDataSetChanged();

                recyclerView.setVisibility(View.VISIBLE);
                noHangoutsText.setVisibility(View.GONE);
            } else if (hangoutList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                noHangoutsText.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error loading hangouts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addDemoHangouts() {
        // Define the current time for comparison
        Date now = new Date();

        // For upcoming tab
        if (isUpcoming) {
            // Add a future event
            Date futureDate1 = new Date(now.getTime() + 86400000); // Tomorrow
            Hangout hangout1 = new Hangout(
                    "1",
                    "Do Re Mi Karaoke",
                    "Duluth, GA",
                    "",
                    new Timestamp(futureDate1),
                    groupId.isEmpty() ? "1" : groupId
            );

            // Add another future event
            Date futureDate2 = new Date(now.getTime() + 172800000); // Day after tomorrow
            Hangout hangout2 = new Hangout(
                    "2",
                    "Stars and Strikes",
                    "Stone Mountain, GA",
                    "",
                    new Timestamp(futureDate2),
                    groupId.isEmpty() ? "1" : groupId
            );

            // Add a third future event
            Date futureDate3 = new Date(now.getTime() + 259200000); // Three days from now
            Hangout hangout3 = new Hangout(
                    "3",
                    "Julia's Birthday Dinner",
                    "Stone Mountain, GA",
                    "",
                    new Timestamp(futureDate3),
                    groupId.isEmpty() ? "2" : groupId
            );

            hangoutList.add(hangout1);
            hangoutList.add(hangout2);
            hangoutList.add(hangout3);
        } else {
            // For past tab
            // Add a past event
            Date pastDate1 = new Date(now.getTime() - 86400000); // Yesterday
            Hangout hangout1 = new Hangout(
                    "4",
                    "Pool Party",
                    "Duluth, GA 30096",
                    "",
                    new Timestamp(pastDate1),
                    groupId.isEmpty() ? "1" : groupId
            );

            // Add another past event
            Date pastDate2 = new Date(now.getTime() - 172800000); // Two days ago
            Hangout hangout2 = new Hangout(
                    "5",
                    "Movie Night",
                    "Atlanta, GA",
                    "",
                    new Timestamp(pastDate2),
                    groupId.isEmpty() ? "2" : groupId
            );

            hangoutList.add(hangout1);
            hangoutList.add(hangout2);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onHangoutClick(Hangout hangout) {
        Intent intent = new Intent(requireContext(), HangoutDetailActivity.class);
        intent.putExtra("HANGOUT_ID", hangout.getId());
        startActivity(intent);
    }

    @Override
    public void onCancelClick(Hangout hangout) {
        // TODO: Implement cancel functionality
        Toast.makeText(requireContext(), "Cancellation feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVoteClick(Hangout hangout) {
        // TODO: Implement voting functionality
        Toast.makeText(requireContext(), "Voting feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}