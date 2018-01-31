package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MovieAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.EndlessScrollListener;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.MovieListLoader;

import java.util.ArrayList;
import java.util.List;

public class MoviesPopularFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Movie>> {

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static int DATABASE_LOADER_ID = 0;

    private int MOVIES_LOADER_CURRENT_PAGE_ID = 1;

    private View rootView;

    private static ArrayList<Movie> movies = new ArrayList<>();

    private MovieAdapter movieAdapter;

    private TextView emptyStateTextView;

    private ListView movieListView;

    private Parcelable state;

    public MoviesPopularFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.movies_list, container, false);

        FloatingActionButton fab = rootView.findViewById(R.id.search_movie_fab);
        fab.setVisibility(View.GONE);

        movieListView = rootView.findViewById(R.id.list);

        //Only visible if no temporarMovies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        movieListView.setEmptyView(emptyStateTextView);

        movieAdapter = new MovieAdapter(getActivity(), movies);
        movieListView.setAdapter(movieAdapter);

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(MOVIES_LOADER_CURRENT_PAGE_ID, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Setup the item click listener
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link MovieActivityHTTP}
                Intent intent = new Intent(getActivity(), MovieActivityHTTP.class);
                Movie movie = movies.get(position);
                intent.putExtra("tmdb_id", String.valueOf(movie.getTmdbId()));
                startActivity(intent);
            }
        });

        movieListView.setOnScrollListener(new EndlessScrollListener(5, 1) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Show loading indicator when searching for new temporarMovies
                View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
                loadingIndicator.setVisibility(View.VISIBLE);

                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (connMgr != null) {
                    networkInfo = connMgr.getActiveNetworkInfo();
                }

                // If there is a network connection, fetch more
                if (networkInfo != null && networkInfo.isConnected()) {
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(++MOVIES_LOADER_CURRENT_PAGE_ID, null, MoviesPopularFragment.this);
                    return true;
                } else {
                    loadingIndicator = rootView.findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);

                    emptyStateTextView.setText(R.string.no_internet_connection);
                    return false;
                }
            }
        });

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.discover_menu,menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        state = movieListView.onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(state != null) {
            movieListView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        String page = String.valueOf(MOVIES_LOADER_CURRENT_PAGE_ID);

        String tmdbUrl = "https://api.themoviedb.org/3/discover/movie";
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("page", page);

        return new MovieListLoader(getActivity(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // TODO - Find a better way to stop the adding of extra movies when returning from MovieActivity
        if (MOVIES_LOADER_CURRENT_PAGE_ID == loader.getId() && (loader.getId()-1) * 20 == movieAdapter.getCount()) {
            // Set empty state text to display "No temporarMovies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movies);

            if (movies != null && !movies.isEmpty()) {
                movieAdapter.addAll(movies);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    private void refreshMovieList() {
        if (getActivity() != null) {
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }
}
