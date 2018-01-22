package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieActivityHTTP extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie>{

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final int EXISTING_MOVIE_LOADER = 0;

    private String tmdbId;

    private ImageView imageView;
    private TextView titleTextView;
    private TextView releaseDateTextView;
    private TextView averageVoteTextView;
    private TextView voteCountTextView;
    private TextView overviewTextView;
    private Button imdbButton;

    private TextView emptyStateTextView;

    private boolean inUserCollection;
    private boolean isWatched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        setTitle("Movie");

        Intent intent = getIntent();
        tmdbId = intent.getExtras().getString("tmdb_id");

        imageView = findViewById(R.id.movie_image);
        titleTextView = findViewById(R.id.movie_title);
        averageVoteTextView = findViewById(R.id.average_vote);
        voteCountTextView = findViewById(R.id.vote_count);
        releaseDateTextView = findViewById(R.id.release_date);
        overviewTextView = findViewById(R.id.overview);
        imdbButton = findViewById(R.id.imdb_url_button);

        getSupportLoaderManager().initLoader(EXISTING_MOVIE_LOADER, null, this);

        //Only visible if no movies are found
        emptyStateTextView = findViewById(R.id.empty_view);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(1, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<Movie> onCreateLoader(int i, Bundle bundle) {
        String tmdbUrl = "https://api.themoviedb.org/3/movie/" + tmdbId;
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api_key", API_KEY);

        return new MovieDataLoader(this, uriBuilder.toString());
    }


    @Override
    public void onLoadFinished(Loader<Movie> loader, final Movie movie) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (movie == null) {
            return;
        }

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        String imageUrl = movie.getImageUrl();
        String title = movie.getTitle();
        String averageVote = String.valueOf(movie.getVote());
        String voteCount = movie.getVoteCount();
        String releaseDate = movie.getDate();
        String overview = movie.getOverview();
        final String imdbUrl = movie.getImdbUrl();

        Picasso.with(this)
                .load(imageUrl)
                .into(imageView);
        titleTextView.setText(title);
        averageVoteTextView.setText(averageVote);
        voteCountTextView.setText(voteCount);
        releaseDateTextView.setText(releaseDate);
        overviewTextView.setText(overview);

        imdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(imdbUrl));
                startActivity(intent);
            }
        });

        if (title != null && !title.isEmpty()) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movie_data);
        }

        final ToggleButton addRemoveMovieButton = findViewById(R.id.add_remove_movie);
        addRemoveMovieButton.setChecked(inUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    insertMovie(movie);
                } else {
                    // TODO
                    // removeMovie(movieId);
                }
            }
        });

        final ToggleButton watchedNotWatchedButton = findViewById(R.id.watched_not_watched_movie);
        watchedNotWatchedButton.setChecked(isWatched);
        watchedNotWatchedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // TODO - Update watched -> true
                } else {
                    // TODO - Update watched -> false
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {
        // TODO
    }

    private void insertMovie(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.TMDB_ID, movie.getTmdbId());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE, movie.getVote());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT, movie.getVoteCount());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS, movie.getDateInMilliseconds());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMDB_URL, movie.getImdbUrl());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_ID, movie.getImageId());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL, movie.getThumbnailUrl());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_URL, movie.getImageUrl());
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_WATCHED, movie.getWatchedIntValue());

        getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
    }

    private void removeMovie(String movieId) {
        getContentResolver().delete(Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId), null, null);
    }
}
