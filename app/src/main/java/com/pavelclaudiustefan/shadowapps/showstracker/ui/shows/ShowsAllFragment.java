package com.pavelclaudiustefan.shadowapps.showstracker.ui.shows;

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
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.SearchActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class ShowsAllFragment extends Fragment{

    private View rootView;

    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    @BindView(R.id.empty_view)
    TextView emptyStateTextView;

    private ArrayList<Show> shows = new ArrayList<>();

    private ShowItemListAdapter showItemListAdapter;

    public ShowsAllFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.category_list, container, false);

        ButterKnife.bind(this, rootView);

        ListView showListView = rootView.findViewById(R.id.list);

        //Only visible if no movies are found
        showListView.setEmptyView(emptyStateTextView);

        showItemListAdapter = new ShowItemListAdapter(getContext(), shows);
        showListView.setAdapter(showItemListAdapter);

        requestAndAddShows();

        // Setup the item click listener
        showListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ShowActivityHTTP.class);
                Show show = shows.get(position);
                intent.putExtra("tmdb_id", String.valueOf(show.getTmdbId()));
                startActivity(intent);
            }
        });

        FloatingActionButton searchFab = rootView.findViewById(R.id.search_fab);
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshShowsList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    private void requestAndAddShows() {
        if (getActivity() != null) {
            Box<Show> showsBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(Show.class);
            List<Show> shows = showsBox.getAll();
            showItemListAdapter.addAll(shows);
            loadingIndicator.setVisibility(View.GONE);
        } else {
            Log.e("ShadowDebug", "ShowsAllFragment - getApplication() error");
        }
    }


    public void refreshShowsList() {
        if (getActivity() != null) {
            ((ShowsActivity)getActivity()).dataChanged();
        }
    }
}
