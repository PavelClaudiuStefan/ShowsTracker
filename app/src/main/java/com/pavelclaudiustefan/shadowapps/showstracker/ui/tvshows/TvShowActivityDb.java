package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.SeasonsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Season;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowActivityDb extends AppCompatActivity {

    @BindView(R.id.scroll_view)
    NestedScrollView scrollView;
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
    @BindView(R.id.seasons_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.add_remove_tv_show)
    ToggleButton addRemoveMovieButton;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    private Box<TvShow> tvShowsBox;
    private Box<Season> seasonsBox;
    private Box<Episode> episodesBox;

    private List<Season> seasons;
    private List<List<Episode>> episodes;

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
        seasonsBox = ((MyApp)getApplication()).getBoxStore().boxFor(Season.class);
        episodesBox = ((MyApp)getApplication()).getBoxStore().boxFor(Episode.class);


        Intent intent = getIntent();
        tmdbId = intent.getLongExtra("tmdb_id", -1);

        setUpTvShowData();
        setUpRecyclerView();
    }

    // Get data from object box
    private void setUpTvShowData() {
        TvShow tvShow = tvShowsBox.get(tmdbId);
        if (tvShow != null) {
            seasons = tvShow.getSeasons();
            inCollection = true;
            setTitle(tvShow.getTitle());
            displayTvShow(tvShow);
        } else {
            displayError();
        }
    }

    private void displayTvShow(TvShow tvShow) {
        // Stops the activity being started scrolled down
        scrollView.setFocusableInTouchMode(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

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
        addRemoveMovieButton.setEnabled(false);
        new Handler().postDelayed(() -> addRemoveMovieButton.setEnabled(true), 2000);
        for (int i = 0; i < seasons.size(); i++) {
            seasons.get(i).addEpisodes(episodes.get(i));
        }
        tvShowsBox.attach(tvShow);
        tvShow.addSeasons(seasons);
        tvShowsBox.put(tvShow);
    }

    private void removeShow(TvShow tvShow) {
        addRemoveMovieButton.setEnabled(false);
        new Handler().postDelayed(() -> addRemoveMovieButton.setEnabled(true), 2000);
        if (seasons == null) {
            seasons = tvShow.getSeasons();
        }
        episodes = new ArrayList<>();
        for (Season season : seasons) {
            episodes.add(season.getEpisodes());
            episodesBox.remove(season.getEpisodes());
            tvShowsBox.attach(tvShow);
            seasonsBox.remove(season);
        }
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

    private void setUpRecyclerView() {
        SeasonsCardsAdapter seasonsCardsAdapter = new SeasonsCardsAdapter(this, seasons, R.menu.menu_tv_shows_list, new SeasonsCardsAdapter.SeasonsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(TvShowActivityDb.this, "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(TvShowActivityDb.this, "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                // TODO
                Log.i("ShadowDebug", "Card selected");
            }
        });

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TvShowActivityDb.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(seasonsCardsAdapter);

    }

}
