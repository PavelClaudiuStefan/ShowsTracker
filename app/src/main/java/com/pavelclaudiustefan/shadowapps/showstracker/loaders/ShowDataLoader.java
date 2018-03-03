package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Show;


public class ShowDataLoader extends AsyncTaskLoader<Show> {

    private String url;

    public ShowDataLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public Show loadInBackground() {
        if (url == null) {
            return null;
        }

        return QueryUtils.fetchShowData(url);
    }
}
