package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.Intent;
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

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class MovieActivityDb extends AppCompatActivity {

    @BindView(R.id.thumbnail)
    ImageView imageView;
    @BindView(R.id.add_remove_movie)
    ToggleButton addRemoveMovieButton;
    @BindView(R.id.watched_not_watched_movie)
    ToggleButton watchedNotWatchedButton;
    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.average_vote)
    TextView averageVoteTextView;
    @BindView(R.id.vote_count)
    TextView voteCountTextView;
    @BindView(R.id.release_date)
    TextView cinemaReleaseDateTextView;
    @BindView(R.id.digital_release_date)
    TextView digitalReleaseDateTextView;
    @BindView(R.id.physical_release_date)
    TextView physicalReleaseDateTextView;
    @BindView(R.id.overview)
    TextView overviewTextView;
    @BindView(R.id.imdb_url_button)
    Button imdbButton;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;

    private Box<Movie> moviesBox;

    private boolean inUserCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        setTitle("Movie");

        ButterKnife.bind(this);
        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

        Intent intent = getIntent();
        long tmdbId = intent.getLongExtra("tmdb_id", -1);

        inUserCollection = true;

        if (tmdbId != -1) {
            requestAndDisplayMovieFromDb(tmdbId);
        } else {
            Log.i("ShadowDebug", "MovieActivityDb tmdbId=-1 - ERROR");
        }
    }

    private void requestAndDisplayMovieFromDb(long tmdbId) {
        Movie movie = moviesBox.get(tmdbId);
        displayMovie(movie);
    }

    private void displayMovie(final Movie movie) {
        String voteCountStr = movie.getVoteCount() + " votes";
        String cinemaReleaseDate = "Cinema release: " + movie.getReleaseDate();
        String digitalReleaseDate = "Digital release: " + movie.getDigitalReleaseDate();
        String physicalReleaseDate = "Physical release: " + movie.getPhysicalReleaseDate();

        // Update the views on the screen with the values from the database
        Picasso.get()
                .load(movie.getImageUrl())
                .into(imageView);

        titleTextView.setText(movie.getTitle());
        averageVoteTextView.setText(String.valueOf(movie.getVote()));
        voteCountTextView.setText(String.valueOf(voteCountStr));
        cinemaReleaseDateTextView.setText(cinemaReleaseDate);
        digitalReleaseDateTextView.setText(digitalReleaseDate);
        physicalReleaseDateTextView.setText(physicalReleaseDate);
        overviewTextView.setText(movie.getOverview());

        imdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(movie.getImdbUrl()));
                startActivity(intent);
            }
        });

        if (movie.getTitle() != null && !movie.getTitle().isEmpty()) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            // Set empty state text to display "No movies found." It's not visible if any movie is added to the adapter
            emptyStateTextView.setText(R.string.no_movie_data);
        }

        addRemoveMovieButton.setChecked(inUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    insertMovie(movie);
                    inUserCollection = true;
                } else {
                    removeMovie(movie);
                    inUserCollection = false;
                    movie.setWatched(false);
                    ToggleButton watchedNotWatchedButton = findViewById(R.id.watched_not_watched_movie);
                    watchedNotWatchedButton.setChecked(false);
                }
            }
        });

        watchedNotWatchedButton.setChecked(movie.isWatched());
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

    private void insertMovie(Movie movie) {
        moviesBox.put(movie);
    }

    private void removeMovie(Movie movie) {
        moviesBox.remove(movie);
    }

    private void setMovieWatched(Movie movie, boolean isWatched){
        movie.setWatched(isWatched);
        moviesBox.put(movie);
    }
}
