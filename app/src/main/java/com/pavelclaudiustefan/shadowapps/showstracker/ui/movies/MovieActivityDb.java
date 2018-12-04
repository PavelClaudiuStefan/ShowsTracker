package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;

public class MovieActivityDb extends MovieActivity {

    @Override
    void requestAndDisplayMovie(long tmdbId) {
        Movie movie = getMovieFromDb(tmdbId);
        if (movie != null) {
            setInUserCollection(true);
            displayMovie(movie);
        } else {
            displayError();
        }
    }
}
