package com.pavelclaudiustefan.shadowapps.showstracker.ui.shows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_shows) {
            closeDrawer();
            return true;
        } else {
            return super.onNavigationItemSelected(item);
        }
    }
}
