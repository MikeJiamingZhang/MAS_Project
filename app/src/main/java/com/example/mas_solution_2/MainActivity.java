package com.example.mas_solution_2;

import android.content.Intent;
import android.os.Bundle;



import androidx.appcompat.app.AppCompatActivity;



import androidx.navigation.ui.AppBarConfiguration;


import com.example.mas_solution_2.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // log in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // if user not signed in, then start logging in process
        if(user == null){
            //Toast.makeText(this, "Go to sign-ins...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, authentication.class);
            startActivity(intent);
        }
    }
}