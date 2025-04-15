package com.example.mas_solution_2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

//referencing https://firebase.google.com/docs/auth/android/firebaseui#java
public class authentication extends AppCompatActivity {

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result){
        IdpResponse response = result.getIdpResponse();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // If good password
        if(result.getResultCode() == RESULT_OK && user != null){
            Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show();

            // Store user information in Firestore
            String userId = user.getUid();
            String userEmail = user.getEmail();
            String displayName = user.getDisplayName();

            if (userEmail != null && !userEmail.isEmpty()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("email", userEmail);
                userData.put("displayName", displayName);

                // Save to Firestore
                FirebaseFirestore.getInstance().collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            // Continue to GroupsActivity after saving user data
                            Intent intent = new Intent(this, GroupsActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Even if saving fails, continue to GroupsActivity
                            Intent intent = new Intent(this, GroupsActivity.class);
                            startActivity(intent);
                            finish();
                        });
            } else {
                // If no email, just continue to GroupsActivity
                Intent intent = new Intent(this, GroupsActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else{
            // Putout a text
            Toast.makeText(getApplicationContext(), "Sign-in failed, please try again!!", Toast.LENGTH_LONG).show();
        }
    }

    // signin launcher
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    // Creates the sign in page when signing in
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User already logged in, go directly to GroupsActivity
            startActivity(new Intent(this, GroupsActivity.class));
            finish();
            return;
        }

        //Toast.makeText(getApplicationContext(), "check", Toast.LENGTH_LONG).show();
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }
}