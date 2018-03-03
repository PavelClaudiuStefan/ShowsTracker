package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.content.Context;

import com.pavelclaudiustefan.shadowapps.showstracker.fetchers.MovieDataFetcher;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;

import java.util.List;

public class MovieListLoader extends VideoMainItemListLoader {

    public MovieListLoader(Context context, String url) {
        super(context, url);
    }

    @Override
    public List<VideoMainItem> moviesList(String url) {
        return new MovieDataFetcher().fetchData(url);
    }
}
