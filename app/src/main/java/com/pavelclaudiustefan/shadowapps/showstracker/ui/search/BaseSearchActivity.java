package com.pavelclaudiustefan.shadowapps.showstracker.ui.search;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseSearchActivity<T extends Show> extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
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
    private ShowsCardsAdapter<T> showItemListAdapter;
    private ArrayList<T> items = new ArrayList<>();
    private String query;
    private int menuResId;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState != null) {
            loadDataFromSavedInstanceState(savedInstanceState);
        } else {
            searchView.setIconifiedByDefault(false);
        }

        setUpSearchView();
        setUpRecyclerView();
    }

    private void setUpSearchView() {
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchViewQuery) {
                // OnQueryTextChange does this already
                query = searchViewQuery;
                if (!items.isEmpty()) {
                    items.clear();
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
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        items = (ArrayList<T>) savedInstanceState.getSerializable("items");
        currentPage = (int) savedInstanceState.getSerializable("currentPage");
        totalPages = (int) savedInstanceState.getSerializable("totalPages");
        loadingIndicator.setVisibility(View.GONE);
        isLoading = false;
    }

    private void setUpRecyclerView() {
        //Only visible if no items are found
        if (items == null || items.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }

        showItemListAdapter = new ShowsCardsAdapter<>(this, items, menuResId, new ShowsCardsAdapter.ShowsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(BaseSearchActivity.this, "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(BaseSearchActivity.this, "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                Intent intent = getIntentForShowActivityHTTP();
                T item = items.get(position);
                intent.putExtra("tmdb_id", item.getTmdbId());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(BaseSearchActivity.this, cardView.findViewById(R.id.image), "image");
                startActivity(intent, options.toBundle());
            }

            @Override
            public boolean onLongClicked(int position, CardView cardView) {
                return false;
            }
        });
        recyclerView.setAdapter(showItemListAdapter);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(showItemListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLoading) {
                    return;
                }
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    loadMore();
                    //Log.i("ShadowDebug", "\nEND OF LIST" + "\nvisibleItemCount: " + visibleItemCount + "\ntotalItemCount: "+ totalItemCount + "\npastVisibleItems: " + pastVisibleItems);
                }
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
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> Log.d("BaseSearchActivity", "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache + " currentPage: " + currentPage))
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        List<T> requestedItems = getShowsFromJsonResponse(response);

                        if (currentPage == 1) {
                            totalPages = QueryUtils.getTotalPagesFromJson(response);
                        }

                        if (requestedItems != null) {
                            items.addAll(requestedItems);
                            showItemListAdapter.notifyDataSetChanged();

                        } else {
                            Log.e("ShadowDebug", "SearchActivity - No Shows extracted from Json response");
                        }
                        loadingIndicator.setVisibility(View.GONE);
                        isLoading = false;
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
                isLoading = false;
                showEmptyStateTextView(R.string.no_internet_connection);
            }
        }
    }

    private void loadMore() {
        // TvShow loading indicator when searching for new temporarMovies
        loadingIndicator.setVisibility(View.VISIBLE);
        isLoading = true;

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        // If there is a network connection, fetch more
        if (networkInfo != null && networkInfo.isConnected()) {
            currentPage++;
            loadSearchResults();
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

    public void setMenuResId(int menuResId) {
        this.menuResId = menuResId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
