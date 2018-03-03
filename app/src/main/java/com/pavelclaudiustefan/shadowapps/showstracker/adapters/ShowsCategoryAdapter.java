package com.pavelclaudiustefan.shadowapps.showstracker.adapters;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
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
            return new ShowsDiscoverFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.category_movies_to_watch);
        } else if (position == 1) {
            return context.getString(R.string.category_movies_all);
        } else {
            return context.getString(R.string.category_movies_discover);
        }
    }

    public int getItemPosition(Object item) {
        return POSITION_NONE;
    }

}