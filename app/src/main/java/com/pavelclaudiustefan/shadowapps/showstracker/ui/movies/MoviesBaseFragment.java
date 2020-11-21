package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataSubscription;

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

    private BoxStore boxStore;
    private Box<Movie> moviesBox;
    private ArrayList<Movie> movies;

    private DataSubscription subscription;
    private Query<Movie> moviesQuery;

    public MoviesBaseFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null) {
            boxStore = ((MyApp) getActivity().getApplication()).getBoxStore();
            moviesBox = boxStore.boxFor(Movie.class);
        }

        initFilteringAndSortingOptionsValues();
        requestMoviesAndAddToAdapter();
        setUpRecyclerView();

        return rootView;
    }

    public abstract void initFilteringAndSortingOptionsValues();

    private void requestMoviesAndAddToAdapter() {
        moviesQuery = requestMoviesFromDb();
        // TODO: 09-Dec-18 - observer that refreshes movies list when new data is loaded
//        subscription = moviesQuery.subscribe().onlyChanges().observer(data -> {
//            Toast.makeText(getContext(), "moviesQuery CHANGED: " + data.size(), Toast.LENGTH_SHORT).show();
//            Log.i("ShadowDebug", "requestMoviesAndAddToAdapter: moviesQuery CHANGED: " + data.size());
//        });
//        boxStore.subscribe(Movie.class).observer(data -> {
//            Toast.makeText(getContext(), "BOXSTORE UPDATED", Toast.LENGTH_SHORT).show();
//            refreshMovieList();
//        });
        addMoviesToAdapter(moviesQuery.find());
    }

    private void addMoviesToAdapter(List<Movie> reqMovies) {
        movies = (ArrayList<Movie>) reqMovies;
        movieItemListAdapter = new ShowsCardsAdapter<>(getContext(), movies, R.menu.menu_movie_card, new ShowsCardsAdapter.ShowsAdapterListener() {
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

    public abstract Query<Movie> requestMoviesFromDb();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (!subscription.isCanceled())
//            subscription.cancel();
    }
}
