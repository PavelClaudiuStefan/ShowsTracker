package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.MovieAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.MovieListLoader;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";
    private static int MOVIES_LOADER_ID = 0;

    private ListView movieListView;
    private TextView emptyStateTextView;
    private SearchView searchView;

    private MovieAdapter movieAdapter;

    private String query;

    private ArrayList<Movie> movies = new ArrayList<>();

    private Parcelable state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        searchView = findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadSearchResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // TODO - Show suggestions
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (movieAdapter != null) {
            state = movieListView.onSaveInstanceState();
        }
    }

    private void loadSearchResults(String query) {
        this.query = query;

        movieListView = findViewById(R.id.list);

        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        //Only visible if no temporarMovies are found
        emptyStateTextView = findViewById(R.id.empty_view);
        movieListView.setEmptyView(emptyStateTextView);

        movieAdapter = new MovieAdapter(this, movies);
        movieListView.setAdapter(movieAdapter);

        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(MOVIES_LOADER_ID++, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Setup the item click listener
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link MovieActivityHTTP}
                Intent intent = new Intent(SearchActivity.this, MovieActivityHTTP.class);
                Movie movie = movies.get(position);
                intent.putExtra("tmdb_id", String.valueOf(movie.getTmdbId()));
                startActivity(intent);
            }
        });
    }


    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        searchView.clearFocus();

        String tmdbUrl = "https://api.themoviedb.org/3/search/movie";
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("query", query);

        return new MovieListLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
        emptyStateTextView.setText(R.string.no_movies);

        movieAdapter.clear();

        if (movies != null && !movies.isEmpty()) {
            movieAdapter.addAll(movies);
        }

        if(state != null) {
            movieListView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }
}
