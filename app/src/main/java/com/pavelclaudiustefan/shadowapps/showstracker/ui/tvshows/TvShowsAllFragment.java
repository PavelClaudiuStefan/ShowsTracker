package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TvShowComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow_;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.search.TvShowSearchActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowsAllFragment extends Fragment{

    @BindView(R.id.list)
    ListView showListView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.search_fab)
    FloatingActionButton searchFab;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<TvShow> tvShows = new ArrayList<>();
    private ShowItemListAdapter<TvShow> tvShowItemListAdapter;

    private Box<TvShow> showsBox;

    private String currentFilterOption;
    private int currentSortByOption;
    private int currentSortDirectionOption;

    public TvShowsAllFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) {
            showsBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(TvShow.class);
        } else {
            Log.e("ShadowDebug", "TvShowsAllFragment - getApplication() error");
        }

        initFilteringAndSortingOptionsValues();

        setUpListView();
        setUpListeners();

        return rootView;
    }

    private void initFilteringAndSortingOptionsValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_tv_shows_collection_filter),
                getString(R.string.settings_tv_shows_collection_filter_default)
        );
        currentSortByOption = sharedPrefs.getInt(
                getString(R.string.settings_tv_shows_collection_sort),
                TvShowComparator.BY_DATE
        );

        currentSortDirectionOption = sharedPrefs.getInt(
                getString(R.string.settings_tv_shows_collection_sort_direction),
                TvShowComparator.ASCENDING
        );
    }

    private void setUpListView() {
        //Only visible if no movies are found
        showListView.setEmptyView(emptyStateTextView);

        tvShowItemListAdapter = new ShowItemListAdapter<>(getContext(), tvShows);
        showListView.setAdapter(tvShowItemListAdapter);

        requestAndAddShowsToAdapter();

        // Setup the item click listener
        showListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO - TvShowActivityDb
                Intent intent = new Intent(getActivity(), TvShowActivityHTTP.class);
                TvShow tvShow = tvShows.get(position);
                intent.putExtra("tmdb_id", String.valueOf(tvShow.getTmdbId()));
                startActivity(intent);
            }
        });
    }

    private void requestAndAddShowsToAdapter() {
        List<TvShow> tvShows = requestShowsFromDb();
        assert tvShows != null;
        tvShowItemListAdapter.addAll(tvShows);
        loadingIndicator.setVisibility(View.GONE);
    }

    private List<TvShow> requestShowsFromDb() {
        switch (currentFilterOption) {
            case "all":
                return requestMoviesAll();
            case "released":
                return requestMoviesReleased();
            case "not_released":
                return requestMoviesNotReleased();
            default:
                Log.e("TvShowsAllFragment", "TvShowsAllFragment - Filtering - displaying shows error");
                return null;
        }
    }

    private List<TvShow> requestMoviesAll() {
        return showsBox.query()
                .sort(new TvShowComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<TvShow> requestMoviesReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return showsBox.query()
                .less(TvShow_.releaseDateInMilliseconds, todayInMilliseconds)
                .sort(new TvShowComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<TvShow> requestMoviesNotReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return showsBox.query()
                .greater(TvShow_.releaseDateInMilliseconds, todayInMilliseconds)
                .sort(new TvShowComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private void setUpListeners() {
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TvShowSearchActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshShowsList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void refreshShowsList() {
        if (getActivity() != null) {
            ((TvShowsActivity)getActivity()).dataChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.tv_shows_collection_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Set active option invisible
        switch (currentFilterOption) {
            case "all":
                MenuItem allItem = menu.findItem(R.id.menu_show_all);
                allItem.setEnabled(false);
                break;
            case "released":
                MenuItem cinemaItem = menu.findItem(R.id.menu_show_released);
                cinemaItem.setEnabled(false);
                break;
            case "not_released":
                MenuItem notReleasedItem = menu.findItem(R.id.menu_show_not_released);
                notReleasedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Filtering - setting menu error");
                break;
        }

        // Set active option invisible
        switch (currentSortByOption) {
            case TvShowComparator.BY_DATE:
                MenuItem dateItem = menu.findItem(R.id.menu_sort_by_date);
                dateItem.setEnabled(false);
                break;
            case TvShowComparator.BY_RATING:
                MenuItem ratingItem = menu.findItem(R.id.menu_sort_by_rating);
                ratingItem.setEnabled(false);
                break;
            case TvShowComparator.ALPHABETICALLY:
                MenuItem alphabeticallyItem = menu.findItem(R.id.menu_sort_alphabetically);
                alphabeticallyItem.setEnabled(false);
                break;
            default:
                Log.e("TvShowsAllFragment", "Sorting error: currentSortOption" + currentSortByOption);
                break;
        }

        // Set active option invisible
        switch (currentSortDirectionOption) {
            case TvShowComparator.ASCENDING:
                MenuItem ascItem = menu.findItem(R.id.menu_sort_asc);
                ascItem.setEnabled(false);
                break;
            case TvShowComparator.DESCENDING:
                MenuItem descItem = menu.findItem(R.id.menu_sort_desc);
                descItem.setEnabled(false);
                break;
            default:
                Log.e("TvShowsAllFragment", "Sorting direction error");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_all:
                saveFilterOption("all");
                break;
            case R.id.menu_show_released:
                saveFilterOption("released");
                break;
            case R.id.menu_show_not_released:
                saveFilterOption("not_released");
                break;
            case R.id.menu_sort_by_date:
                saveSortByOption(TvShowComparator.BY_DATE);
                break;
            case R.id.menu_sort_by_rating:
                saveSortByOption(TvShowComparator.BY_RATING);
                break;
            case R.id.menu_sort_alphabetically:
                saveSortByOption(TvShowComparator.ALPHABETICALLY);
                break;
            case R.id.menu_sort_asc:
                saveSortDirectionOption(TvShowComparator.ASCENDING);
                break;
            case R.id.menu_sort_desc:
                saveSortDirectionOption(TvShowComparator.DESCENDING);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshMovieList();
        return true;
    }

    public void refreshMovieList() {
        if (getActivity() != null) {
            tvShowItemListAdapter.clear();
            ((TvShowsActivity)getActivity()).dataChanged();
        }
    }

    private void saveFilterOption(String filterOption) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_tv_shows_collection_filter), filterOption);
        editor.apply();
    }

    private void saveSortByOption(int sortBy) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_tv_shows_collection_sort), sortBy);
        editor.apply();
    }

    private void saveSortDirectionOption(int sortDirection) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_tv_shows_collection_sort_direction), sortDirection);
        editor.apply();
    }
}
