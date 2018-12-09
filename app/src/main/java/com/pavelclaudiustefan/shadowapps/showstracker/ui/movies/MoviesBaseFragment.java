package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

// Base for fragments that display movies from the database
public abstract class MoviesBaseFragment extends Fragment{

    @BindView(R.id.recycler_view)
    RecyclerView moviesRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ShowsCardsAdapter<Movie> movieItemListAdapter;

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
        setUpRecyclerView();

        return rootView;
    }

    public abstract void initFilteringAndSortingOptionsValues();

    private void requestMoviesAndAddToAdapter() {
        movies = (ArrayList<Movie>) requestMoviesFromDb();
        movieItemListAdapter = new ShowsCardsAdapter<>(getContext(), movies, R.menu.menu_movies_list, new ShowsCardsAdapter.ShowsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(MoviesBaseFragment.this.getContext(), "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(MoviesBaseFragment.this.getContext(), "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                if (MoviesBaseFragment.this.getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MovieActivityDb.class);
                    Movie movie = movies.get(position);
                    intent.putExtra("tmdb_id", movie.getTmdbId());
                    if (getActivity() != null) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(MoviesBaseFragment.this.getActivity(), cardView.findViewById(R.id.image), "image");
                        startActivity(intent, options.toBundle());
                    }
                } else {
                    Log.e("MoviesBaseFragment", "Parent activity is null");
                }
            }

            @Override
            public boolean onLongClicked(int position, CardView cardView) {
                return false;
            }
        });
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setUpRecyclerView() {
        //Only visible if no movies are found
        emptyStateTextView.setText(R.string.no_movies_added);
        if (movies == null || movies.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        moviesRecyclerView.setAdapter(movieItemListAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshMovieList();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void refreshMovieList() {
        if (getActivity() != null) {
            //movieItemListAdapter.clear();
            ((MoviesActivity)getActivity()).dataChanged();
        }
    }

    public Box<Movie> getMoviesBox() {
        return moviesBox;
    }

    public abstract List<Movie> requestMoviesFromDb();
}
