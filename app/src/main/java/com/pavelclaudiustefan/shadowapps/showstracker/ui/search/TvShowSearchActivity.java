package com.pavelclaudiustefan.shadowapps.showstracker.ui.search;

import android.content.Intent;
import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowActivityHTTP;

import java.util.List;

public class TvShowSearchActivity extends BaseSearchActivity<TvShow> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search TV Shows");
        setSearchViewQueryHint("Search new tv show...");
    }

    @Override
    public String getTmdbBaseSearchUrl() {
        return "https://api.themoviedb.org/3/search/tv";
    }

    @Override
    public List<TvShow> getShowsFromJsonResponse(String jsonResponse) {
        return QueryUtils.extractTvShowsFromJson(jsonResponse);
    }

    @Override
    public Intent getIntentForShowActivityHTTP() {
        return new Intent(this, TvShowActivityHTTP.class);
    }
}
