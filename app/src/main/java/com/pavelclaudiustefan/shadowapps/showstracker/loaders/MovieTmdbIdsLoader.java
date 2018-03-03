package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.MoviesDiscoverFragment;

import java.util.ArrayList;

public class MovieTmdbIdsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private MoviesDiscoverFragment moviesDiscoverFragment;

    public MovieTmdbIdsLoader(MoviesDiscoverFragment moviesDiscoverFragment) {
        this.moviesDiscoverFragment = moviesDiscoverFragment;
        moviesDiscoverFragment.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                VideoItemContract.MovieEntry._ID,
                VideoItemContract.MovieEntry.TMDB_ID};

        if (moviesDiscoverFragment.getContext() != null) {
            return new CursorLoader(moviesDiscoverFragment.getContext(),
                    VideoItemContract.MovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        } else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ArrayList<Integer> tmdbIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            int tmdbIdColumnIndex = cursor.getColumnIndex(VideoItemContract.MovieEntry.TMDB_ID);
            int tmdbId = cursor.getInt(tmdbIdColumnIndex);
            tmdbIds.add(tmdbId);
        }

        moviesDiscoverFragment.setTmdbIds(tmdbIds);
        moviesDiscoverFragment.startLoader();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
