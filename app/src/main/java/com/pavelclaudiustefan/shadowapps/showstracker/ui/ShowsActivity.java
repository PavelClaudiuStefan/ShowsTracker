package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCategoryAdapter;

public class ShowsActivity extends VideoSectionsContainerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TV Shows");
    }

    @Override
    public void initAdapter() {
        adapter = new ShowsCategoryAdapter(this, getSupportFragmentManager());
    }
}
