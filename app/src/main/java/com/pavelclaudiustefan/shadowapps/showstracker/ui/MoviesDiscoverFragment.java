package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.RecommendedMoviesListLoader;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.TmdbIdsLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoviesDiscoverFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Movie>> {

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";
    private final static String TOP_RATED_URL = "https://api.themoviedb.org/3/movie/top_rated";
    private final static String POPULAR_URL = "https://api.themoviedb.org/3/discover/movie";
    private final static String RECOMMENDED_OPTION = "recommended_option";
    private String tmdbUrl;

    private boolean isRecommended = false;

    private int DATABASE_LOADER_ID = 0;
    private int HTTP_LOADER_ID = 1;
    private int currentPage = 1;

    private int totalPages;

    private View rootView;

    private static ArrayList<Movie> movies = new ArrayList<>();
    private ArrayList<Integer> tmdbIds;

    private MovieAdapter movieAdapter;

    private boolean isShowItemsInCollection;

    private TextView emptyStateTextView;
    private ListView movieListView;

    private Parcelable state;

    public MoviesDiscoverFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.movies_list, container, false);

        init();

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
            // Loads tmdbIds and then starts the movies loader
            new TmdbIdsLoader(this);

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

        if (!isRecommended) {
            movieListView.setOnScrollListener(new EndlessScrollListener(5, 1) {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {
                    if (page <= totalPages) {
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
                            loaderManager.initLoader(++HTTP_LOADER_ID, null, MoviesDiscoverFragment.this);
                            currentPage++;
                            return true;
                        } else {
                            loadingIndicator = rootView.findViewById(R.id.loading_indicator);
                            loadingIndicator.setVisibility(View.GONE);

                            emptyStateTextView.setText(R.string.no_internet_connection);
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            });
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

    private void init() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tmdbUrl = sharedPrefs.getString(
                getString(R.string.settings_discover_tmdb_url),
                getString(R.string.settings_discover_tmdb_url_default)
        );
        if (Objects.equals(tmdbUrl, "recommended_option")) {
            isRecommended = true;
        }

        isShowItemsInCollection = sharedPrefs.getBoolean(
                getString(R.string.settings_discover_show_watched),
                true
        );
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
        if (isRecommended) {
            return new RecommendedMoviesListLoader(getActivity(), tmdbIds);
        } else {
            String page = String.valueOf(currentPage);

            Uri baseUri = Uri.parse(tmdbUrl);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("api_key", API_KEY);
            uriBuilder.appendQueryParameter("page", page);

            return new MovieListLoader(getActivity(), uriBuilder.toString());
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        if (!isRecommended) {
            // Get total pages from the first movie
            if (currentPage == 1) {
                totalPages = movies.get(0).getTotalPages();
            }
        }

        // TODO - Find a better way to stop the adding of extra movies when returning from MovieActivity
        if (HTTP_LOADER_ID == loader.getId() && (loader.getId()-1) * 20 == movieAdapter.getCount()) {
            // Set empty state text to display "No temporarMovies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movies);

            // If isShowItemsInCollection = false -> hide watched
            if (!isShowItemsInCollection) {
                ArrayList<Movie> moviesToDelete = new ArrayList<>();
                for (Movie movie : movies) {
                    int movieTmdbId = movie.getTmdbId();
                    for (int tmdbId:tmdbIds) {
                        if (movieTmdbId == tmdbId) {
                            moviesToDelete.add(movie);
                        }
                    }
                }
                movies.removeAll(moviesToDelete);
            }

            if (movies != null && !movies.isEmpty()) {
                movieAdapter.addAll(movies);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.discover_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Set active option invisible
        switch (tmdbUrl) {
            case TOP_RATED_URL:
                MenuItem topRatedItem = menu.findItem(R.id.menu_show_top_rated);
                topRatedItem.setEnabled(false);
                break;
            case POPULAR_URL:
                MenuItem popularItem = menu.findItem(R.id.menu_show_popular);
                popularItem.setEnabled(false);
                break;
            case RECOMMENDED_OPTION:
                MenuItem recommendedItem = menu.findItem(R.id.menu_show_recommended);
                recommendedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Filtering error");
                break;
        }
        if (isShowItemsInCollection) {
            MenuItem showWatchedItem = menu.findItem(R.id.show_hide_watched);
            showWatchedItem.setTitle(R.string.hide_collection_movies);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_top_rated:
                saveSettingsTmdbUrl(TOP_RATED_URL);
                break;
            case R.id.menu_show_popular:
                saveSettingsTmdbUrl(POPULAR_URL);
                break;
            case R.id.menu_show_recommended:
                saveSettingsTmdbUrl(RECOMMENDED_OPTION);
                break;
            case R.id.show_hide_watched:
                saveSettingsShowHideWatched();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshMovieList();
        return true;
    }

    private void saveSettingsTmdbUrl(String url) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_discover_tmdb_url), url);
        editor.apply();
    }

    private void saveSettingsShowHideWatched() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(getString(R.string.settings_discover_show_watched), !isShowItemsInCollection);
        editor.apply();
    }

    private void refreshMovieList() {
        if (getActivity() != null) {
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }

    public void startLoader() {
        getLoaderManager().initLoader(HTTP_LOADER_ID, null, this);
    }

    public void setTmdbIds(ArrayList<Integer> tmdbIds) {
        this.tmdbIds = tmdbIds;
    }

}



