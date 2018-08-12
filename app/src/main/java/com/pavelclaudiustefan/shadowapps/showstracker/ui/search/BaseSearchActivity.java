package com.pavelclaudiustefan.shadowapps.showstracker.ui.search;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.EndlessScrollListener;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseSearchActivity<T extends Show> extends AppCompatActivity {

    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    // These fields are saved in the bundle in onSaveInstanceState(Bundle outState)
    // and restored in onCreate
    private int currentPage = 1;    // The current page last loaded
    private int totalPages;         // Number of pages with shows
    private ArrayList<T> items = new ArrayList<>();
    private String query;

    private ShowItemListAdapter<T> showItemListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            loadDataFromSavedInstanceState(savedInstanceState);
        } else {
            searchView.setIconifiedByDefault(false);
        }

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchViewQuery) {
                // OnQueryTextChange does this already
                query = searchViewQuery;
                if (!items.isEmpty()) {
                    showItemListAdapter.clear();
                }
                currentPage = 1;
                loadSearchResults();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchViewQuery) {
                query = searchViewQuery;
                // TODO - Improve or remove this
                // Might make too many calls to the TMDb API
//                if (!items.isEmpty()) {
//                    showItemListAdapter.clear();
//                }
//                if (query.length() >= 3) {
//                    currentPage = 1;
//                    loadSearchResults();
//                }
                return false;
            }
        });

        setUpListView();
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        items = (ArrayList<T>) savedInstanceState.getSerializable("items");
        currentPage = (int) savedInstanceState.getSerializable("currentPage");
        totalPages = (int) savedInstanceState.getSerializable("totalPages");
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setUpListView() {
        //Only visible if no movies are found
        listView.setEmptyView(emptyStateTextView);

        showItemListAdapter = new ShowItemListAdapter<>(this, items);
        listView.setAdapter(showItemListAdapter);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = getIntentForShowActivityHTTP();
                T item = items.get(position);
                intent.putExtra("tmdb_id", String.valueOf(item.getTmdbId()));
                startActivity(intent);
            }
        });

        listView.setOnScrollListener(new EndlessScrollListener(5, 1) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                return currentPage <= totalPages && loadMore();
            }
        });
    }

    private void loadSearchResults() {
        String tmdbUrl = getTmdbBaseSearchUrl();
        String page = String.valueOf(currentPage);

        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", TmdbConstants.API_KEY);
        uriBuilder.appendQueryParameter("query", query);
        uriBuilder.appendQueryParameter("page", page);
        AndroidNetworking.get(uriBuilder.toString())
                .setTag(this)
                .setPriority(Priority.LOW)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .setAnalyticsListener(new AnalyticsListener() {
                    @Override
                    public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                        Log.d("BaseSearchActivity", "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache + " currentPage: " + currentPage);
                    }
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        loadingIndicator.setVisibility(View.GONE);

                        List<T> shows = getShowsFromJsonResponse(response);

                        if (currentPage == 1) {
                            totalPages = QueryUtils.getTotalPagesFromJson(response);
                        }

                        if (shows != null) {
                            showItemListAdapter.addAll(shows);
                        } else {
                            Log.e("ShadowDebug", "SearchActivity - No Shows extracted from Json response");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayError();
                        Log.e("ShadowDebug", "MoviesDiscoverFragment - " + anError.getErrorBody());
                    }
                });
        Log.i("ShadowDebug", "CurrentPage: " + currentPage + " | TotalPages: " + totalPages);
    }

    private void displayError() {
        // Default - Generic error - Set empty state text to display "No movies found." It's not visible if any Show is added to the adapter
        showEmptyStateTextView(R.string.no_shows);

        // Check if there is a more specific error
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                // First, hide loading indicator so error message will be visible
                loadingIndicator.setVisibility(View.GONE);
                showEmptyStateTextView(R.string.no_internet_connection);
            }
        }
    }

    private boolean loadMore() {
        // TvShow loading indicator when searching for new temporarMovies
        loadingIndicator.setVisibility(View.VISIBLE);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        // If there is a network connection, fetch more
        if (networkInfo != null && networkInfo.isConnected()) {
            currentPage++;
            loadSearchResults();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("items", items);
        outState.putSerializable("currentPage", currentPage);
        outState.putSerializable("totalPages", totalPages);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        AndroidNetworking.cancel(this);
    }

    private void showEmptyStateTextView(int resid) {
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(resid);
    }

    public void setSearchViewQueryHint(String hint) {
        searchView.setQueryHint(hint);
    }

    // returns search url for movies or tv shows
    public abstract String getTmdbBaseSearchUrl();

    // returns list of shows (movies or tv shows)
    public abstract List<T> getShowsFromJsonResponse(String jsonResponse);

    // returns intent for show activity (MovieActivityHTTP or TvShowActivity)
    public abstract Intent getIntentForShowActivityHTTP();
}
