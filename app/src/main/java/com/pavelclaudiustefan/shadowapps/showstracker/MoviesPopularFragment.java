package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;

import java.util.ArrayList;
import java.util.List;

public class MoviesPopularFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Movie>> {

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static String TMDB_URL = "https://api.themoviedb.org/3/discover/movie";

    private View rootView;

    private ArrayList<Movie> movies = new ArrayList<>();

    private MovieAdapter movieAdapter;

    private TextView emptyStateTextView;

    public MoviesPopularFragment() {
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

        movieAdapter = new MovieAdapter(getActivity(), movies);
        movieListView.setAdapter(movieAdapter);

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(1, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = movies.get(position);
                insertMovie(movie);
            }
        });

        return rootView;
    }

    private void insertMovie(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        values.put(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE, movie.getVote());
        values.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getDate());
        values.put(MovieEntry.COLUMN_MOVIE_IMDB_ID, movie.getImdbUrl());
        values.put(MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL, movie.getThumbnailUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_URL, movie.getImageUrl());

        Uri newUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, values);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sortBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_sort_by_default)
        );

        Uri baseUri = Uri.parse(TMDB_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("sort_by", sortBy);

        return new MovieLoader(getActivity(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
        emptyStateTextView.setText(R.string.no_movies);

        movieAdapter.clear();

        if (movies != null && !movies.isEmpty()) {
            movieAdapter.addAll(movies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }
}
