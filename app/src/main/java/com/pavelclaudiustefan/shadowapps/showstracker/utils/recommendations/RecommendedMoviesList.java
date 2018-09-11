package com.pavelclaudiustefan.shadowapps.showstracker.utils.recommendations;

import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesDiscoverFragment;

import java.util.ArrayList;
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
    public void sortItems(ArrayList<Movie> items) {
        Collections.sort(items, movieComparator);
    }

    @Override
    public List<Movie> extractShowsFromJsonResponse(String jsonResponse) {
        return QueryUtils.extractMoviesFromJson(jsonResponse);
    }

    @Override
    public void onDataIncremented() {
        mds.onMoviesListIncremented();
    }

    @Override
    public void onDataLoaded() {
        mds.onMoviesListLoaded();
    }


}
