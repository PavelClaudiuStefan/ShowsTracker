package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class MovieActivityHTTP extends AppCompatActivity{

    public static final String TAG = "MovieActivityHTTP";

    private long tmdbId;

    @BindView(R.id.thumbnail)
    ImageView imageView;
    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.add_remove_movie)
    ToggleButton addRemoveMovieButton;
    @BindView(R.id.watched_not_watched_movie)
    ToggleButton watchedNotWatchedButton;
    @BindView(R.id.release_date)
    TextView cinemaReleaseDateTextView;
    @BindView(R.id.digital_release_date)
    TextView digitalReleaseDateTextView;
    @BindView(R.id.physical_release_date)
    TextView physicalReleaseDateTextView;
    @BindView(R.id.average_vote)
    TextView averageVoteTextView;
    @BindView(R.id.vote_count)
    TextView voteCountTextView;
    @BindView(R.id.overview)
    TextView overviewTextView;
    @BindView(R.id.imdb_url_button)
    Button imdbButton;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    private boolean inUserCollection;

    private Box<Movie> moviesBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        setTitle("Movie");

        ButterKnife.bind(this);

        Intent intent = getIntent();
        String tmdbIdString = intent.getStringExtra("tmdb_id");
        if (tmdbIdString != null && !tmdbIdString.isEmpty()) {
            tmdbId = Long.parseLong(tmdbIdString);
        }

        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

        requestAndDisplayMovie();
    }

    private void requestAndDisplayMovie() {
        String tmdbUrl = TmdbConstants.MOVIES_URL + tmdbId;
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", TmdbConstants.API_KEY);
        uriBuilder.appendQueryParameter("append_to_response", "release_dates");
        AndroidNetworking.get(uriBuilder.toString())
                .setTag(this)
                .setPriority(Priority.HIGH)
                .getResponseOnlyFromNetwork()
                .build()
                .setAnalyticsListener(new AnalyticsListener() {
                    @Override
                    public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                        Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache);
                    }
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Movie movie = QueryUtils.extractMovieDataFromJson(response);
                        displayMovie(movie);
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayError();
                        Log.e("ShadowDebug", anError.getErrorBody());
                    }
                });
    }

    private void displayMovie(final Movie movie) {
        // Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);
        if (movie != null) {
            emptyStateTextView.setVisibility(View.GONE);

            String imageUrl = movie.getImageUrl();
            String title = movie.getTitle();
            String averageVote = String.valueOf(movie.getVote());
            int voteCount = movie.getVoteCount();
            String cinemaReleaseDate = "Cinema release: " + movie.getReleaseDate();
            String digitalReleaseDate = "Digital release: " + movie.getDigitalReleaseDate();
            String physicalReleaseDate = "Physical release: " + movie.getPhysicalReleaseDate();
            String overview = movie.getOverview();
            final String imdbUrl = movie.getImdbUrl();

            setTitle(title);
            Picasso.get()
                    .load(imageUrl)
                    .into(imageView);
            titleTextView.setText(title);
            averageVoteTextView.setText(averageVote);
            String voteCountStr = voteCount + " votes";
            voteCountTextView.setText(voteCountStr);
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

            Movie dbMovie = requestMovieFromDb();
            boolean isWatched;
            if (dbMovie != null) { // if movie is in collection
                inUserCollection = true;
                isWatched = dbMovie.isWatched();
                // Using a different movie object(from http) other than the one from the database
                // because this way it adds the newest movie data in the database
                movie.setWatched(isWatched);
            } else {
                inUserCollection = false;
                isWatched = false;
            }

            addRemoveMovieButton.setChecked(inUserCollection);
            addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        insertMovie(movie);
                        inUserCollection = true;
                    } else {
                        removeMovie(tmdbId);
                        inUserCollection = false;
                        if (movie.isWatched()) {
                            watchedNotWatchedButton.toggle();
                        }
                        //movie.setWatched(false);
                    }
                }
            });


            watchedNotWatchedButton.setChecked(isWatched);
            long todayInMilliseconds = System.currentTimeMillis();
            if (movie.getReleaseDateInMilliseconds() > todayInMilliseconds) {
                watchedNotWatchedButton.setEnabled(false);
            } else {
                watchedNotWatchedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            if (inUserCollection) {
                                setMovieWatched(movie, true);
                            } else {
                                movie.setWatched(true);
                                addRemoveMovieButton.toggle();
                            }
                        } else {
                            setMovieWatched(movie, false);
                        }
                    }
                });
            }
        } else {
            // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movie_data);
            hideMovieViews();
        }
    }

    private Movie requestMovieFromDb() {
        return moviesBox.get(tmdbId);
    }

    private void insertMovie(Movie movie) {
        moviesBox.put(movie);
    }

    private void removeMovie(long id) {
        moviesBox.remove(id);
    }

    // Updates movie
    private void setMovieWatched(Movie movie, boolean isWatched){
        movie.setWatched(isWatched);
        moviesBox.put(movie);
    }

    private void displayError() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo == null || !networkInfo.isConnected()) {
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);
            hideMovieViews();
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // Used when no movie data exists in the activity
    // Either because of no internet connection or other errors
    private void hideMovieViews() {
        imageView.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        addRemoveMovieButton.setVisibility(View.GONE);
        watchedNotWatchedButton.setVisibility(View.GONE);
        cinemaReleaseDateTextView.setVisibility(View.GONE);
        digitalReleaseDateTextView.setVisibility(View.GONE);
        physicalReleaseDateTextView.setVisibility(View.GONE);
        averageVoteTextView.setVisibility(View.GONE);
        voteCountTextView.setVisibility(View.GONE);
        overviewTextView.setVisibility(View.GONE);
        imdbButton.setVisibility(View.GONE);
    }

}
