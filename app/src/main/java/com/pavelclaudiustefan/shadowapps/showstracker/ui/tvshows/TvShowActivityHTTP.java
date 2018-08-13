package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class TvShowActivityHTTP extends AppCompatActivity{

    public static final String TAG = "TvShowActivityHTTP";

    private String tmdbId;

    @BindView(R.id.thumbnail)
    ImageView imageView;
    @BindView(R.id.buttons_layout)
    LinearLayout buttonsLayout;
    @BindView(R.id.add_remove_show)
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

    private boolean inUserCollection;

    private Box<TvShow> showsBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_show);
        setTitle("TV tvShow");

        ButterKnife.bind(this);

        // Hide views until movie data is loaded
        loadingIndicator.setVisibility(View.VISIBLE);
        setTvShowViewsVisibility(View.GONE);

        showsBox = ((MyApp)getApplication()).getBoxStore().boxFor(TvShow.class);

        Intent intent = getIntent();
        tmdbId = intent.getStringExtra("tmdb_id");

        requestAndDisplayShow();
    }

    private void requestAndDisplayShow() {
        String tmdbUrl = TmdbConstants.TV_SHOWS_URL + tmdbId;
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", TmdbConstants.API_KEY);

        AndroidNetworking.get(uriBuilder.toString())
                .setTag(this)
                .setPriority(Priority.HIGH)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
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
        if (showsBox.get(tvShow.getTmdbId()) != null) {
            inUserCollection = true;
        }

        addRemoveMovieButton.setChecked(inUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    insertShow(tvShow);
                    inUserCollection = true;
                } else {
                    removeShow(tvShow.getTmdbId());
                    inUserCollection = false;
                }
            }
        });
    }

    private void insertShow(TvShow tvShow) {
        showsBox.put(tvShow);
    }

    private void removeShow(long showTmdbId) {
        showsBox.remove(showTmdbId);
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
        buttonsLayout.setVisibility(resid);

        genericInfoLayout.setVisibility(resid);
        overviewLayout.setVisibility(resid);
    }
}
