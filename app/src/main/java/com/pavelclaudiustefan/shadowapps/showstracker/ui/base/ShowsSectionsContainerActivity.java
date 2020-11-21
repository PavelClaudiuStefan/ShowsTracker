package com.pavelclaudiustefan.shadowapps.showstracker.ui.base;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.pavelclaudiustefan.shadowapps.showstracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class ShowsSectionsContainerActivity extends BaseActivity {

    FragmentStatePagerAdapter adapter;

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        adapter = getFragmentStatePagerAdapter();
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void dataChanged() {
        adapter.notifyDataSetChanged();
    }

    public abstract FragmentStatePagerAdapter getFragmentStatePagerAdapter();
}
