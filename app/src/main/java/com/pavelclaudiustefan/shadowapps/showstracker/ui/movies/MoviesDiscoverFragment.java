package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.search.MovieSearchActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.recommendations.RecommendedMoviesList;

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
    private boolean isLoading = false;
    private boolean isManualRefresh = false;

    // These fields are saved in the bundle in onSaveInstanceState(Bundle outState)
    // and restored in onCreateView
    private int currentPage = 1;    // The current page last loaded
    private int totalPages;         // Number of pages with movies
    private static ArrayList<Movie> movies = new ArrayList<>();

    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.recycler_view)
    RecyclerView moviesRecyclerView;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ShowsCardsAdapter<Movie> movieItemListAdapter;

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
        if (getActivity() != null) {
            // Used to retrieve tmdbIds for hiding collection movies
            moviesBox = ((MyApp)getActivity().getApplication()).getBoxStore().boxFor(Movie.class);
        }

        init();

        movieItemListAdapter = new ShowsCardsAdapter<>(getActivity(), movies, R.menu.menu_movies_list, new ShowsCardsAdapter.ShowsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(MoviesDiscoverFragment.this.getContext(), "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(MoviesDiscoverFragment.this.getContext(), "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                if (MoviesDiscoverFragment.this.getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MovieActivityHTTP.class);
                    Movie movie = movies.get(position);
                    intent.putExtra("tmdb_id", movie.getTmdbId());
                    if (getActivity() != null) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(MoviesDiscoverFragment.this.getActivity(), cardView.findViewById(R.id.image), "image");
                        startActivity(intent, options.toBundle());
                    }
                } else {
                    Log.e("MoviesBaseFragment", "Parent activity is null");
                }
            }
        });

        loadingIndicator.setVisibility(View.VISIBLE);
        isLoading = true;
        if (isRecommended) {
            // Recommended movies section
            long[] tmdbIds = getTmdbIds();
            loadingIndicator.setIndeterminate(false);
            loadingIndicator.setMax(tmdbIds.length);
            // TODO - implement menu sorting options
            MovieComparator movieComparator = new MovieComparator(MovieComparator.BY_NUMBER_OF_TIMES_RECOMMENDED, MovieComparator.DESCENDING);
            RecommendedMoviesList recommendedMoviesList = new RecommendedMoviesList(this, tmdbIds, movieComparator);
            recommendedMoviesList.addRecommendedToList(movies);
        } else {
            // Popular movies / Top rated movies section
            setTmdbUrl();

            if (savedInstanceState != null) {
                movies = (ArrayList<Movie>) savedInstanceState.getSerializable("movies");
                currentPage = (int) savedInstanceState.getSerializable("currentPage");
                totalPages = (int) savedInstanceState.getSerializable("totalPages");
                //Log.i("ShadowDebug", "2 - I am gone");
                loadingIndicator.setVisibility(View.GONE);
                isLoading = false;
            }

            if (movies.isEmpty()) {
                // Request movies only if savedInstanceState has none and the recommended option is not active
                requestAndAddMovies();
            }
        }

        setUpRecyclerView();

        return rootView;
    }

    private void setUpRecyclerView() {
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        moviesRecyclerView.setAdapter(movieItemListAdapter);

        if (!isRecommended) {
            moviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (isLoading) {
                        return;
                    }
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        loadMore();
                    }
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshList();
            swipeRefreshLayout.setRefreshing(false);
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
        ANRequest.GetRequestBuilder requestBuilder = AndroidNetworking
                .get(tmdbUrl)
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
                .addQueryParameter("page", String.valueOf(currentPage))
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES);

        if (isManualRefresh) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        requestBuilder.build()
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache + " currentPage: " + currentPage))
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        List<Movie> requestedMovies = QueryUtils.extractMoviesFromJson(response);

                        if (currentPage == 1) {
                            totalPages = QueryUtils.getTotalPagesFromJson(response);
                        }

                        if (requestedMovies != null && !requestedMovies.isEmpty()) {
                            if (!showItemsInCollection) {
                                removeCollectionMovies(requestedMovies);
                            }
                            movies.addAll(requestedMovies);
                        } else {
                            displayPossibleError();
                            Log.e("ShadowDebug", "MoviesDiscoverFragment - No movies extracted from Json response");
                        }
                        onMoviesListLoaded();
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayPossibleError();
                        Log.e("ShadowDebug", "MoviesDiscoverFragment onError() - " + anError);
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
        MenuItem showWatchedItem = menu.findItem(R.id.show_hide_watched);
        if (isRecommended) {
            showWatchedItem.setVisible(false);
        } else if (showItemsInCollection) {
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
            case R.id.search:
                Intent intent = new Intent(getActivity(), MovieSearchActivity.class);
                startActivity(intent);
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

    private void loadMore() {
        // Movie loading indicator when searching for new temporarMovies
        loadingIndicator.setVisibility(View.VISIBLE);
        isLoading = true;

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
            movies.clear();
            currentPage = 1;
            isManualRefresh = true;
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }

    private void displayPossibleError() {
        // First, hide loading indicator so error message will be visible
        if (movies == null || movies.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
        loadingIndicator.setVisibility(View.GONE);
        isLoading = false;

        // Default - Generic error - Set empty state text to display "No movies found." It's not visible if any Show is added to the adapter
        showEmptyStateTextView(R.string.no_movies_found);

        // Check if there is a more specific error
        if (getActivity() != null) {
            // Used to test internet connection
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr != null) {
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
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
        movieItemListAdapter.notifyDataSetChanged();
        displayPossibleError();
    }
}