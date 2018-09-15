package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;
import android.widget.Toast;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.TvShowComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow_;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.search.TvShowSearchActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowsCollectionFragment extends Fragment{

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.search_fab)
    FloatingActionButton searchFab;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<TvShow> tvShows = new ArrayList<>();
    private ShowsCardsAdapter<TvShow> tvShowItemListAdapter;

    private Box<TvShow> showsBox;

    private String currentFilterOption;
    private int currentSortByOption;
    private int currentSortDirectionOption;

    public TvShowsCollectionFragment() {
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
            Log.e("ShadowDebug", "TvShowsCollectionFragment - getApplication() error");
        }

        initFilteringAndSortingOptionsValues();

        setUpRecyclerView();
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

    private void setUpRecyclerView() {
        tvShowItemListAdapter = new ShowsCardsAdapter<>(getContext(), tvShows, R.menu.menu_tv_shows_list, new ShowsCardsAdapter.ShowsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(TvShowsCollectionFragment.this.getContext(), "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(TvShowsCollectionFragment.this.getContext(), "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                Intent intent = new Intent(getActivity(), TvShowActivityDb.class);
                TvShow tvShow = tvShows.get(position);
                intent.putExtra("tmdb_id", tvShow.getTmdbId());
                if (TvShowsCollectionFragment.this.getActivity() != null) {
                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(TvShowsCollectionFragment.this.getActivity(), cardView.findViewById(R.id.image), "image");
                    startActivity(intent, options.toBundle());
                }
            }
        });

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tvShowItemListAdapter);

        requestAndAddShowsToList();
    }

    private void requestAndAddShowsToList() {
        List<TvShow> requestedTvShows = requestShowsFromDb();
        tvShows.addAll(requestedTvShows);
        tvShowItemListAdapter.notifyDataSetChanged();
        loadingIndicator.setVisibility(View.GONE);

        //Only visible if no movies are found
        emptyStateTextView.setText(R.string.no_tv_shows_added);
        if (tvShows == null || tvShows.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
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
                Log.e("TvShowsCollection", "TvShowsCollectionFragment - Filtering - displaying shows error");
                // NULL - as empty object pattern
                return new ArrayList<>();
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
        searchFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), TvShowSearchActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshShowsList();
            swipeRefreshLayout.setRefreshing(false);
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
                MenuItem notReleasedItem = menu.findItem(R.id.menu_show_upcoming);
                notReleasedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesCollection", "Filtering - setting menu error");
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
                Log.e("TvShowsCollection", "Sorting error: currentSortOption" + currentSortByOption);
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
                Log.e("TvShowsCollection", "Sorting direction error");
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
            case R.id.menu_show_upcoming:
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

        refreshShowsList();
        return true;
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
