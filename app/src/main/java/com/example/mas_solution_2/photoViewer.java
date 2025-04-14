package com.example.mas_solution_2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// a view for a photo
public class photoViewer extends AppCompatActivity {
    String url;
    String hangoutId;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_photoview);
        ImageView frame = findViewById(R.id.photo);
        url = getIntent().getStringExtra("URL");
        hangoutId = getIntent().getStringExtra("HANGOUT_ID");
        Glide.with(this).load(url).into(frame);

        frame.setOnClickListener(v -> {finish();}); // short click just exists the view

        frame.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Delete this Photo?").setPositiveButton("Yes", (dialog, which) -> deletePhoto(url)).setNegativeButton("No", null).show();
            return true;
        });

    }

    private void deletePhoto(String url){
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        ref.delete().addOnSuccessListener(aVoid -> {
            // Remove from Firestore
            FirebaseFirestore.getInstance().collection("hangouts")
                    .document(hangoutId)
                    .update("photoUrls", FieldValue.arrayRemove(url))
                    .addOnSuccessListener(aVoid2 -> {
                        finish(); // close viewer
                    });
        });
    }

}
