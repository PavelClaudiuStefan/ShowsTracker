package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("ShadowDebug", "???");
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // User is logged in
            activityIntent = new Intent(SplashActivity.this, MoviesActivity.class);
            //activityIntent = new Intent(this, GroupsActivity.class);
        } else {
            // User is not logged in
            activityIntent = new Intent(this, LoginActivity.class);
        }

        // TODO - Check first if the movies stored in the objectbox db belong to the logged in firebase user
        // TODO - If not, delete the objectbox db, and remake it using data from firestore

        startActivity(activityIntent);
        finish();
    }

}
