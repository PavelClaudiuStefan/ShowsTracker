package com.pavelclaudiustefan.shadowapps.showstracker.ui.shows;

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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import io.objectbox.Box;

public class ShowActivityHTTP extends AppCompatActivity{

    public static final String TAG = "ShowActivityHTTP";

    //TODO - Hide the API key
    private final static String API_KEY = "e0ff28973a330d2640142476f896da04";

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
    private Box<Show> showsBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        setTitle("TV show");

        showsBox = ((MyApp)getApplication()).getBoxStore().boxFor(Show.class);

        // TODO - Better way of sending tmdbId through intent
        Intent intent = getIntent();
        tmdbId = intent.getExtras().getString("tmdb_id");

        imageView = findViewById(R.id.thumbnail);
        titleTextView = findViewById(R.id.title);
        averageVoteTextView = findViewById(R.id.average_vote);
        voteCountTextView = findViewById(R.id.vote_count);
        releaseDateTextView = findViewById(R.id.release_date);
        overviewTextView = findViewById(R.id.overview);

        requestAndDisplayShow();

        //Only visible if no show is found
        emptyStateTextView = findViewById(R.id.empty_view);
    }


    private void displayShow() {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        String imageUrl = show.getImageUrl();
        String title = show.getTitle();
        String averageVote = String.valueOf(show.getVote());
        int voteCount = show.getVoteCount();
        String releaseDate = "Air date: " + show.getReleaseDate();
        String overview = show.getOverview();

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

        setUpAddButton();

        // TODO - Check logic - idk if I got it right
        // If there is no network connection, or data cached - Display no internet connection message
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if ((networkInfo == null || !networkInfo.isConnected()) && show == null) {
            loadingIndicator.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        } else if (title != null && !title.isEmpty()) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            // Set empty state text to display "No movies found." It's not visible if any show is added to the adapter
            emptyStateTextView.setText(R.string.no_movie_data);
        }
    }

    private void requestAndDisplayShow() {
        String tmdbUrl = "https://api.themoviedb.org/3/tv/" + tmdbId;
        Uri baseUri = Uri.parse(tmdbUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", API_KEY);

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
                        show = QueryUtils.extractShowDataFromJson(response);
                        displayShow();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("ShadowDebug", anError.getErrorBody());
                    }
                });
    }

    // Set the toggle button state (Add to collection button)
    private void setUpAddButton() {
        if (showsBox.get(show.getTmdbId()) != null) {
            inUserCollection = true;
        }

        final ToggleButton addRemoveMovieButton = findViewById(R.id.add_remove_show);
        addRemoveMovieButton.setChecked(inUserCollection);
        addRemoveMovieButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    insertShow(show);
                    inUserCollection = true;
                } else {
                    removeShow(show.getTmdbId());
                    inUserCollection = false;
                }
            }
        });
    }

    private void insertShow(Show show) {
        showsBox.put(show);
    }

    private void removeShow(long showTmdbId) {
        showsBox.remove(showTmdbId);
    }

}
