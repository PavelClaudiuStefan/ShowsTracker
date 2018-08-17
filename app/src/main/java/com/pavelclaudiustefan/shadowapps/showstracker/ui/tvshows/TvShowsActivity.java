package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MenuItem;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.TvShowsCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.base.ShowsSectionsContainerActivity;

import butterknife.BindView;

public class TvShowsActivity extends ShowsSectionsContainerActivity {

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    public TvShowsActivity() {
        setLayout(R.layout.activity_shows);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TV Shows");
        navigationView.getMenu().findItem(R.id.nav_tv_shows).setChecked(true);
    }

    @Override
    public FragmentStatePagerAdapter getFragmentPagerAdapter() {
        return new TvShowsCategoryAdapter(this, getSupportFragmentManager());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_tv_shows) {
            closeDrawer();
            return true;
        } else {
            return super.onNavigationItemSelected(item);
        }
    }
}
