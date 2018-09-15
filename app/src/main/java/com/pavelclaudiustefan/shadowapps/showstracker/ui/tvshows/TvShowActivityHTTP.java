package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Season;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowActivityHTTP extends AppCompatActivity{

    public static final String TAG = "TvShowActivityHTTP";

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.button_container)
    FrameLayout buttonLayout;
    @BindView(R.id.add_remove_tv_show)
    ToggleButton addRemoveMovieButton;
    @BindView(R.id.generic_info_layout)
    RelativeLayout genericInfoLayout;
    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.release_date)
    TextView releaseDateTextView;
    @BindView(R.id.average_vote)
    TextView averageVoteTextView;
    @BindView(R.id.vote_count)
    TextView voteCountTextView;
    @BindView(R.id.overview_layout)
    RelativeLayout overviewLayout;
    @BindView(R.id.overview)
    TextView overviewTextView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    private long tmdbId;
    private boolean inUserCollection;
    private Box<TvShow> showsBox;
    private Box<Season> seasonsBox;
    private Box<Episode> episodesBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_show_http);
        ButterKnife.bind(this);
        setupActionBar();
        setUpToolbar();

        // Hide views until movie data is loaded
        loadingIndicator.setVisibility(View.VISIBLE);
        setTvShowViewsVisibility(View.GONE);

        showsBox = ((MyApp)getApplication()).getBoxStore().boxFor(TvShow.class);
        seasonsBox = ((MyApp)getApplication()).getBoxStore().boxFor(Season.class);
        episodesBox = ((MyApp)getApplication()).getBoxStore().boxFor(Episode.class);

        Intent intent = getIntent();
        tmdbId = intent.getLongExtra("tmdb_id", -1);

        supportPostponeEnterTransition();

        requestAndDisplayShow();
    }

    private void requestAndDisplayShow() {
        AndroidNetworking.get(TmdbConstants.TV_SHOWS_URL + tmdbId)
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache))
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        TvShow tvShow = QueryUtils.extractTvShowDataFromJson(response);
                        if (tvShow != null) {
                            displayShow(tvShow);
                        } else {
                            displayError();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayError();
                        Log.e("ShadowDebug", anError.getErrorBody());
                    }
                });
    }

    private void displayShow(TvShow tvShow) {
        loadingIndicator.setVisibility(View.GONE);

        String imageUrl = tvShow.getImageUrl();
        String title = tvShow.getTitle();
        String averageVote = String.valueOf(tvShow.getVote());
        int voteCount = tvShow.getVoteCount();
        String releaseDate = "Air date: " + tvShow.getReleaseDate();
        String overview = tvShow.getOverview();

        //setTitle(title);
        collapsingToolbar.setTitle(title);

        Picasso.get()
                .load(imageUrl)
                .fit()
                .noFade()
                .centerCrop()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                    }
                    @Override
                    public void onError(Exception e) {
                        supportStartPostponedEnterTransition();
                    }
                });

        titleTextView.setText(title);
        averageVoteTextView.setText(averageVote);
        String voteCountStr = voteCount + " votes";
        voteCountTextView.setText(voteCountStr);
        releaseDateTextView.setText(releaseDate);
        overviewTextView.setText(overview);

        setUpAddButton(tvShow);

        setTvShowViewsVisibility(View.VISIBLE);
    }

    // Set the toggle button state (Add to collection button)
    private void setUpAddButton(final TvShow tvShow) {
        if (showsBox.get(tvShow.getTmdbId()) != null) {
            inUserCollection = true;
        }

        addRemoveMovieButton.setChecked(inUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                addSeasonsAndEpisodesToTvShowAndInsert(tvShow);
                inUserCollection = true;
            } else {
                removeShow(tvShow);
                inUserCollection = false;
            }
        });
    }

    private void addSeasonsAndEpisodesToTvShowAndInsert(TvShow tvShow) {
        addRemoveMovieButton.setEnabled(false);
        new Handler().postDelayed(() -> addRemoveMovieButton.setEnabled(true), 2000);
        // get seasons
        AndroidNetworking.get(TmdbConstants.TV_SHOWS_URL + tmdbId)
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache))
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        List<Season> seasons = QueryUtils.extractSeasonsFromJson(response);
                        if (seasons != null && !seasons.isEmpty()) {
                            addEpisodesToTvShowAndInsert(tvShow, seasons);
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        displayError();
                        Log.e("ShadowDebug", anError.getErrorBody());
                    }
                });
    }

    private void addEpisodesToTvShowAndInsert(TvShow tvShow, List<Season> seasons) {
        for (Season season : seasons) {
            // get episodes
            AndroidNetworking.get(TmdbConstants.TV_SHOWS_URL + tmdbId + "/season/" + season.getSeasonNumber())
                    .addQueryParameter("api_key", TmdbConstants.API_KEY)
                    .setTag(this)
                    .setPriority(Priority.HIGH)
                    .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                    .build()
                    .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache))
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            List<Episode> episodes = QueryUtils.extractEpisodesFromJson(response, season.getNumberOfEpisodes());
                            if (episodes != null && !episodes.isEmpty()) {
                                season.addEpisodes(episodes);
                                showsBox.attach(tvShow);
                                tvShow.addSeason(season);
                                showsBox.put(tvShow);
                            }
                        }
                        @Override
                        public void onError(ANError anError) {
                            displayError();
                            Log.e("ShadowDebug", anError.getErrorBody());
                        }
                    });
        }
    }

    private void removeShow(TvShow tvShow) {
        addRemoveMovieButton.setEnabled(false);
        new Handler().postDelayed(() -> addRemoveMovieButton.setEnabled(true), 2000);
        for (Season season : tvShow.getSeasons()) {
            episodesBox.remove(season.getEpisodes());
            showsBox.attach(tvShow);
            seasonsBox.remove(season);
        }
        showsBox.remove(tvShow);
    }

    private void displayError() {
        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_tv_show_found);
        setTvShowViewsVisibility(View.GONE);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo == null || !networkInfo.isConnected()) {
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // Hides every view except the EmptyTextView
    // Used to display current error
    private void setTvShowViewsVisibility(int resid) {
        imageView.setVisibility(resid);
        buttonLayout.setVisibility(resid);

        genericInfoLayout.setVisibility(resid);
        overviewLayout.setVisibility(resid);
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar() {
        setSupportActionBar(toolbar);

        //toolbar.setTitle("");
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Finishes the activity if the Up button is pressed
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
