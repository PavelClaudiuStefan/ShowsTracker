package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;

import com.pavelclaudiustefan.shadowapps.showstracker.fetchers.ShowDataFetcher;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;

import java.util.List;

public class ShowsListLoader extends VideoMainItemListLoader {

    public ShowsListLoader(Context context, String url) {
        super(context, url);
    }

    @Override
    public List<VideoMainItem> moviesList(String url) {
        return new ShowDataFetcher().fetchData(url);
    }
}
