package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;

import java.util.List;

public abstract class VideoMainItemListLoader extends AsyncTaskLoader<List<VideoMainItem>> {

    private String url;

    public VideoMainItemListLoader(Context context, String url) {
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

        return moviesList(url);
    }

    public abstract List<VideoMainItem> moviesList(String url);
}
