package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.search.MovieSearchActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public abstract class MoviesBaseFragment extends Fragment{

    @BindView(R.id.list)
    ListView movieListView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;
    @BindView(R.id.search_fab)
    FloatingActionButton searchFab;

    private boolean isFabVisible = false;
    private ShowItemListAdapter<Movie> movieItemListAdapter;

    private Box<Movie> moviesBox;
    private ArrayList<Movie> movies;

    public MoviesBaseFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);

        ButterKnife.bind(this, rootView);
        if (getActivity() != null) {
            moviesBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(Movie.class);
        }

        //Only visible if no movies are found
        emptyStateTextView = rootView.findViewById(R.id.empty_view);
        movieListView.setEmptyView(emptyStateTextView);

        movies = (ArrayList<Movie>) requestMoviesFromDb();

        movieItemListAdapter = new ShowItemListAdapter<>(getContext(), movies);
        movieListView.setAdapter(movieItemListAdapter);
        loadingIndicator.setVisibility(View.GONE);

        // Setup the item click listener
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MovieActivityDb.class);
                Movie movie = movies.get(position);
                intent.putExtra("tmdb_id", movie.getTmdbId());
                startActivity(intent);
            }
        });


        if (isFabVisible) {
            searchFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MovieSearchActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            searchFab.setVisibility(View.GONE);
        }

        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMovieList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    public void setSearchFabVisibility(boolean value) {
        isFabVisible = value;
    }

    public void refreshMovieList() {
        if (getActivity() != null) {
            movieItemListAdapter.clear();
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }

    public Box<Movie> getMoviesBox() {
        return moviesBox;
    }

    public abstract List<Movie> requestMoviesFromDb();
}
