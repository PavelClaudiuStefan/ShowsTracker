package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;

import java.util.ArrayList;
import java.util.List;

public class RecommendedMoviesListLoader extends AsyncTaskLoader<List<Movie>> {

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
    public List<Movie> loadInBackground() {
        if (tmdbIds == null) {
            return null;
        }

        return QueryUtils.fetchRecommendedMoviesData(tmdbIds);
    }
}
