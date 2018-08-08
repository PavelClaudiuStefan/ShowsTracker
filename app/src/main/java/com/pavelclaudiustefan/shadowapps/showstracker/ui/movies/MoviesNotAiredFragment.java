package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract;

public class MoviesNotAiredFragment extends MoviesFragment {

    public MoviesNotAiredFragment() {

        String selection = VideoItemContract.MovieEntry.MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS + ">=?";
        setSelection(selection);

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};
        setSelectionArgs(selectionArgs);
    }

}