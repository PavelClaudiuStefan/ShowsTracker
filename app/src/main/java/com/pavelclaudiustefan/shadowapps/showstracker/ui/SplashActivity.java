package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.groups.GroupsActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowsActivity;

public class SplashActivity extends AppCompatActivity {

    public static final String MOVIES_SECTION = "movies";
    public static final String TV_SHOWS_SECTION = "tv_shows";
    public static final String GROUPS_SECTION = "groups";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // User is logged in
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String startingSectionValue = prefs.getString("starting_section", "movies");

            switch (startingSectionValue) {
                case MOVIES_SECTION:
                    activityIntent = new Intent(SplashActivity.this, MoviesActivity.class);
                    break;
                case TV_SHOWS_SECTION:
                    activityIntent = new Intent(SplashActivity.this, TvShowsActivity.class);
                    break;
                case GROUPS_SECTION:
                    activityIntent = new Intent(SplashActivity.this, GroupsActivity.class);
                    break;
                default:
                    activityIntent = new Intent(SplashActivity.this, MoviesActivity.class);
                    break;
            }
        } else {
            // User is not logged in
            activityIntent = new Intent(this, LoginActivity.class);
        }

        // TODO - Check first if the movies stored in the objectbox db belong to the logged in firebase user
        // TODO - lastUpdated value stored locally and on server -> To see which data is more fresh
        // lastUpdatedByUser - date when user added or removed
        // lastUpdatedAuto - date when data is updated (titles, new seasons, etc.)
        // TODO - If not, delete the objectbox db, and remake it using data from firestore

        startActivity(activityIntent);
        finish();
    }

}
