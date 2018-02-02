package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.MovieDataLoader;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

public class MovieActivityHTTP extends AppCompatActivity implements LoaderManager.LoaderCallbacks{

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final int EXISTING_MOVIE_LOADER = 0;
    private static final int EXISTING_MOVIE_LOADER_ID = 1;

    private String tmdbId;

    private ImageView imageView;
    private TextView titleTextView;
    private TextView cinemaReleaseDateTextView;
    private TextView digitalReleaseDateTextView;
    private TextView physicalReleaseDateTextView;
    private TextView averageVoteTextView;
    private TextView voteCountTextView;
    private TextView overviewTextView;
    private Button imdbButton;

    private TextView emptyStateTextView;

    private boolean inUserCollection;
    private boolean isWatched;

    private Movie movie;
    private String movieId;

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
        cinemaReleaseDateTextView = findViewById(R.id.cinema_release_date);
        digitalReleaseDateTextView = findViewById(R.id.digital_release_date);
        physicalReleaseDateTextView = findViewById(R.id.physical_release_date);
        overviewTextView = findViewById(R.id.overview);
        imdbButton = findViewById(R.id.imdb_url_button);

        getLoaderManager().initLoader(EXISTING_MOVIE_LOADER, null, this);

        //Only visible if no movies are found
        emptyStateTextView = findViewById(R.id.empty_view);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EXISTING_MOVIE_LOADER, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == EXISTING_MOVIE_LOADER) {
            String tmdbUrl = "https://api.themoviedb.org/3/movie/" + tmdbId;
            Uri baseUri = Uri.parse(tmdbUrl);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("api_key", API_KEY);
            uriBuilder.appendQueryParameter("append_to_response", "release_dates");

            return new MovieDataLoader(this, uriBuilder.toString());
        } else if (id == EXISTING_MOVIE_LOADER_ID){
            String[] projection = {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_WATCHED};

            String selection = MovieEntry.TMDB_ID + "=?";

            String[] selectionArgs = {String.valueOf(movie.getTmdbId())};

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    MovieEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (loader.getId() == EXISTING_MOVIE_LOADER) {
            this.movie = (Movie)data;

            if (movie == null) {
                return;
            }

            getLoaderManager().initLoader(EXISTING_MOVIE_LOADER_ID, null, this);

            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            String imageUrl = movie.getImageUrl();
            String title = movie.getTitle();
            String averageVote = String.valueOf(movie.getVote());
            String voteCount = movie.getVoteCount();
            String cinemaReleaseDate = "Cinema release: " + movie.getCinemaReleaseDate();
            String digitalReleaseDate = "Digital release: " + movie.getDigitalReleaseDate();
            String physicalReleaseDate = "Physical release: " + movie.getPhysicalReleaseDate();
            String overview = movie.getOverview();
            final String imdbUrl = movie.getImdbUrl();

            setTitle(title);
            Picasso.with(this)
                    .load(imageUrl)
                    .into(imageView);
            titleTextView.setText(title);
            averageVoteTextView.setText(averageVote);
            voteCountTextView.setText(voteCount);
            cinemaReleaseDateTextView.setText(cinemaReleaseDate);
            digitalReleaseDateTextView.setText(digitalReleaseDate);
            physicalReleaseDateTextView.setText(physicalReleaseDate);
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
        } else {
            Cursor cursor = (Cursor)data;

            if (cursor != null && cursor.getCount() >= 1 && cursor.moveToFirst()) {
                inUserCollection = true;
                // Find the columns of movie attributes that we're interested in
                int movieIdColumnIndex = cursor.getColumnIndex(MovieEntry._ID);
                int watchedColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_WATCHED);

                // Extract out the value from the Cursor for the given column index
                movieId = cursor.getString(movieIdColumnIndex);
                isWatched = cursor.getInt(watchedColumnIndex) == 1; // 1 -> true, 0 -> false

                movie.setWatched(isWatched);
            } else {
                inUserCollection = false;
                isWatched = false;
            }
            final ToggleButton addRemoveMovieButton = findViewById(R.id.add_remove_movie);
            addRemoveMovieButton.setChecked(inUserCollection);
            addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        insertMovie(movie);
                        inUserCollection = true;
                    } else {
                        removeMovie(movieId);
                        inUserCollection = false;
                        movie.setWatched(false);
                    }
                }
            });

            final ToggleButton watchedNotWatchedButton = findViewById(R.id.watched_not_watched_movie);
            watchedNotWatchedButton.setChecked(isWatched);
            long todayInMilliseconds = System.currentTimeMillis();
            if (movie.getCinemaReleaseDateInMilliseconds() > todayInMilliseconds) {
                watchedNotWatchedButton.setEnabled(false);
            } else {
                watchedNotWatchedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            if (inUserCollection) {
                                setMovieWatched(movie, movieId, 1);
                            } else {
                                movie.setWatched(true);
                                addRemoveMovieButton.toggle();
                            }
                        } else {
                            setMovieWatched(movie, movieId, 0);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (loader.getId() == EXISTING_MOVIE_LOADER) {
            // TODO
        } else if (loader.getId() == EXISTING_MOVIE_LOADER_ID) {
            // TODO
        }
    }

    private void insertMovie(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.TMDB_ID, movie.getTmdbId());
        values.put(MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        values.put(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE, movie.getVote());
        values.put(MovieEntry.COLUMN_MOVIE_VOTE_COUNT, movie.getVoteCount());
        values.put(MovieEntry.COLUMN_MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS, movie.getCinemaReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_DIGITAL_RELEASE_DATE_IN_MILLISECONDS, movie.getDigitalReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_PHYSICAL_RELEASE_DATE_IN_MILLISECONDS, movie.getPhysicalReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        values.put(MovieEntry.COLUMN_MOVIE_IMDB_URL, movie.getImdbUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_ID, movie.getImageId());
        values.put(MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL, movie.getThumbnailUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_URL, movie.getImageUrl());
        values.put(MovieEntry.COLUMN_MOVIE_WATCHED, movie.getWatchedIntValue());

        getContentResolver().insert(MovieEntry.CONTENT_URI, values);
    }

    private void removeMovie(String movieId) {
        getContentResolver().delete(Uri.withAppendedPath(MovieEntry.CONTENT_URI, movieId), null, null);
    }

    private void setMovieWatched(Movie movie, String movieId, int isWatchedAsInt){
        ContentValues values = new ContentValues();
        values.put(MovieEntry.TMDB_ID, movie.getTmdbId());
        values.put(MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        values.put(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE, movie.getVote());
        values.put(MovieEntry.COLUMN_MOVIE_VOTE_COUNT, movie.getVoteCount());
        values.put(MovieEntry.COLUMN_MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS, movie.getCinemaReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_DIGITAL_RELEASE_DATE_IN_MILLISECONDS, movie.getDigitalReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_PHYSICAL_RELEASE_DATE_IN_MILLISECONDS, movie.getPhysicalReleaseDateInMilliseconds());
        values.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        values.put(MovieEntry.COLUMN_MOVIE_IMDB_URL, movie.getImdbUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_ID, movie.getImageId());
        values.put(MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL, movie.getThumbnailUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE_URL, movie.getImageUrl());
        values.put(MovieEntry.COLUMN_MOVIE_WATCHED, isWatchedAsInt);

        getContentResolver().update(Uri.withAppendedPath(MovieEntry.CONTENT_URI, movieId), values, null, null);
    }

}
