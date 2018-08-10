package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MenuItem;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.TvShowsCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.base.ShowsSectionsContainerActivity;

public class TvShowsActivity extends ShowsSectionsContainerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TV Shows");
    }

    @Override
    public FragmentStatePagerAdapter getFragmentPagerAdapter() {
        return new TvShowsCategoryAdapter(this, getSupportFragmentManager());
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
