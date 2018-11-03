package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.EpisodesCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode_;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.search.TvShowSearchActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.EpisodeComparator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class EpisodesToWatchFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Episode> episodes = new ArrayList<>();
    private EpisodesCardsAdapter tvShowItemListAdapter;

    private Box<Episode> episodesBox;

    private String currentFilterOption;
    private int currentSortDirectionOption;

    public EpisodesToWatchFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) {
            episodesBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(Episode.class);
        } else {
            Log.e("ShadowDebug", "TvShowsCollectionFragment - getApplication() error");
        }

        initFilteringAndSortingOptionsValues();

        setUpRecyclerView();
        setUpListener();

        return rootView;
    }

    private void initFilteringAndSortingOptionsValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_tv_shows_episodes_to_watch_filter),
                getString(R.string.settings_tv_shows_episodes_to_watch_filter_default)
        );

        currentSortDirectionOption = sharedPrefs.getInt(
                getString(R.string.settings_tv_shows_episodes_to_watch_sort_direction),
                EpisodeComparator.ASCENDING
        );
    }

    private void setUpRecyclerView() {
        boolean isOverflowEnabled = currentFilterOption.equals("released");
        tvShowItemListAdapter = new EpisodesCardsAdapter(getActivity(), episodes, R.menu.menu_episodes_list, isOverflowEnabled, new EpisodesCardsAdapter.EpisodesAdapterListener() {

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO - instead of removing episode let user unwatch it
                Episode currentEpisode = episodes.remove(position);
                currentEpisode.setWatched(true);
                episodesBox.put(currentEpisode);
                tvShowItemListAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Episode watched", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                // TODO
//                Intent intent = new Intent(getActivity(), EpisodeActivity.class);
//                Episode episode = episodes.get(position);
//                intent.putExtra("tmdb_id", episode.getTmdbId());
//                if (EpisodesToWatchFragment.this.getActivity() != null) {
//                    ActivityOptionsCompat options = ActivityOptionsCompat.
//                            makeSceneTransitionAnimation(EpisodesToWatchFragment.this.getActivity(), cardView.findViewById(R.id.image), "image");
//                    startActivity(intent, options.toBundle());
//                }
            }
        });

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tvShowItemListAdapter);

        requestAndAddShowsToList();
    }

    private void requestAndAddShowsToList() {
        List<Episode> requestedEpisodesDb = requestEpisodesFromDb();
        episodes.addAll(requestedEpisodesDb);
        tvShowItemListAdapter.notifyDataSetChanged();
        loadingIndicator.setVisibility(View.GONE);

        //Only visible if no movies are found
        emptyStateTextView.setText(R.string.no_tv_shows_added);
        if (episodes == null || episodes.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }

    private List<Episode> requestEpisodesFromDb() {
        switch (currentFilterOption) {
            case "released":
                return requestReleasedEpisodes();
            case "upcoming":
                return requestUpcomingEpisodes();
            default:
                Log.e("ShadowDebug", "EpisodesToWatchFragment - Filtering - displaying shows error");
                return null;
        }
    }

    private List<Episode> requestReleasedEpisodes() {
        long todayInMilliseconds = System.currentTimeMillis();
        return episodesBox.query()
                .less(Episode_.releaseDateInMilliseconds, todayInMilliseconds)
                .equal(Episode_.isWatched, false)
                .sort(new EpisodeComparator(currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Episode> requestUpcomingEpisodes() {
        long todayInMilliseconds = System.currentTimeMillis();
        return episodesBox.query()
                .greater(Episode_.releaseDateInMilliseconds, todayInMilliseconds)
                .equal(Episode_.isWatched, false)
                .sort(new EpisodeComparator(currentSortDirectionOption))
                .build()
                .find();
    }

    private void setUpListener() {
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
        inflater.inflate(R.menu.tv_shows_episodes_to_watch_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Set active option invisible
        switch (currentFilterOption) {
            case "released":
                MenuItem cinemaItem = menu.findItem(R.id.menu_show_released);
                cinemaItem.setEnabled(false);
                break;
            case "upcoming":
                MenuItem notReleasedItem = menu.findItem(R.id.menu_show_upcoming);
                notReleasedItem.setEnabled(false);
                break;
            default:
                Log.e("ShadowDebug", "Episodes to watch - Filtering - setting menu error");
                break;
        }

        // Set active option invisible
        switch (currentSortDirectionOption) {
            case EpisodeComparator.ASCENDING:
                MenuItem ascItem = menu.findItem(R.id.menu_sort_asc_by_date);
                ascItem.setEnabled(false);
                break;
            case EpisodeComparator.DESCENDING:
                MenuItem descItem = menu.findItem(R.id.menu_sort_desc_by_date);
                descItem.setEnabled(false);
                break;
            default:
                Log.e("ShadowDebug", "Episodes to watch - Sorting direction error");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_released:
                saveFilterOption("released");
                break;
            case R.id.menu_show_upcoming:
                saveFilterOption("upcoming");
                break;
            case R.id.menu_sort_asc_by_date:
                saveSortDirectionOption(EpisodeComparator.ASCENDING);
                break;
            case R.id.menu_sort_desc_by_date:
                saveSortDirectionOption(EpisodeComparator.DESCENDING);
                break;
            case R.id.search:
                Intent intent = new Intent(getActivity(), TvShowSearchActivity.class);
                startActivity(intent);
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
        editor.putString(getString(R.string.settings_tv_shows_episodes_to_watch_filter), filterOption);
        editor.apply();
    }

    private void saveSortDirectionOption(int sortDirection) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_tv_shows_episodes_to_watch_sort_direction), sortDirection);
        editor.apply();
    }
}
