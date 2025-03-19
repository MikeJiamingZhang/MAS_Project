package com.example.mas_solution_2;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CommunityPlannerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}