package com.pavelclaudiustefan.shadowapps.showstracker.loaders;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.DiscoverListFragment;

import java.util.ArrayList;

public class TmdbIdsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private DiscoverListFragment dlf;

    public TmdbIdsLoader(DiscoverListFragment dlf) {
        this.dlf = dlf;
        dlf.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.TMDB_ID};

        if (dlf.getContext() != null) {
            return new CursorLoader(dlf.getContext(),
                    MovieContract.MovieEntry.CONTENT_URI,
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
            int tmdbIdColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.TMDB_ID);
            int tmdbId = cursor.getInt(tmdbIdColumnIndex);
            tmdbIds.add(tmdbId);
        }

        dlf.setTmdbIds(tmdbIds);
        dlf.startLoader();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
