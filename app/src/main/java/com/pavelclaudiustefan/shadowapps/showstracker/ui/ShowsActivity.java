package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.R;

public class ShowsActivity extends CategoryContainerActivity {

    @Override
    public void setContentViewLayout() {
        setContentView(R.layout.activity_shows);
        Log.i("Claudiu", "ShowsActivity");
    }
}
