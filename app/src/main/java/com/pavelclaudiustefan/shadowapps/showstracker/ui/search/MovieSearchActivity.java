package com.pavelclaudiustefan.shadowapps.showstracker.ui.search;

import android.content.Intent;
import android.os.Bundle;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MovieActivityHTTP;

import java.util.List;

public class MovieSearchActivity extends BaseSearchActivity<Movie> {

    public MovieSearchActivity() {
        setMenuResId(R.menu.menu_movies_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search Movies");
        setSearchViewQueryHint("Search new movie...");
    }

    @Override
    public String getTmdbBaseSearchUrl() {
        return "https://api.themoviedb.org/3/search/movie";
    }

    @Override
    public List<Movie> getShowsFromJsonResponse(String jsonResponse) {
        return QueryUtils.extractMoviesFromJson(jsonResponse);
    }

    @Override
    public Intent getIntentForShowActivityHTTP() {
        return new Intent(this, MovieActivityHTTP.class);
    }

}
