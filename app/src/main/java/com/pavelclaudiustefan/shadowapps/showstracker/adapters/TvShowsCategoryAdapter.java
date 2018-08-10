package com.pavelclaudiustefan.shadowapps.showstracker.adapters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowsAllFragment;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowsDiscoverFragment;

public class TvShowsCategoryAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public TvShowsCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TvShowsAllFragment();
        } else if (position == 1) {
            return new TvShowsAllFragment();
        } else if (position == 2) {
            return new TvShowsDiscoverFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.category_shows_to_watch);
        } else if (position == 1) {
            return context.getString(R.string.category_shows_all);
        } else {
            return context.getString(R.string.category_shows_discover);
        }
    }

    public int getItemPosition(@NonNull Object item) {
        return POSITION_NONE;
    }

}