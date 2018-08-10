package com.pavelclaudiustefan.shadowapps.showstracker.ui.base;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

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

        adapter = getFragmentPagerAdapter();
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void dataChanged() {
        adapter.notifyDataSetChanged();
    }

    public abstract FragmentStatePagerAdapter getFragmentPagerAdapter();
}
