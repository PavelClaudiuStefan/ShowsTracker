package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
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

    public TvShowsAllFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);

        ButterKnife.bind(this, rootView);
        setUpListView();
        setUpListeners();

        return rootView;
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
                Intent intent = new Intent(getActivity(), TvShowActivityHTTP.class);
                TvShow tvShow = tvShows.get(position);
                intent.putExtra("tmdb_id", String.valueOf(tvShow.getTmdbId()));
                startActivity(intent);
            }
        });
    }

    private void requestAndAddShowsToAdapter() {
        if (getActivity() != null) {
            Box<TvShow> showsBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(TvShow.class);
            List<TvShow> tvShows = showsBox.getAll();
            tvShowItemListAdapter.addAll(tvShows);
            loadingIndicator.setVisibility(View.GONE);
        } else {
            Log.e("ShadowDebug", "TvShowsAllFragment - getApplication() error");
        }
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
}
