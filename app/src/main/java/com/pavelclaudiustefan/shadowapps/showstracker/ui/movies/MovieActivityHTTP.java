package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.net.Uri;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;

public class MovieActivityHTTP extends MovieActivity {

    public static final String TAG = "MovieActivityHTTP";

    @Override
    void requestAndDisplayMovie(long tmdbId) {
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
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) ->
                        Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache))
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Movie movie = QueryUtils.extractMovieDataFromJson(response);
                        if (movie != null) {
                            Movie dbMovie = getMovieFromDb(movie.getTmdbId());
                            if (dbMovie != null) {
                                setInUserCollection(true);
                                movie.setWatched(dbMovie.isWatched());
                            } else {
                                setInUserCollection(false);
                                movie.setWatched(false);
                            }
                            displayMovie(movie);
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
}
