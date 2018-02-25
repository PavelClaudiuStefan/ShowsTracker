package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.R;

public class MoviesActivity extends CategoryContainerActivity {

    MoviesCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Movies");

        ViewPager viewPager = findViewById(R.id.viewpager);
        adapter = new MoviesCategoryAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void setContentViewLayout() {
        setContentView(R.layout.activity_movies);
        Log.i("Claudiu", "MoviesActivity");
    }

    public void dataChanged() {
        adapter.notifyDataSetChanged();
    }

}
