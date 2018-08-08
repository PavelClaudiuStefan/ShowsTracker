package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.VideoMainItem;

import java.util.ArrayList;
import java.util.List;

public class RecommendedMoviesListLoader extends AsyncTaskLoader<List<VideoMainItem>> {

    private ArrayList<Integer> tmdbIds;

    public RecommendedMoviesListLoader(Context context, ArrayList<Integer> tmdbIds) {
        super(context);
        this.tmdbIds = tmdbIds;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<VideoMainItem> loadInBackground() {
        if (tmdbIds == null) {
            return null;
        }

        return QueryUtils.fetchRecommendedMoviesData(tmdbIds);
    }
}
