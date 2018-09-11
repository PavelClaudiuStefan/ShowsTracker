package com.pavelclaudiustefan.shadowapps.showstracker.utils.recommendations;

import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.TvShowComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowsDiscoverFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendedTvShowsList extends RecommendedShowsList<TvShow> {

    private TvShowsDiscoverFragment tsds;
    private TvShowComparator tvShowComparator;

    public RecommendedTvShowsList(TvShowsDiscoverFragment tsds, long[] tmdbIds, TvShowComparator tvShowComparator) {
        super(TmdbConstants.TV_SHOWS_URL, tmdbIds);
        this.tsds = tsds;
        this.tvShowComparator = tvShowComparator;
    }

    @Override
    public void sortItems(ArrayList<TvShow> items) {
        Collections.sort(items, tvShowComparator);
    }

    @Override
    public List<TvShow> extractShowsFromJsonResponse(String jsonResponse) {
        return QueryUtils.extractTvShowsFromJson(jsonResponse);
    }

    @Override
    public void onDataIncremented() {
        tsds.onTvShowsIncremented();
    }

    @Override
    public void onDataLoaded() {
        tsds.onTvShowsListLoaded();
    }


}
