package com.pavelclaudiustefan.shadowapps.showstracker;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *  Helper methods used to query data from The Movie Database (TMDb)
 */

final class QueryUtils {

    private final static String LOG_TAG = "QueryUtils";

    static List<Movie> fetchMoviesData(String stringUrl) {
        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractMoviesFromJson(jsonResponse);
    }

    static Movie fetchMovieData(String stringUrl) {
        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractMovieDataFromJson(jsonResponse);
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error while building url", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Movie> extractMoviesFromJson(String moviesJSON) {
        if (TextUtils.isEmpty(moviesJSON)) {
            return null;
        }

        List<Movie> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(moviesJSON);
            JSONArray moviesArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject currentMovie = moviesArray.getJSONObject(i);

                int tmdbId = currentMovie.getInt("id");
                String title = currentMovie.getString("title");
                double voteAverage = currentMovie.getDouble("vote_average");
                String releaseDate = currentMovie.getString("release_date");
                String imageUrl = currentMovie.getString("backdrop_path");

                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
                long dateInMillseconds = date.getTime();

                Movie movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                // TODO Add imdb url when adding movie into database
                //String imdbUrl = fetchImdbUrl(tmdbId);
                String imdbUrl = "http://www.imdb.com";
                movie.setImdbUrl(imdbUrl);

                movies.add(movie);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return movies;
    }

    private static Movie extractMovieDataFromJson(String movieJSON) {
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        Movie movie = null;

        try {
            JSONObject movieJsonData = new JSONObject(movieJSON);

            int tmdbId = movieJsonData.getInt("id");
            String title = movieJsonData.getString("title");
            double voteAverage = movieJsonData.getDouble("vote_average");
            String releaseDate = movieJsonData.getString("release_date");
            String imageUrl = movieJsonData.getString("backdrop_path");
            String imdbUrl = "http://www.imdb.com/title/" + movieJsonData.getString("imdb_id");
            int voteCount = movieJsonData.getInt("vote_count");
            String overview = movieJsonData.getString("overview");

            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
            long dateInMillseconds = date.getTime();

            movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl, imdbUrl, voteCount, overview);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing json results", e);
        } catch (ParseException e) {
            Log.i("Claudiu", "QuerryUtils stuff happened");
        }

        return movie;
    }
}
