package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.R;

public class MoviesActivity extends AppCompatActivity {

    MoviesCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        setTitle("Movies");

        ViewPager viewPager = findViewById(R.id.viewpager);
        adapter = new MoviesCategoryAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void dataChanged() {
        adapter.notifyDataSetChanged();
    }

}
