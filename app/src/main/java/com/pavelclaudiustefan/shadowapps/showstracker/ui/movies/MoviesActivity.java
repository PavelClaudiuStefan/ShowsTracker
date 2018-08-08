package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.VideoSectionsContainerActivity;

public class MoviesActivity extends VideoSectionsContainerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Movies");
    }

    @Override
    public void initAdapter() {
        setAdapter(new MoviesCategoryAdapter(this, getSupportFragmentManager()));
    }

}
