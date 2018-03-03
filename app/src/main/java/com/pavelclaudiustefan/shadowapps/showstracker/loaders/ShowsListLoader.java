package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;

import java.util.List;

public class ShowsListLoader extends AsyncTaskLoader<List<VideoMainItem>> {

    private String url;

    public ShowsListLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<VideoMainItem> loadInBackground() {
        if (url == null) {
            return null;
        }

        return new QueryUtils().fetchShowsData(url);
    }

}