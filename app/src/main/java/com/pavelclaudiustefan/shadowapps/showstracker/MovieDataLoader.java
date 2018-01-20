package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;


public class MovieDataLoader extends AsyncTaskLoader<Movie> {

    private String url;

    MovieDataLoader(Context context, String url) {
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
