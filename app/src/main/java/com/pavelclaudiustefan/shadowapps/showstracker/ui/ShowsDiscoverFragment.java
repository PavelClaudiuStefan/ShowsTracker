package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.ShowsListLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowsDiscoverFragment extends DiscoverListFragment{

    public ShowsDiscoverFragment() {
        super();
        setTopRatedUrl("https://api.themoviedb.org/3/tv/top_rated");
        setPopularUrl("https://api.themoviedb.org/3/tv/popular");
    }

    @Override
    public void init()  {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String option = sharedPrefs.getString(
                getString(R.string.settings_discover_option),
                getString(R.string.settings_discover_option_default)
        );
        setOption(option);

        if (Objects.equals(option, "recommended_option")) {
            setRecommended(true);
        }

        boolean showItemsInCollection = sharedPrefs.getBoolean(
                getString(R.string.settings_discover_show_watched),
                true
        );
        setShowItemsInCollection(showItemsInCollection);
    }

    @Override
    public Loader<List<VideoMainItem>> recommendedList(ArrayList<Integer> tmdbIds) {
        // TODO
        return null;
    }

    @Override
    public Loader<List<VideoMainItem>> popularOrTopRatedList(String url) {
        return new ShowsListLoader(getActivity(), url);
    }

    @Override
    public void refreshList() {
        if (getActivity() != null) {
            ((ShowsActivity)getActivity()).dataChanged();
        }
    }

}
