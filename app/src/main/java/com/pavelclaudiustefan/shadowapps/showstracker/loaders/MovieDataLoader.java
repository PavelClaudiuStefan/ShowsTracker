package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.content.AsyncTaskLoader;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;


public class MovieDataLoader extends AsyncTaskLoader<Movie> {

    private String url;

    public MovieDataLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public Movie loadInBackground() {
        if (url == null) {
            return null;
        }

        return QueryUtils.fetchMovieData(url);
    }
}
