package com.pavelclaudiustefan.shadowapps.showstracker;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

public class MovieActivitySQL extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_MOVIE_LOADER = 0;

    private Uri currentMovieUri;

    private ImageView imageView;
    private TextView titleTextView;
    private TextView averageVoteTextView;
    private TextView releaseDateTextView;
    private Button imdbButton;

    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = getIntent();
        currentMovieUri = intent.getData();
        setTitle("Movie");

        emptyStateTextView = findViewById(R.id.empty_view);

        imageView = findViewById(R.id.movie_image);
        titleTextView = findViewById(R.id.movie_title);
        averageVoteTextView = findViewById(R.id.average_vote);
        releaseDateTextView = findViewById(R.id.release_date);
        imdbButton = findViewById(R.id.imdb_url_button);

        getLoaderManager().initLoader(EXISTING_MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                MovieEntry._ID,
                MovieEntry.COLUMN_MOVIE_TITLE,
                MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE,
                MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                MovieEntry.COLUMN_MOVIE_IMDB_ID,
                MovieEntry.COLUMN_MOVIE_IMAGE_URL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentMovieUri,         // Query the content URI for the current pet
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
            // Find the columns of pet attributes that we're interested in
            int imageUrlColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_IMAGE_URL);
            int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
            int averageVoteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
            int releaseDateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
            int imdbIdColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_IMDB_ID);

            // Extract out the value from the Cursor for the given column index
            String imageUrl = cursor.getString(imageUrlColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String averageVote = cursor.getString(averageVoteColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            final String imdbUrl = cursor.getString(imdbIdColumnIndex);

            // Update the views on the screen with the values from the database
            Picasso.with(this)
                    .load(imageUrl)
                    .into(imageView);
            titleTextView.setText(title);
            averageVoteTextView.setText(averageVote);
            releaseDateTextView.setText(releaseDate);

            imdbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(imdbUrl));
                    startActivity(intent);
                }
            });

            if (title != null && title != "") {
                emptyStateTextView.setVisibility(View.GONE);
            } else {
                // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
                emptyStateTextView.setText(R.string.no_movie_data);
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //TODO
    }
}
