package com.pavelclaudiustefan.shadowapps.showstracker.adapters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.ShowsAllFragment;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.ShowsDiscoverFragment;

public class ShowsCategoryAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public ShowsCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ShowsAllFragment();
        } else if (position == 1) {
            return new ShowsAllFragment();
        } else if (position == 2) {
            return new ShowsAllFragment();
        } else if (position == 3) {
            return new ShowsDiscoverFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.category_shows_to_watch);
        } else if (position == 1) {
            return context.getString(R.string.category_shows_upcoming);
        } else if (position == 2) {
            return context.getString(R.string.category_shows_all);
        } else {
            return context.getString(R.string.category_shows_discover);
        }
    }

    public int getItemPosition(@NonNull Object item) {
        return POSITION_NONE;
    }

}