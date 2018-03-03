package com.pavelclaudiustefan.shadowapps.showstracker.ui;

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

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowCursorAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.ShowEntry;

public class ShowsAllFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private View rootView;

    private TextView emptyStateTextView;

    private static int SHOW_LOADER = 0;

    ShowCursorAdapter showCursorAdapter;

    private String selection = null;
    private String[] selectionArgs = null;
    private String sortOrder = null;

    public ShowsAllFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.category_list, container, false);

        ListView showListView = rootView.findViewById(R.id.list);

        //Only visible if no movies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        showListView.setEmptyView(emptyStateTextView);

        showCursorAdapter = new ShowCursorAdapter(getContext(), null);
        showListView.setAdapter(showCursorAdapter);

        // Setup the item click listener
        showListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO
                Intent intent = new Intent(getActivity(), MovieActivitySQL.class);

                Uri currentMovieUri = ContentUris.withAppendedId(ShowEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentMovieUri);

                // Launch the {@link EditorActivity} to display the data for the current movie.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(SHOW_LOADER, null, this);

        FloatingActionButton searchFab = rootView.findViewById(R.id.search_fab);
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshShowsList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ShowEntry._ID,
                ShowEntry.TMDB_ID,
                ShowEntry.COLUMN_SHOW_TITLE,
                ShowEntry.COLUMN_SHOW_AVERAGE_VOTE,
                ShowEntry.COLUMN_SHOW_RELEASE_DATE_IN_MILLISECONDS,
                ShowEntry.COLUMN_SHOW_THUMBNAIL_URL,};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getContext(),   // Parent activity context
                ShowEntry.CONTENT_URI,         // Provider content URI to query
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

        // Set empty state text to display "No shows found." It's not visible if any movie is added to the adapter
        emptyStateTextView.setText(R.string.no_shows);

        showCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        showCursorAdapter.swapCursor(null);
    }

    public void refreshShowsList() {
        if (getActivity() != null) {
            ((ShowsActivity)getActivity()).dataChanged();
        }
    }
}
