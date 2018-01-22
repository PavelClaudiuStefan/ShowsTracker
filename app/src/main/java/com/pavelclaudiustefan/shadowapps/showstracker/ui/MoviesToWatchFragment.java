package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;

public class MoviesToWatchFragment extends MoviesFragment {

    public MoviesToWatchFragment() {

        String selection = MovieEntry.COLUMN_MOVIE_WATCHED + "=? AND " +
                           MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + "<?";

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {"0", String.valueOf(todayInMilliseconds)};

        setSelection(selection);
        setSelectionArgs(selectionArgs);
    }

}
