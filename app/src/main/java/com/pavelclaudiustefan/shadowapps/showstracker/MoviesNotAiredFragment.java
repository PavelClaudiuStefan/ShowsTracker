package com.pavelclaudiustefan.shadowapps.showstracker;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;

public class MoviesNotAiredFragment extends MoviesFragment {

    public MoviesNotAiredFragment() {

        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + ">=?";
        setSelection(selection);

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};
        setSelectionArgs(selectionArgs);
    }

}
