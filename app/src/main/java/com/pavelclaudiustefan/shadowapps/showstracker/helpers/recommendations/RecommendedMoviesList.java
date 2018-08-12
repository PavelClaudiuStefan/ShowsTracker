package com.pavelclaudiustefan.shadowapps.showstracker.helpers.recommendations;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesDiscoverFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecommendedMoviesList extends RecommendedShowsList<Movie> {

    private MoviesDiscoverFragment mds;
    private MovieComparator movieComparator;

    public RecommendedMoviesList(MoviesDiscoverFragment mds, long[] tmdbIds, MovieComparator movieComparator) {
        super(TmdbConstants.MOVIES_URL, tmdbIds);
        this.mds = mds;
        this.movieComparator = movieComparator;
    }

    @Override
    public ArrayList<Movie> sortItems(ArrayList<Movie> items) {
        Collections.sort(items, movieComparator);
        return items;
    }

    @Override
    public List<Movie> extractShowsFromJsonResponse(String jsonResponse) {
        return QueryUtils.extractMoviesFromJson(jsonResponse);
    }

    @Override
    public void onDataLoaded() {
        mds.onMoviesListLoaded();
    }


}
