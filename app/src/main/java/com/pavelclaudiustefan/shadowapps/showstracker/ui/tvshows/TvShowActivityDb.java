package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowActivityDb extends AppCompatActivity {

    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
    @BindView(R.id.add_remove_tv_show)
    ToggleButton addRemoveMovieButton;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    private Box<TvShow> tvShowsBox;
    private long tmdbId;
    private boolean inCollection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_show_db);
        ButterKnife.bind(this);
        setupActionBar();
        setUpToolbar();

        // Hide views until movie data is loaded
        loadingIndicator.setVisibility(View.VISIBLE);
        setTvShowViewsVisibility(View.GONE);

        tvShowsBox = ((MyApp)getApplication()).getBoxStore().boxFor(TvShow.class);

        Intent intent = getIntent();
        tmdbId = intent.getLongExtra("tmdb_id", -1);

        setUpTvShowData();
    }

    // Get data from object box
    private void setUpTvShowData() {
        TvShow tvShow = tvShowsBox.get(tmdbId);
        if (tvShow != null) {
            inCollection = true;
            setTitle(tvShow.getTitle());
            displayTvShow(tvShow);
        } else {
            displayError();
        }
    }

    private void displayTvShow(TvShow tvShow) {
        loadingIndicator.setVisibility(View.GONE);

        String imageUrl = tvShow.getImageUrl();
        String title = tvShow.getTitle();
        String averageVote = String.valueOf(tvShow.getVote());
        int voteCount = tvShow.getVoteCount();
        String releaseDate = "Air date: " + tvShow.getReleaseDate();
        String overview = tvShow.getOverview();

        setTitle(title);
        Picasso.get()
                .load(imageUrl)
                .into(imageView);
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
        if (tvShowsBox.get(tvShow.getTmdbId()) != null) {
            inCollection = true;
        }

        addRemoveMovieButton.setChecked(inCollection);
        addRemoveMovieButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                insertShow(tvShow);
                inCollection = true;
            } else {
                removeShow(tvShow);
                inCollection = false;
            }
        });
    }

    private void insertShow(TvShow tvShow) {
        tvShowsBox.put(tvShow);
    }

    private void removeShow(TvShow tvShow) {
        tvShowsBox.remove(tvShow);
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

        genericInfoLayout.setVisibility(resid);
        overviewLayout.setVisibility(resid);
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

        toolbar.setTitle("");
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
}
