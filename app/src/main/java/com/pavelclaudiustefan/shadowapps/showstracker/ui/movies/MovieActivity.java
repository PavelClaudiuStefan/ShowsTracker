package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public abstract class MovieActivity extends AppCompatActivity{

    public static final String TAG = "MovieActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.buttons_layout)
    LinearLayout buttonsLayout;
    @BindView(R.id.generic_info_layout)
    RelativeLayout genericInfoLayout;
    @BindView(R.id.overview_layout)
    RelativeLayout overviewLayout;
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
    ProgressBar loadingIndicator;

    private boolean isInUserCollection;
    private Box<Movie> moviesBox;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        setUpToolbar();
        setupActionBar();

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

        // Hide views until movie data is loaded
        setMovieViewsVisibility(View.GONE);

        Intent intent = getIntent();
        long tmdbId = intent.getLongExtra("tmdb_id", -1);
        imageView.setTransitionName(String.valueOf(tmdbId));

        // For transitions
        imageView.setTransitionName(String.valueOf(tmdbId));

        if (tmdbId != -1) {
            requestAndDisplayMovie(tmdbId);
        } else {
            Log.e("ShadowDebug", "MovieActivityDb tmdbId=-1 - ERROR");
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    abstract void requestAndDisplayMovie(long tmdbId);

    void displayMovie(final Movie movie) {
        setTitle(movie.getTitle());

        // Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);

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

        //imageView.setTransitionName(String.valueOf(movie.getTmdbId()));

        setTitle(title);
        imageView.setTransitionName(String.valueOf(movie.getTmdbId()));
        Picasso.get()
                .load(imageUrl)
                .into(imageView);

        supportPostponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                }
        );

        titleTextView.setText(title);
        averageVoteTextView.setText(averageVote);
        String voteCountStr = voteCount + " votes";
        voteCountTextView.setText(voteCountStr);
        cinemaReleaseDateTextView.setText(cinemaReleaseDate);
        digitalReleaseDateTextView.setText(digitalReleaseDate);
        physicalReleaseDateTextView.setText(physicalReleaseDate);
        overviewTextView.setText(overview);

        imdbButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(imdbUrl));
            startActivity(intent);
        });

        addRemoveMovieButton.setChecked(isInUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                insertMovie(movie);
                isInUserCollection = true;
            } else {
                removeMovie(movie);
                isInUserCollection = false;
                if (movie.isWatched()) {
                    watchedNotWatchedButton.toggle();
                }
                //movie.setWatched(false);
            }
        });


        watchedNotWatchedButton.setChecked(movie.isWatched());
        long todayInMilliseconds = System.currentTimeMillis();
        if (movie.getReleaseDateInMilliseconds() > todayInMilliseconds) {
            watchedNotWatchedButton.setEnabled(false);
        } else {
            watchedNotWatchedButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    if (isInUserCollection) {
                        setMovieWatched(movie, true);
                    } else {
                        movie.setWatched(true);
                        addRemoveMovieButton.toggle();
                    }
                } else {
                    setMovieWatched(movie, false);
                }
            });
        }

        setMovieViewsVisibility(View.VISIBLE);
    }

    // Updates movie
    private void setMovieWatched(Movie movie, boolean isWatched){
        movie.setWatched(isWatched);
        insertMovie(movie);
    }

    private void insertMovie(Movie movie) {
        // Insert movie in MovieBox
        moviesBox.put(movie);

        // Insert movie in firestore users/{userId}/movies and if valid, in groups/{groupTitle}/movies
        if (firebaseAuth.getUid() != null) {
            // Insert valid movie in users/{userId}/movies
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("tmdbId", movie.getTmdbId());
            movieData.put("isWatched", movie.isWatched());
            firestore.collection("users").document(firebaseAuth.getUid()).collection("movies")
                    .document(String.valueOf(movie.getTmdbId()))
                    .set(movieData)
                    .addOnFailureListener(e -> Log.e(TAG, "Insert movie in users/" + movie.getTmdbId() + "/movies failure: ", e));

            // For every group that user is a member of -> update movies
            firestore.collection("users").document(firebaseAuth.getUid()).collection("groups")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (!movie.isWatched()) {
                                // Valid movie is removed -> raise the number of users that have this movie as valid (valid = unwatched collection movie)
                                updateGroupMovie(documentSnapshot.getId(), movie.getTmdbId(), +1);
                            } else {
                                if (isInUserCollection) {
                                    // Valid movie is made invalid -> lower the number of users
                                    updateGroupMovie(documentSnapshot.getId(), movie.getTmdbId(), -1);
                                } else {
                                    // Invalid movie is added -> Doesn't affect number of valid movies
                                    updateGroupMovie(documentSnapshot.getId(), movie.getTmdbId(), 0);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Getting user groups failure: ", e));
        }
    }

    private void removeMovie(Movie movie) {
        moviesBox.remove(movie);

        // Remove movie from firestore
        if (firebaseAuth.getUid() != null) {
            // Remove movie from users/{userId}/movies
            firestore.collection("users").document(firebaseAuth.getUid()).collection("movies")
                    .document(String.valueOf(movie.getTmdbId()))
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Delete movie from users/" + movie.getTmdbId() + "/movies failure: ", e));

            // For every group that user is a member of -> update movies
            firestore.collection("users").document(firebaseAuth.getUid()).collection("groups")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (!movie.isWatched()) {
                                // Valid movie is removed -> lower the number of users that have this movie as valid (valid = unwatched collection movie)
                                updateGroupMovie(documentSnapshot.getId(), movie.getTmdbId(), -1);
                            } else {
                                // Invalid movie is removed -> Doesn't affect number of valid movies
                                updateGroupMovie(documentSnapshot.getId(), movie.getTmdbId(), 0);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Getting user groups failure: ", e));
        }
    }

    // Add or update movie data in group/{groupTitle}/movies
    private void updateGroupMovie(String groupTitle, long tmdbId, int increment) {
        CollectionReference groupMovies = firestore.collection("groups").document(groupTitle).collection("movies");
        groupMovies.document(String.valueOf(tmdbId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> updatedMovieData = new HashMap<>();
                    updatedMovieData.put("tmdbId", tmdbId);
                    if (documentSnapshot.exists()) {
                        Long nrOfUsers = documentSnapshot.getLong("nrOfUsers");
                        if (nrOfUsers != null) {
                            updatedMovieData.put("nrOfUsers", nrOfUsers + increment);
                            groupMovies.document(String.valueOf(tmdbId))
                                    .set(updatedMovieData)
                                    .addOnFailureListener(e -> Log.e(TAG, "Updating movies (incrementing nrOfUsers) in groups/{title}/movies failure: ", e));
                        }
                    } else {
                        long nrOfUsers = 1L;
                        updatedMovieData.put("nrOfUsers", nrOfUsers);
                        groupMovies.document(String.valueOf(tmdbId))
                                .set(updatedMovieData)
                                .addOnFailureListener(e -> Log.e(TAG, "Updating movies (initiating nrOfUsers) in groups/{title}/movies failure: ", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Updating movies in groups/{title}/movies failure: ", e));
    }

    void displayError() {
        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setText(R.string.no_movie_data);
        setMovieViewsVisibility(View.GONE);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo == null || !networkInfo.isConnected()) {
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // Used when no movie data exists in the activity
    // Either because of no internet connection or other errors
    private void setMovieViewsVisibility(int resid) {
        imageView.setVisibility(resid);

        buttonsLayout.setVisibility(resid);
        genericInfoLayout.setVisibility(resid);
        overviewLayout.setVisibility(resid);

        imdbButton.setVisibility(resid);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar() {
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Finishes the activity if the Up button is pressed
        if (id == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setInUserCollection(boolean inUserCollection) {
        isInUserCollection = inUserCollection;
    }

    Movie getMovieFromDb(long tmdbId) {
        return moviesBox.get(tmdbId);
    }
}
