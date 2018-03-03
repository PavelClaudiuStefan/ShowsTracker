package com.pavelclaudiustefan.shadowapps.showstracker.ui;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.ShowEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Show;
import com.pavelclaudiustefan.shadowapps.showstracker.loaders.ShowDataLoader;
import com.squareup.picasso.Picasso;

public class ShowActivityHTTP extends AppCompatActivity implements LoaderManager.LoaderCallbacks{

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final int EXISTING_SHOW_LOADER = 0;
    private static final int EXISTING_SHOW_LOADER_ID = 1;

    private String tmdbId;

    private ImageView imageView;
    private TextView titleTextView;
    private TextView releaseDateTextView;
    private TextView averageVoteTextView;
    private TextView voteCountTextView;
    private TextView overviewTextView;

    private TextView emptyStateTextView;

    private boolean inUserCollection;

    private Show show;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        setTitle("TV show");

        Intent intent = getIntent();
        tmdbId = intent.getExtras().getString("tmdb_id");

        imageView = findViewById(R.id.thumbnail);
        titleTextView = findViewById(R.id.title);
        averageVoteTextView = findViewById(R.id.average_vote);
        voteCountTextView = findViewById(R.id.vote_count);
        releaseDateTextView = findViewById(R.id.release_date);
        overviewTextView = findViewById(R.id.overview);

        getLoaderManager().initLoader(EXISTING_SHOW_LOADER, null, this);

        //Only visible if no show is found
        emptyStateTextView = findViewById(R.id.empty_view);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EXISTING_SHOW_LOADER, null, this);
        } else {
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == EXISTING_SHOW_LOADER) {
            String tmdbUrl = "https://api.themoviedb.org/3/tv/" + tmdbId;
            Uri baseUri = Uri.parse(tmdbUrl);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("api_key", API_KEY);

            return new ShowDataLoader(this, uriBuilder.toString());
        } else if (id == EXISTING_SHOW_LOADER_ID){
            //TODO - database for shows
            String[] projection = {
                    ShowEntry._ID,
                    ShowEntry.COLUMN_SHOW_WATCHED};

            String selection = ShowEntry.TMDB_ID + "=?";

            String[] selectionArgs = {String.valueOf(show.getTmdbId())};

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    ShowEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (loader.getId() == EXISTING_SHOW_LOADER) {
            this.show = (Show)data;

            if (show == null) {
                return;
            }

            getLoaderManager().initLoader(EXISTING_SHOW_LOADER_ID, null, this);

            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            String imageUrl = show.getImageUrl();
            String title = show.getTitle();
            String averageVote = String.valueOf(show.getVote());
            int voteCount = show.getVoteCount();
            String releaseDate = "Air date: " + show.getReleaseDate();
            String overview = show.getOverview();

            setTitle(title);
            Picasso.with(this)
                    .load(imageUrl)
                    .into(imageView);
            titleTextView.setText(title);
            averageVoteTextView.setText(averageVote);
            String voteCountStr = voteCount + " votes";
            voteCountTextView.setText(voteCountStr);
            releaseDateTextView.setText(releaseDate);
            overviewTextView.setText(overview);

            if (title != null && !title.isEmpty()) {
                emptyStateTextView.setVisibility(View.GONE);
            } else {
                // Set empty state text to display "No movies found." It's not visible if any show is added to the adapter
                emptyStateTextView.setText(R.string.no_movie_data);
            }
        } else {
            Cursor cursor = (Cursor)data;

            if (cursor != null && cursor.getCount() >= 1 && cursor.moveToFirst()) {
                inUserCollection = true;
                // Find the columns of show attributes that we're interested in
                int movieIdColumnIndex = cursor.getColumnIndex(ShowEntry._ID);

                // Extract out the value from the Cursor for the given column index
                movieId = cursor.getString(movieIdColumnIndex);
            } else {
                inUserCollection = false;
            }
            final ToggleButton addRemoveMovieButton = findViewById(R.id.add_remove_movie);
            addRemoveMovieButton.setChecked(inUserCollection);
            addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        insertShow(show);
                        inUserCollection = true;
                    } else {
                        removeShow(movieId);
                        inUserCollection = false;
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        loader.abandon();
    }

    private void insertShow(Show show) {
        ContentValues values = new ContentValues();
        values.put(ShowEntry.TMDB_ID, show.getTmdbId());
        values.put(ShowEntry.COLUMN_SHOW_TITLE, show.getTitle());
        values.put(ShowEntry.COLUMN_SHOW_AVERAGE_VOTE, show.getVote());
        values.put(ShowEntry.COLUMN_SHOW_VOTE_COUNT, show.getVoteCount());
        values.put(ShowEntry.COLUMN_SHOW_RELEASE_DATE_IN_MILLISECONDS, show.getReleaseDateInMilliseconds());
        values.put(ShowEntry.COLUMN_SHOW_OVERVIEW, show.getOverview());
        values.put(ShowEntry.COLUMN_SHOW_IMAGE_ID, show.getImageId());
        values.put(ShowEntry.COLUMN_SHOW_THUMBNAIL_URL, show.getThumbnailUrl());
        values.put(ShowEntry.COLUMN_SHOW_IMAGE_URL, show.getImageUrl());
        values.put(ShowEntry.COLUMN_SHOW_WATCHED, show.getWatchedAsIntValue());

        getContentResolver().insert(ShowEntry.CONTENT_URI, values);
    }

    private void removeShow(String movieId) {
        getContentResolver().delete(Uri.withAppendedPath(ShowEntry.CONTENT_URI, movieId), null, null);
    }

}
