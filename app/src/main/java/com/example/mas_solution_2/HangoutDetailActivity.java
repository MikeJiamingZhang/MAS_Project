package com.example.mas_solution_2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HangoutDetailActivity extends AppCompatActivity implements PhotoAdapter.PhotoClickListener {

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private String hangoutId;
    private Hangout hangout;

    private ImageView hangoutImageView;
    private TextView hangoutTitleView;
    private TextView hangoutDateView;
    private TextView hangoutLocationView;
    private TextView participantsCountView;
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<String> photoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangout_detail);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // If user not signed in, redirect to authentication
        if (currentUser == null) {
            startActivity(new Intent(this, authentication.class));
            finish();
            return;
        }

        // Get hangout ID from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hangoutId = extras.getString("HANGOUT_ID", "");
        }

        if (hangoutId.isEmpty()) {
            Toast.makeText(this, "Hangout ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("");  // We'll set the title later

        hangoutImageView = findViewById(R.id.hangout_image);
        hangoutTitleView = findViewById(R.id.hangout_title);
        hangoutDateView = findViewById(R.id.hangout_date);
        hangoutLocationView = findViewById(R.id.hangout_location);
        participantsCountView = findViewById(R.id.participants_count);

        // Setup photos recycler view
        photosRecyclerView = findViewById(R.id.recycler_photos);
        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, photoList, this);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photosRecyclerView.setAdapter(photoAdapter);

        // Add photos button click handler
        Button addPhotoButton = findViewById(R.id.btn_add_photo);
        addPhotoButton.setOnClickListener(v -> {
            // Toast.makeText(this, "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show();
            addPhoto();
        });

        // Group chat button click handler
        Button groupChatButton = findViewById(R.id.btn_group_chat);
        groupChatButton.setOnClickListener(v -> {
            // Fixed to pass both hangout ID and name
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("HANGOUT_ID", hangoutId);
            if (hangout != null) {
                intent.putExtra("GROUP_NAME", hangout.getName() + " Chat");
            }
            startActivity(intent);
        });

        // FAB for directions
        FloatingActionButton fabDirections = findViewById(R.id.fab_directions);
        fabDirections.setOnClickListener(v -> {
            if (hangout != null && hangout.getLocation() != null && !hangout.getLocation().isEmpty()) {
                // Open Google Maps with the location
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(hangout.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load hangout data
        loadHangout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWhenReturned();

    }

    private void loadHangout() {
        // For demo purposes
        /*
        if (hangoutId.equals("4")) {
            // Pool party demo
            Date pastDate = new Date(System.currentTimeMillis() - 86400000); // Yesterday
            hangout = new Hangout(
                    "4",
                    "Pool Party",
                    "2550 Pleasant Hill Rd Suite #205, Duluth, GA 30096",
                    "",
                    new Timestamp(pastDate),
                    "1"
            );
            hangout.addParticipant("user1");
            hangout.addParticipant("user2");
            hangout.addParticipant("user3");
            hangout.addParticipant("user4");
            hangout.addParticipant("user5");

            updateUI();
            return;
        }
        */

        firestore.collection("hangouts").document(hangoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hangout = documentSnapshot.toObject(Hangout.class);
                        hangout.setId(documentSnapshot.getId());

                        // Load photos if any
                        if (hangout.getPhotoUrls() != null && !hangout.getPhotoUrls().isEmpty()) {
                            photoList.clear();
                            photoList.addAll(hangout.getPhotoUrls());
                            photoAdapter.notifyDataSetChanged();
                        } else {
                            photoList.clear();
                            photoAdapter.notifyDataSetChanged();
                        }

                        updateUI();
                    } else {
                        Toast.makeText(this, "Hangout not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading hangout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        if (hangout == null) return;

        // Set the toolbar title
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(hangout.getName());

        // Set hangout title
        hangoutTitleView.setText(hangout.getName());

        // Set date text
        Timestamp timestamp = hangout.getDate();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            boolean isPast = date.before(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String dateStr = dateFormat.format(date);

            if (isPast) {
                hangoutDateView.setText("Happened " + dateStr);
            } else {
                hangoutDateView.setText("Happening " + dateStr);
            }
        }

        // Set location
        hangoutLocationView.setText(hangout.getLocation());

        // Set participants count
        int participantCount = hangout.getParticipants() != null ? hangout.getParticipants().size() : 0;
        participantsCountView.setText(participantCount + " participants");

        // Load the hangout image
        if (hangout.getImageUrl() != null && !hangout.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(hangout.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(hangoutImageView);
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

    @Override
    public void onPhotoClick(String photoUrl, int position) {
        if (position == 0 && photoUrl.equals("add")) {
            // Handle the "add photo" action

        } else {
            //Toast.makeText(this, "Photo viewer coming soon!", Toast.LENGTH_SHORT).show();
            // start a new intent of a enlarged photoViewer
            Intent intent = new Intent(this, photoViewer.class);
            intent.putExtra("URL", photoUrl);
            intent.putExtra("HANGOUT_ID", hangoutId);
            startActivity(intent);
        }
    }

    public void addPhoto() {
        // check permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            return;
        } else {
            //Toast.makeText(getApplicationContext(), "got permission", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setType("image/*"); // images only
            intent.setAction(Intent.ACTION_GET_CONTENT); // get stuff from storage
            startActivityForResult(Intent.createChooser(intent, "Select a Picture!"), 1002);
        }
    }

    @Override
    // The file that actually ads photo to firebase
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            Uri imageuri = data.getData();
            String fileName = String.valueOf(System.currentTimeMillis());
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("hangout_photos/" + hangoutId + "/" + fileName);
            // we put file into the storage. Step after step, in case of race condition
            storageRef.putFile(imageuri)
                    .addOnSuccessListener(getURL ->
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> { // get url of it
                                firestore.collection("hangouts").document(hangoutId).update("photoUrls", FieldValue.arrayUnion(uri.toString())).addOnSuccessListener(putUrl -> {
                                            photoList.add(uri.toString()); // put the url of the storage file into firestore under the hangouts
                                            photoAdapter.notifyDataSetChanged(); // update the recyclerView
                                });
                            })
                    );
        }
    }

    // Re-fetch photoList from firestore
    private void refreshWhenReturned() {
        firestore.collection("hangouts").document(hangoutId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.toObject(Hangout.class) != null && documentSnapshot.toObject(Hangout.class).getPhotoUrls() != null) {
                    photoList.clear();
                    photoList.addAll(documentSnapshot.toObject(Hangout.class).getPhotoUrls());
                    photoAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}