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
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.VideoMainItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.EndlessScrollListener;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.ShowTmdbIdsLoader;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.ShowsListLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowsDiscoverFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<VideoMainItem>> {

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final String TOP_RATED_OPTION = "top_rated_option";
    private static final String POPULAR_OPTION = "popular_option";
    private static final String RECOMMENDED_OPTION = "recommended_option";
    private String option;

    private String topRatedUrl;
    private String popularUrl;
    private String tmdbUrl;

    private boolean isRecommended = false;

    private int DATABASE_LOADER_ID = 0;
    private int HTTP_LOADER_ID = 1;
    private int currentPage = 1;

    private static boolean canLoadNewItems;

    private int totalPages;

    private View rootView;

    private static ArrayList<VideoMainItem> items = new ArrayList<>();
    private ArrayList<Integer> tmdbIds;

    private VideoMainItemListAdapter videoMainItemListAdapter;

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

        init();
        setTmdbUrl();

        FloatingActionButton fab = rootView.findViewById(R.id.search_fab);
        fab.setVisibility(View.GONE);

        listView = rootView.findViewById(R.id.list);

        //Only visible if no temporarMovies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        listView.setEmptyView(emptyStateTextView);

        videoMainItemListAdapter = new VideoMainItemListAdapter(getActivity(), items);
        listView.setAdapter(videoMainItemListAdapter);

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Loads tmdbIds and then starts the items loader
            new ShowTmdbIdsLoader(this);

        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ShowActivityHTTP.class);
                VideoMainItem item = items.get(position);
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
    }



    @Override
    public void onLoadFinished(Loader<List<VideoMainItem>> loader, List<VideoMainItem> items) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // TODO - Save the totalPages value only once
        if (!isRecommended) {
            // Get total pages from the first movie
            if (currentPage == 1) {
                totalPages = items.get(0).getTotalPages();
            }
        }

        if (canLoadNewItems) {
            // Set empty state text to display "No videoMainItems found." It's not visible if any VideoMainItem is added to the adapter
            emptyStateTextView.setText(R.string.no_shows);

            // If showItemsInCollection = false -> hide watched
            if (!showItemsInCollection) {
                ArrayList<VideoMainItem> itemsToDelete = new ArrayList<>();
                for (VideoMainItem item : items) {
                    int movieTmdbId = item.getTmdbId();
                    for (int tmdbId:tmdbIds) {
                        if (movieTmdbId == tmdbId) {
                            itemsToDelete.add(item);
                        }
                    }
                }
                items.removeAll(itemsToDelete);
            }

            if (items != null && !items.isEmpty()) {
                videoMainItemListAdapter.addAll(items);
            }
        }
        canLoadNewItems = false;
    }

    @Override
    public Loader<List<VideoMainItem>> onCreateLoader(int id, Bundle args) {
        if (isRecommended) {
            return recommendedList(tmdbIds);
        } else {
            String page = String.valueOf(currentPage);

            Uri baseUri = Uri.parse(tmdbUrl);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("api_key", API_KEY);
            uriBuilder.appendQueryParameter("page", page);

            return new ShowsListLoader(getActivity(), uriBuilder.toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<VideoMainItem>> loader) {
        videoMainItemListAdapter.clear();
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

    public void startLoader() {
        canLoadNewItems = true;
        getLoaderManager().initLoader(HTTP_LOADER_ID, null, this);
    }

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
            LoaderManager loaderManager = getLoaderManager();
            canLoadNewItems = true;
            loaderManager.initLoader(++HTTP_LOADER_ID, null, com.pavelclaudiustefan.shadowapps.showstracker.ui.ShowsDiscoverFragment.this);
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

    public void setTmdbIds(ArrayList<Integer> tmdbIds) {
        this.tmdbIds = tmdbIds;
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

    public Loader<List<VideoMainItem>> recommendedList(ArrayList<Integer> tmdbIds) {
        // TODO
        return null;
    }

    public void refreshList() {
        if (getActivity() != null) {
            ((ShowsActivity)getActivity()).dataChanged();
        }
    }

}
