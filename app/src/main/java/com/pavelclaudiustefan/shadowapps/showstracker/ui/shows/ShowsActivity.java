package com.pavelclaudiustefan.shadowapps.showstracker.ui.shows;

import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.VideoSectionsContainerActivity;

public class ShowsActivity extends VideoSectionsContainerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TV Shows");
    }

    @Override
    public void initAdapter() {
        setAdapter(new ShowsCategoryAdapter(this, getSupportFragmentManager()));
    }
}
