package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.EndlessScrollListener;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.recommendations.RecommendedMoviesList;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class MoviesDiscoverFragment extends Fragment {

    public static final String TAG = "MoviesDiscoverFragment";

    private static final String TOP_RATED_OPTION = "top_rated_option";
    private static final String POPULAR_OPTION = "popular_option";
    private static final String RECOMMENDED_OPTION = "recommended_option";
    private String option;

    private String tmdbUrl;

    private boolean isRecommended = false;

    // These fields are saved in the bundle in onSaveInstanceState(Bundle outState)
    // and restored in onCreateView
    private int currentPage = 1;    // The current page last loaded
    private int totalPages;         // Number of pages with movies
    private static ArrayList<Movie> movies = new ArrayList<>();

    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.search_fab)
    FloatingActionButton fab;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ShowItemListAdapter<Movie> movieItemListAdapter;

    private boolean showItemsInCollection;

    private Box<Movie> moviesBox;

    public MoviesDiscoverFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);

        ButterKnife.bind(this, rootView);
        fab.setVisibility(View.GONE);
        if (getActivity() != null) {
            // Used to retrieve tmdbIds for hiding collection movies
            moviesBox = ((MyApp)getActivity().getApplication()).getBoxStore().boxFor(Movie.class);
        }

        init();

        movieItemListAdapter = new ShowItemListAdapter<>(getActivity(), movies);
        //Log.i("ShadowDebug", "1 - I am visible");
        loadingIndicator.setVisibility(View.VISIBLE);
        if (isRecommended) {
            // Recommended movies section
            long[] tmdbIds = getTmdbIds();
            loadingIndicator.setIndeterminate(false);
            loadingIndicator.setMax(tmdbIds.length);
            RecommendedMoviesList recommendedMoviesList = new RecommendedMoviesList(this, tmdbIds, new MovieComparator(MovieComparator.BY_RATING, MovieComparator.DESCENDING));
            //movies = recommendedMoviesList.getList();
            //movieItemListAdapter.addAll(movies);
            //movieItemListAdapter = new ShowItemListAdapter<>(getContext(), movies);
            recommendedMoviesList.addRecommendedToAdapter(movieItemListAdapter);
        } else {
            // Popular movies / Top rated movies section
            setTmdbUrl();

            if (savedInstanceState != null) {
                movies = (ArrayList<Movie>) savedInstanceState.getSerializable("movies");
                currentPage = (int) savedInstanceState.getSerializable("currentPage");
                totalPages = (int) savedInstanceState.getSerializable("totalPages");
                //Log.i("ShadowDebug", "2 - I am gone");
                loadingIndicator.setVisibility(View.GONE);
            }

            if (movies.isEmpty()) {
                // Request movies only if savedInstanceState has none and the recommended option is not active
                requestAndAddMovies();
            }
        }

        setUpListView();

        return rootView;
    }

    private void setUpListView() {
        listView.setAdapter(movieItemListAdapter);

        //Only visible if no movies are found
        listView.setEmptyView(emptyStateTextView);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MovieActivityHTTP.class);
                Movie item = movies.get(position);
                intent.putExtra("tmdb_id", String.valueOf(item.getTmdbId()));
                startActivity(intent);
            }
        });

        if (!isRecommended) {
            listView.setOnScrollListener(new EndlessScrollListener(5, 1) {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {
                    //Log.i("ShadowDebug", "Why are you calling rn???");
                    return currentPage <= totalPages && loadMore();
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("movies", movies);
        outState.putSerializable("currentPage", currentPage);
        outState.putSerializable("totalPages", totalPages);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        AndroidNetworking.cancel(this);
    }

    // Requests movies and adds them to the movieItemListAdapter
    public void requestAndAddMovies() {
        String page = String.valueOf(currentPage);

        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", TmdbConstants.API_KEY);
        uriBuilder.appendQueryParameter("page", page);
        AndroidNetworking.get(uriBuilder.toString())
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .setAnalyticsListener(new AnalyticsListener() {
                    @Override
                    public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                        Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache + " currentPage: " + currentPage);
                    }
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //Log.i("ShadowDebug", "3 - I am gone");
                        loadingIndicator.setVisibility(View.GONE);

                        List<Movie> movies = QueryUtils.extractMoviesFromJson(response);

                        if (currentPage == 1) {
                            totalPages = QueryUtils.getTotalPagesFromJson(response);
                        }

                        if (movies != null && !movies.isEmpty()) {
                            if (!showItemsInCollection) {
                                removeCollectionMovies(movies);
                            }

                            movieItemListAdapter.addAll(movies);
                        } else {
                            displayPossibleError();
                            Log.e("ShadowDebug", "MoviesDiscoverFragment - No tvShows extracted from Json response");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayPossibleError();
                        Log.e("ShadowDebug", "MoviesDiscoverFragment onError() - " + anError.getErrorBody());
                    }
                });
    }

    private void removeCollectionMovies(List<Movie> movies) {
        long[] tmdbIds = getTmdbIds();
        ArrayList<Movie> itemsToDelete = new ArrayList<>();
        for (Movie item : movies) {
            long movieTmdbId = item.getTmdbId();
            for (long tmdbId : tmdbIds) {
                if (movieTmdbId == tmdbId) {
                    itemsToDelete.add(item);
                }
            }
        }
        movies.removeAll(itemsToDelete);
    }

    private long[] getTmdbIds() {
        return moviesBox.query().build().findIds();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.discover_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Set active option invisible
        switch (option) {
            case TOP_RATED_OPTION:
                MenuItem topRatedItem = menu.findItem(R.id.menu_show_top_rated);
                topRatedItem.setEnabled(false);
                break;
            case POPULAR_OPTION:
                MenuItem popularItem = menu.findItem(R.id.menu_show_popular);
                popularItem.setEnabled(false);
                break;
            case RECOMMENDED_OPTION:
                MenuItem recommendedItem = menu.findItem(R.id.menu_show_recommended);
                recommendedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesDiscoverFragment", "Filtering error");
                break;
        }
        if (showItemsInCollection) {
            MenuItem showWatchedItem = menu.findItem(R.id.show_hide_watched);
            showWatchedItem.setTitle(R.string.hide_collection_movies);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_top_rated:
                saveSettingsOption(TOP_RATED_OPTION);
                break;
            case R.id.menu_show_popular:
                saveSettingsOption(POPULAR_OPTION);
                break;
            case R.id.menu_show_recommended:
                saveSettingsOption(RECOMMENDED_OPTION);
                break;
            case R.id.show_hide_watched:
                saveSettingsShowHideWatched();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshList();
        return true;
    }

    private void saveSettingsOption(String option) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_discover_movies_option), option);
        editor.apply();
    }

    private void saveSettingsShowHideWatched() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(getString(R.string.settings_discover_movies_show_watched), !showItemsInCollection);
        editor.apply();
    }


    private boolean loadMore() {
        // TvShow loading indicator when searching for new temporarMovies
        //Log.i("ShadowDebug", "4 - I am visible");
        loadingIndicator.setVisibility(View.VISIBLE);

        ConnectivityManager connMgr = null;
        if (getActivity() != null) {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch more
        if (networkInfo != null && networkInfo.isConnected()) {
            currentPage++;
            requestAndAddMovies();
            return true;
        } else {
            return false;
        }
    }

    public void setTmdbUrl() {
        if (Objects.equals(option, TOP_RATED_OPTION)) {
            tmdbUrl = TmdbConstants.TOP_RATED_MOVIES_URL;
        } else if (Objects.equals(option, POPULAR_OPTION)) {
            tmdbUrl = TmdbConstants.POPULAR_MOVIES_URL;
        } else {
            tmdbUrl = null;
        }
    }

    public void setShowItemsInCollection(boolean showItemsInCollection) {
        this.showItemsInCollection = showItemsInCollection;
    }

    public void init()  {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        option = sharedPrefs.getString(
                getString(R.string.settings_discover_movies_option),
                getString(R.string.settings_discover_movies_option_default)
        );

        if (Objects.equals(option, "recommended_option")) {
            isRecommended = true;
        }

        boolean showItemsInCollection = sharedPrefs.getBoolean(
                getString(R.string.settings_discover_movies_show_watched),
                false
        );
        setShowItemsInCollection(showItemsInCollection);
    }

    public void refreshList() {
        if (getActivity() != null) {
            movieItemListAdapter.clear();
            currentPage = 1;
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }

    private void displayPossibleError() {
        // Default - Generic error - Set empty state text to display "No movies found." It's not visible if any Show is added to the adapter
        showEmptyStateTextView(R.string.no_movies_found);

        // Check if there is a more specific error
        if (getActivity() != null) {
            // Used to test internet connection
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr != null) {
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    // First, hide loading indicator so error message will be visible
                    //Log.i("ShadowDebug", "5 - I am gone");
                    loadingIndicator.setVisibility(View.GONE);
                    showEmptyStateTextView(R.string.no_internet_connection);
                }
            }
        }
    }

    private void showEmptyStateTextView(int resid) {
        emptyStateTextView.setText(resid);
    }

    // Called when list of recommended movies is received for one movie from collection
    public void onMoviesListIncremented() {
        loadingIndicator.incrementProgressBy(1);
    }

    public void onMoviesListLoaded() {
        //Log.i("ShadowDebug", "6 - I am gone");
        loadingIndicator.setVisibility(View.GONE);
        displayPossibleError();
    }
}