package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
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

// Base for fragments that display movies from the database
public abstract class MoviesBaseFragment extends Fragment{

    @BindView(R.id.list)
    ListView movieListView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.search_fab)
    FloatingActionButton searchFab;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFabVisible = false;
    private ShowItemListAdapter<Movie> movieItemListAdapter;

    private Box<Movie> moviesBox;
    private ArrayList<Movie> movies;

    public MoviesBaseFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null) {
            moviesBox = ((MyApp) getActivity().getApplication()).getBoxStore().boxFor(Movie.class);
        }

        initFilteringAndSortingOptionsValues();
        requestMoviesAndAddToAdapter();
        setUpListView();
        setUpSearchFab();

        return rootView;
    }

    public abstract void initFilteringAndSortingOptionsValues();

    private void requestMoviesAndAddToAdapter() {
        movies = (ArrayList<Movie>) requestMoviesFromDb();
        movieItemListAdapter = new ShowItemListAdapter<>(getContext(), movies);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setUpListView() {
        //Only visible if no movies are found
        emptyStateTextView.setText(R.string.no_movies_added);
        movieListView.setEmptyView(emptyStateTextView);

        movieListView.setAdapter(movieItemListAdapter);

        // Setup the item click listener
        movieListView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(getActivity(), MovieActivityDb.class);
            Movie movie = movies.get(position);
            intent.putExtra("tmdb_id", movie.getTmdbId());
            if (getActivity() != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this.getActivity(), movieListView.findViewById(R.id.image), String.valueOf(movie.getTmdbId()));
                startActivity(intent, options.toBundle());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshMovieList();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void enableSearchFab(boolean value) {
        isFabVisible = value;
    }

    private void setUpSearchFab() {
        if (isFabVisible) {
            searchFab.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), MovieSearchActivity.class);
                startActivity(intent);
            });
        } else {
            searchFab.setVisibility(View.GONE);
        }
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
