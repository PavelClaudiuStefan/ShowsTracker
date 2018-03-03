package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;

public class MoviesActivity extends VideoSectionsContainerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Movies");
    }

    @Override
    public void initAdapter() {
        adapter = new MoviesCategoryAdapter(this, getSupportFragmentManager());
    }

}
