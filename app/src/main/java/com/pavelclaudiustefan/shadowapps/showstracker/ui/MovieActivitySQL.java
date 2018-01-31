package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;
import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

public class MovieActivitySQL extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_MOVIE_LOADER = 0;

    private Uri currentMovieUri;

    private ImageView imageView;
    private TextView titleTextView;
    private TextView averageVoteTextView;
    private TextView voteCountTextView;
    private TextView releaseDateTextView;
    private TextView overviewTextView;
    private Button imdbButton;

    private TextView emptyStateTextView;

    private boolean inUserCollection;
    private boolean isWatched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = getIntent();
        currentMovieUri = intent.getData();
        setTitle("Movie");

        inUserCollection = true;

        emptyStateTextView = findViewById(R.id.empty_view);

        imageView = findViewById(R.id.movie_image);
        titleTextView = findViewById(R.id.movie_title);
        averageVoteTextView = findViewById(R.id.average_vote);
        voteCountTextView = findViewById(R.id.vote_count);
        releaseDateTextView = findViewById(R.id.release_date);
        overviewTextView = findViewById(R.id.overview);
        imdbButton = findViewById(R.id.imdb_url_button);

        getLoaderManager().initLoader(EXISTING_MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.TMDB_ID,
                MovieEntry.COLUMN_MOVIE_TITLE,
                MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE,
                MovieEntry.COLUMN_MOVIE_VOTE_COUNT,
                MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS,
                MovieEntry.COLUMN_MOVIE_OVERVIEW,
                MovieEntry.COLUMN_MOVIE_IMDB_URL,
                MovieEntry.COLUMN_MOVIE_IMAGE_ID,
                MovieEntry.COLUMN_MOVIE_WATCHED};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentMovieUri,         // Query the content URI for the current movie
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        if (cursor.moveToFirst()) {
            // Find the columns of movie attributes that we're interested in
            int movieIdColumnIndex = cursor.getColumnIndex(MovieEntry._ID);
            int tmdbIdColumnIndex = cursor.getColumnIndex(MovieEntry.TMDB_ID);
            int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
            int averageVoteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
            int voteCountColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_VOTE_COUNT);
            int releaseDateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS);
            int overviewColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_OVERVIEW);
            int imdbUrlColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_IMDB_URL);
            int watchedColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_WATCHED);
            int imageIdColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_IMAGE_ID);

            // Extract out the value from the Cursor for the given column index
            final String movieId = cursor.getString(movieIdColumnIndex);
            final int tmdbId = cursor.getInt(tmdbIdColumnIndex);
            final String title = cursor.getString(titleColumnIndex);
            final double averageVote = cursor.getDouble(averageVoteColumnIndex);
            final int voteCount = cursor.getInt(voteCountColumnIndex);
            final long releaseDate = cursor.getLong(releaseDateColumnIndex);
            final String overview = cursor.getString(overviewColumnIndex);
            final String imdbUrl = cursor.getString(imdbUrlColumnIndex);
            final String imageId = cursor.getString(imageIdColumnIndex);
            isWatched = cursor.getInt(watchedColumnIndex) == 1; // 1 -> true, 0 -> false

            final Movie movie = new Movie(tmdbId, title, averageVote, releaseDate, imageId, imdbUrl, voteCount, overview);
            movie.setWatched(isWatched);

            String cinemaReleaseDate = "Released in cinema: " + movie.getDate();

            // Update the views on the screen with the values from the database
            Picasso.with(this)
                    .load(movie.getImageUrl())
                    .into(imageView);
            titleTextView.setText(movie.getTitle());
            averageVoteTextView.setText(String.valueOf(movie.getVote()));
            voteCountTextView.setText(String.valueOf(movie.getVoteCount()));
            releaseDateTextView.setText(cinemaReleaseDate);
            overviewTextView.setText(movie.getOverview());

            imdbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(movie.getImdbUrl()));
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
                        inUserCollection = true;
                    } else {
                        removeMovie(movieId);
                        inUserCollection = false;
                        isWatched = false;
                        ToggleButton watchedNotWatchedButton = findViewById(R.id.watched_not_watched_movie);
                        watchedNotWatchedButton.setChecked(isWatched);
                    }
                }
            });

            final ToggleButton watchedNotWatchedButton = findViewById(R.id.watched_not_watched_movie);
            watchedNotWatchedButton.setChecked(isWatched);
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //TODO
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

    private void setMovieWatched(Movie movie, String movieId, int isWatchedAsInt){
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
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_WATCHED, isWatchedAsInt);

        getContentResolver().update(Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId), values, null, null);
    }
}
