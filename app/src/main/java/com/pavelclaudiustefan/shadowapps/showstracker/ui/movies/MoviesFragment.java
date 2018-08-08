package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MovieCursorAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.MovieEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.SearchActivity;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private View rootView;

    private boolean isFabVisible = false;

    private TextView emptyStateTextView;

    private static int MOVIE_LOADER = 0;

    MovieCursorAdapter movieCursorAdapter;

    private String selection = null;
    private String[] selectionArgs = null;
    private String sortOrder = null;

    public MoviesFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.category_list, container, false);

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
                // Create new intent to go to {@link MovieActivitySQL}
                Intent intent = new Intent(getActivity(), MovieActivitySQL.class);

                Uri currentMovieUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentMovieUri);

                // Launch the {@link EditorActivity} to display the data for the current movie.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);

        FloatingActionButton searchFab = rootView.findViewById(R.id.search_fab);
        if (isFabVisible) {
            searchFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            searchFab.setVisibility(View.GONE);
        }

        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMovieList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.TMDB_ID,
                MovieEntry.MOVIE_TITLE,
                MovieEntry.MOVIE_AVERAGE_VOTE,
                MovieEntry.MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS,
                MovieEntry.MOVIE_IMAGE_ID};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getContext(),   // Parent activity context
                MovieEntry.CONTENT_URI,         // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
        emptyStateTextView.setText(R.string.no_movies);

        movieCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieCursorAdapter.swapCursor(null);
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSearchFabVisibility(boolean value) {
        isFabVisible = value;
    }

    public void refreshMovieList() {
        if (getActivity() != null) {
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }
}
