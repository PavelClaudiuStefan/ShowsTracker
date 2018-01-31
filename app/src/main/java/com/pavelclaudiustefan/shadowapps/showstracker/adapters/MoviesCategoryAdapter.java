package com.pavelclaudiustefan.shadowapps.showstracker.adapters;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.MoviesAllFragment;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.MoviesPopularFragment;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.MoviesToWatchFragment;

public class MoviesCategoryAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public MoviesCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new MoviesToWatchFragment();
        } else if (position == 1) {
            return new MoviesAllFragment();
        } else {
            return new MoviesPopularFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
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