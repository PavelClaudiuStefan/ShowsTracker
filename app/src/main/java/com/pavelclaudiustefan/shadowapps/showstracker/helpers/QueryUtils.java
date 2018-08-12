package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import android.text.TextUtils;
import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  Helper methods used to query data from The Movie Database (TMDb)
 */

public final class QueryUtils {

    private final static String LOG_TAG = "QueryUtils";

    public static List<Movie> extractMoviesFromJson(String moviesJson) {
        if (TextUtils.isEmpty(moviesJson)) {
            return null;
        }

        List<Movie> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(moviesJson);
            JSONArray moviesArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject currentMovie = moviesArray.getJSONObject(i);

                int tmdbId = currentMovie.getInt("id");
                String title = currentMovie.getString("title");
                double voteAverage = currentMovie.getDouble("vote_average");
                String releaseDate = currentMovie.getString("release_date");
                String imageUrl = currentMovie.getString("backdrop_path");

                long dateInMillseconds;
                if (releaseDate != null && !releaseDate.isEmpty()) {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                    dateInMillseconds = date.getTime();
                } else {
                    dateInMillseconds = Long.MAX_VALUE;
                }

                Movie movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);
                movies.add(movie);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return movies;
    }

    public static Movie extractMovieDataFromJson(String moviesJson) {
        if (TextUtils.isEmpty(moviesJson)) {
            return null;
        }

        Movie movie = null;

        try {
            JSONObject movieJsonData = new JSONObject(moviesJson);

            int tmdbId = movieJsonData.getInt("id");
            String title = movieJsonData.getString("title");
            double voteAverage = movieJsonData.getDouble("vote_average");
            String releaseDate = movieJsonData.getString("release_date");
            String imageUrl = movieJsonData.getString("backdrop_path");
            String imdbUrl = "http://www.imdb.com/title/" + movieJsonData.getString("imdb_id");
            int voteCount = movieJsonData.getInt("vote_count");
            String overview = movieJsonData.getString("overview");

            long cinemaReleaseDateInMillseconds;
            if (releaseDate != null && !releaseDate.isEmpty()) {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                cinemaReleaseDateInMillseconds = date.getTime();
            } else {
                cinemaReleaseDateInMillseconds = Long.MAX_VALUE;
            }

            long digitalReleaseDateInMilliseconds = Long.MAX_VALUE;
            long physicalReleaseDateInMilliseconds = Long.MAX_VALUE;

            JSONObject releaseDatesObject = movieJsonData.getJSONObject("release_dates");
            JSONArray releaseDatesArray = releaseDatesObject.getJSONArray("results");
            for (int j = 0; j < releaseDatesArray.length(); j++) {
                JSONObject areaReleaseDates = releaseDatesArray.getJSONObject(j);
                JSONArray typeReleaseDates = areaReleaseDates.getJSONArray("release_dates");
                for (int t = 0; t < typeReleaseDates.length(); t++) {
                    JSONObject typeReleaseDate = typeReleaseDates.getJSONObject(t);
                    int type = typeReleaseDate.getInt("type");

                    if (type == 4) {
                        String digitalDateString =typeReleaseDate.getString("release_date").substring(0, 10);
                        Date digitalDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(digitalDateString);
                        long tempDigitalDateInMillseconds = digitalDate.getTime();
                        if (digitalReleaseDateInMilliseconds > tempDigitalDateInMillseconds)
                            digitalReleaseDateInMilliseconds = tempDigitalDateInMillseconds;
                    }
                    else if (type == 5) {
                        String physicalDateString =typeReleaseDate.getString("release_date").substring(0, 10);
                        Date physicalDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(physicalDateString);
                        long tempPhysicalDateInMillseconds = physicalDate.getTime();
                        if (physicalReleaseDateInMilliseconds > tempPhysicalDateInMillseconds)
                            physicalReleaseDateInMilliseconds = tempPhysicalDateInMillseconds;
                    }
                }
            }
            movie = new Movie(tmdbId, title, voteAverage, cinemaReleaseDateInMillseconds, imageUrl);
            movie.setDigitalReleaseDateInMilliseconds(digitalReleaseDateInMilliseconds);
            movie.setPhysicalReleaseDateInMilliseconds(physicalReleaseDateInMilliseconds);
            movie.setImdbUrl(imdbUrl);
            movie.setVoteCount(voteCount);
            movie.setOverview(overview);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing json results", e);
        } catch (ParseException e) {
            Log.i("Claudiu", "QuerryUtils stuff happened");
        }

        return movie;
    }

    public static List<TvShow> extractTvShowsFromJson(String showsJSON) {
        if (TextUtils.isEmpty(showsJSON)) {
            return null;
        }

        List<TvShow> tvShows = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(showsJSON);
            JSONArray showsArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < showsArray.length(); i++) {
                JSONObject currentShow = showsArray.getJSONObject(i);

                int tmdbId = currentShow.getInt("id");
                String title = currentShow.getString("name");
                double voteAverage = currentShow.getDouble("vote_average");
                String releaseDate = currentShow.getString("first_air_date");
                String imageUrl = currentShow.getString("backdrop_path");

                long dateInMillseconds;
                if (releaseDate != null && !releaseDate.isEmpty()) {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                    dateInMillseconds = date.getTime();
                } else {
                    dateInMillseconds = Long.MAX_VALUE;
                }

                TvShow tvShow = new TvShow(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);
                tvShows.add(tvShow);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "extractTvShowsFromJson - Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tvShows;
    }

    public static TvShow extractTvShowDataFromJson(String showJSON) {
        if (TextUtils.isEmpty(showJSON)) {
            return null;
        }

        TvShow tvShow = null;

        try {
            JSONObject showJsonData = new JSONObject(showJSON);

            int tmdbId = showJsonData.getInt("id");
            String title = showJsonData.getString("name");
            double voteAverage = showJsonData.getDouble("vote_average");
            String releaseDate = showJsonData.getString("first_air_date");
            String imageUrl = showJsonData.getString("backdrop_path");
            int voteCount = showJsonData.getInt("vote_count");
            String overview = showJsonData.getString("overview");

            long releaseDateInMillseconds;
            if (releaseDate != null && !releaseDate.isEmpty()) {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                releaseDateInMillseconds = date.getTime();
            } else {
                releaseDateInMillseconds = Long.MAX_VALUE;
            }

            tvShow = new TvShow(tmdbId, title, voteAverage, releaseDateInMillseconds, imageUrl);
            tvShow.setVoteCount(voteCount);
            tvShow.setOverview(overview);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "extractTvShowDataFromJson - Problem parsing json results", e);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "extractTvShowDataFromJson - stuff happened");
        }

        return tvShow;
    }

    public static int getTotalPagesFromJson(String showsJSON) {
        if (TextUtils.isEmpty(showsJSON)) {
            return 0;
        }

        int numberOfPages = 0;

        try {
            JSONObject baseJsonResponse = new JSONObject(showsJSON);
            numberOfPages = baseJsonResponse.getInt("total_pages");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return numberOfPages;
    }

}
