package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieActivityHTTP extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie>{

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

    private static final int EXISTING_MOVIE_LOADER = 0;

    private String tmdbId;

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
        tmdbId = intent.getExtras().getString("tmdb_id");
        // TODO - Check if movie is in database

        imageView = findViewById(R.id.movie_image);
        titleTextView = findViewById(R.id.movie_title);
        averageVoteTextView = findViewById(R.id.average_vote);
        releaseDateTextView = findViewById(R.id.release_date);
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
    public void onLoadFinished(Loader<Movie> loader, Movie movie) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (movie == null) {
            return;
        }

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Extract out the value from the Cursor for the given column index
        String imageUrl = movie.getImageUrl();
        String title = movie.getTitle();
        String averageVote = String.valueOf(movie.getVote());
        String releaseDate = movie.getDate();
        final String imdbUrl = movie.getImdbUrl();

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

        if (title != null && !title.isEmpty()) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movie_data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {
        // TODO
    }
}
