package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    private String url;

    MovieLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {
        if (url == null) {
            return null;
        }

        return QueryUtils.fetchMovieData(url);
    }
}
