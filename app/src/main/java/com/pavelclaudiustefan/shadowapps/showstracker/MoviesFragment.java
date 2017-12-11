package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private View rootView;

    private TextView emptyStateTextView;

    private static final int MOVIE_LOADER = 0;

    MovieCursorAdapter movieCursorAdapter;

    public MoviesFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.movies_list, container, false);

        ListView movieListView = rootView.findViewById(R.id.list);

        //Only visible if no movies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        movieListView.setEmptyView(emptyStateTextView);

        movieCursorAdapter = new MovieCursorAdapter(getContext(), null);
        movieListView.setAdapter(movieCursorAdapter);

        // Setup the item click listener
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(getActivity(), MovieActivity.class);

                Uri currentMovieUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentMovieUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.COLUMN_MOVIE_TITLE,
                MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE,
                MovieEntry.COLUMN_MOVIE_RELEASE_DATE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getContext(),   // Parent activity context
                MovieEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found." It's not visible if any movie is added to the adapter
        emptyStateTextView.setText(R.string.no_movies);

        movieCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieCursorAdapter.swapCursor(null);
    }
}
