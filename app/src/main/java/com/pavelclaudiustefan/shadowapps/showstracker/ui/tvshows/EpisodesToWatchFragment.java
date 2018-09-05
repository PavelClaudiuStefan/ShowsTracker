package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
    @BindView(R.id.search_fab)
    FloatingActionButton searchFab;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Episode> episodes = new ArrayList<>();
    private EpisodesCardsAdapter tvShowItemListAdapter;

    private Box<Episode> episodesBox;


    public EpisodesToWatchFragment() {
        // TODO
        //setHasOptionsMenu(true);
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

        searchFab.setVisibility(View.GONE);

        setUpRecyclerView();
        setUpListener();

        return rootView;
    }

    private void setUpRecyclerView() {
        tvShowItemListAdapter = new EpisodesCardsAdapter(getActivity(), episodes, R.menu.menu_episodes_list, new EpisodesCardsAdapter.EpisodesAdapterListener() {

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
        return requestUnwatchedEpisodes();
    }

    private List<Episode> requestUnwatchedEpisodes() {
        return episodesBox.query()
                .equal(Episode_.isWatched, false)
                .sort(new EpisodeComparator(EpisodeComparator.BY_DATE, EpisodeComparator.ASCENDING))
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
}
