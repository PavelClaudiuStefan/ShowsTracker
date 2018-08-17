package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MenuItem;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.base.ShowsSectionsContainerActivity;

import butterknife.BindView;

public class MoviesActivity extends ShowsSectionsContainerActivity {

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    public MoviesActivity() {
        setLayout(R.layout.activity_shows);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Movies");
        navigationView.getMenu().findItem(R.id.nav_movies).setChecked(true);
    }

    @Override
    public FragmentStatePagerAdapter getFragmentPagerAdapter() {
        return new MoviesCategoryAdapter(this, getSupportFragmentManager());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_movies) {
            closeDrawer();
            return true;
        } else {
            return super.onNavigationItemSelected(item);
        }
    }
}
