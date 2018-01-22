package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MoviesCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.R;

public class MoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        ViewPager viewPager = findViewById(R.id.viewpager);
        MoviesCategoryAdapter adapter = new MoviesCategoryAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

}
