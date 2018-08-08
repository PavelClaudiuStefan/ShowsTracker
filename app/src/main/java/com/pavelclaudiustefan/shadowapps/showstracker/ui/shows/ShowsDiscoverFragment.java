package com.pavelclaudiustefan.shadowapps.showstracker.ui.shows;

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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.EndlessScrollListener;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowsDiscoverFragment extends Fragment {

    public static final String TAG = "ShowsDiscoverFragment";

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final String TOP_RATED_OPTION = "top_rated_option";
    private static final String POPULAR_OPTION = "popular_option";
    private static final String RECOMMENDED_OPTION = "recommended_option";
    private String option;

    private String topRatedUrl;
    private String popularUrl;
    private String tmdbUrl;

    // TODO - Recommended section
    private boolean isRecommended = false;

    private int currentPage = 1;
    private int totalPages;

    private View rootView;

    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    private static ArrayList<Show> shows = new ArrayList<>();

    private ShowItemListAdapter showItemListAdapter;

    private boolean showItemsInCollection;

    private TextView emptyStateTextView;
    private ListView listView;

    private Parcelable state;

    public ShowsDiscoverFragment() {
        setHasOptionsMenu(true);
        topRatedUrl = "https://api.themoviedb.org/3/tv/top_rated";
        popularUrl = "https://api.themoviedb.org/3/tv/popular";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.category_list, container, false);

        ButterKnife.bind(this, rootView);

        init();
        setTmdbUrl();

        FloatingActionButton fab = rootView.findViewById(R.id.search_fab);
        fab.setVisibility(View.GONE);

        listView = rootView.findViewById(R.id.list);

        //Only visible if no temporarMovies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        listView.setEmptyView(emptyStateTextView);

        showItemListAdapter = new ShowItemListAdapter(getActivity(), shows);
        listView.setAdapter(showItemListAdapter);

        if (!isRecommended) {
            requestAndAddShows();
        }

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // TODO - Get tmdbIds from database to hide shows already in colection

        } else {
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ShowActivityHTTP.class);
                Show item = shows.get(position);
                intent.putExtra("tmdb_id", String.valueOf(item.getTmdbId()));
                startActivity(intent);
            }
        });

        if (!isRecommended) {
            listView.setOnScrollListener(new EndlessScrollListener(5, 1) {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {
                    if (currentPage <= totalPages) {
                        return loadMore();
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
                refreshList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        state = listView.onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(state != null) {
            listView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AndroidNetworking.cancel(this);
    }

    // Requests shows and adds them to the showItemsListAdapter
    public void requestAndAddShows() {
        String page = String.valueOf(currentPage);

        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("page", page);

        AndroidNetworking.get(uriBuilder.toString())
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(0, TimeUnit.SECONDS)
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
                        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
                        loadingIndicator.setVisibility(View.GONE);

                        List<Show> shows = QueryUtils.extractShowsFromJson(response);

                        if (currentPage == 1) {
                            totalPages = QueryUtils.getNumberOfShowsPagesFromJson(response);
                        }

                        if (shows != null) {
                            showItemListAdapter.addAll(shows);
                        } else {
                            Log.e("ShadowDebug", "ShowsDiscoverFragment - No shows extracted from Json response");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("ShadowDebug", anError.getErrorBody());
                    }
                });
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
                Log.e("DiscoverListFragment", "Filtering error");
                break;
        }
        if (showItemsInCollection) {
            MenuItem showWatchedItem = menu.findItem(R.id.show_hide_watched);
            showWatchedItem.setTitle(R.string.hide_collection_shows);
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
        editor.putString(getString(R.string.settings_discover_shows_option), option);
        editor.apply();
    }

    private void saveSettingsShowHideWatched() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(getString(R.string.settings_discover_shows_show_watched), !showItemsInCollection);
        editor.apply();
    }

    // Loads the next page of shows
    private boolean loadMore() {
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
            requestAndAddShows();
            currentPage++;
            return true;
        } else {
            loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
            return false;
        }
    }

    public void setTmdbUrl() {
        if (Objects.equals(option, TOP_RATED_OPTION)) {
            tmdbUrl = topRatedUrl;
        } else if (Objects.equals(option, POPULAR_OPTION)) {
            tmdbUrl = popularUrl;
        } else {
            tmdbUrl = null;
        }
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setShowItemsInCollection(boolean showItemsInCollection) {
        this.showItemsInCollection = showItemsInCollection;
    }

    public void init()  {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        option = sharedPrefs.getString(
                getString(R.string.settings_discover_shows_option),
                getString(R.string.settings_discover_shows_option_default)
        );

        if (Objects.equals(option, "recommended_option")) {
            isRecommended = true;
        }

        boolean showItemsInCollection = sharedPrefs.getBoolean(
                getString(R.string.settings_discover_shows_show_watched),
                true
        );
        setShowItemsInCollection(showItemsInCollection);
    }

    public void refreshList() {
        if (getActivity() != null) {
            showItemListAdapter.clear();
            ((ShowsActivity)getActivity()).dataChanged();
        }
    }

}
