package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;

public class MoviesNotAiredFragment extends MoviesFragment {

    public MoviesNotAiredFragment() {

        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS + ">=?";
        setSelection(selection);

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};
        setSelectionArgs(selectionArgs);
    }

}
