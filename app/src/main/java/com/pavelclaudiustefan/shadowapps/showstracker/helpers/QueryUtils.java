package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import android.net.Uri;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  Helper methods used to query data from The Movie Database (TMDb)
 */

public final class QueryUtils {

    private final static String LOG_TAG = "QueryUtils";

    public static List<VideoMainItem> fetchMoviesData(String stringUrl) {
        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractMoviesFromJson(jsonResponse);
    }

    public static Movie fetchMovieData(String stringUrl) {
        URL url = QueryUtils.createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = QueryUtils.makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return QueryUtils.extractMovieDataFromJson(jsonResponse);
    }

    public static List<VideoMainItem> fetchShowsData(String stringUrl) {
        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractShowsFromJson(jsonResponse);
    }

    public static Show fetchShowData(String stringUrl) {
        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractShowDataFromJson(jsonResponse);
    }

    public static List<VideoMainItem> fetchRecommendedMoviesData(ArrayList<Integer> tmdbIds) {
        ArrayList<URL> urls = createUrls(tmdbIds);

        ArrayList<String> jsonResponses = null;
        try {
            jsonResponses = makeHttpRequests(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractRecommendedMoviesFromJson(jsonResponses);
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

    private static ArrayList<URL> createUrls(ArrayList<Integer> tmdbIds) {
        ArrayList<URL> urls = new ArrayList<>();
        for (Integer tmdbId:tmdbIds) {
            try {
                String tmdbUrl = "https://api.themoviedb.org/3/movie/" + tmdbId + "/recommendations";
                Uri baseUri = Uri.parse(tmdbUrl);
                Uri.Builder uriBuilder = baseUri.buildUpon();
                uriBuilder.appendQueryParameter("api_key", "e0ff28973a330d2640142476f896da04");

                URL url = new URL(uriBuilder.toString());
                urls.add(url);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error while building url", e);
            }
        }
        return urls;
    }

    private static ArrayList<String> makeHttpRequests(ArrayList<URL> urls) throws IOException {
        ArrayList<String> jsonResponses = new ArrayList<>();

        if (urls.isEmpty()) {
            return jsonResponses;
        }

        for (URL url:urls) {
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
                    jsonResponses.add(readFromStream(inputStream));
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
        }
        return jsonResponses;
    }

    private static List<VideoMainItem> extractMoviesFromJson(String moviesJSON) {
        if (TextUtils.isEmpty(moviesJSON)) {
            return null;
        }

        List<VideoMainItem> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(moviesJSON);
            int totalPages = baseJsonResponse.getInt("total_pages");
            JSONArray moviesArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject currentMovie = moviesArray.getJSONObject(i);

                int tmdbId = currentMovie.getInt("id");
                String title = currentMovie.getString("title");
                double voteAverage = currentMovie.getDouble("vote_average");
                String releaseDate = currentMovie.getString("release_date");
                String imageUrl = currentMovie.getString("backdrop_path");

                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                long dateInMillseconds = date.getTime();

                Movie movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                if (i == 0)
                    movie.setTotalPages(totalPages);

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

            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
            long cinemaDateInMillseconds = date.getTime();

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
            movie = new Movie(tmdbId, title, voteAverage, cinemaDateInMillseconds, imageUrl);
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

    private static List<VideoMainItem> extractShowsFromJson(String showsJSON) {
        if (TextUtils.isEmpty(showsJSON)) {
            return null;
        }

        List<VideoMainItem> shows = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(showsJSON);
            int totalPages = baseJsonResponse.getInt("total_pages");
            JSONArray showsArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < showsArray.length(); i++) {
                JSONObject currentShow = showsArray.getJSONObject(i);

                int tmdbId = currentShow.getInt("id");
                String title = currentShow.getString("name");
                double voteAverage = currentShow.getDouble("vote_average");
                String releaseDate = currentShow.getString("first_air_date");
                String imageUrl = currentShow.getString("backdrop_path");

                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                long dateInMillseconds = date.getTime();

                Show show = new Show(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                if (i == 0)
                    show.setTotalPages(totalPages);

                shows.add(show);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "extractShowsFromJson - Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return shows;
    }

    private static Show extractShowDataFromJson(String showJSON) {
        if (TextUtils.isEmpty(showJSON)) {
            return null;
        }

        Show show = null;

        try {
            JSONObject showJsonData = new JSONObject(showJSON);

            int tmdbId = showJsonData.getInt("id");
            String title = showJsonData.getString("name");
            double voteAverage = showJsonData.getDouble("vote_average");
            String releaseDate = showJsonData.getString("first_air_date");
            String imageUrl = showJsonData.getString("backdrop_path");
            int voteCount = showJsonData.getInt("vote_count");
            String overview = showJsonData.getString("overview");

            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
            long releaseDateInMillseconds = date.getTime();

            show = new Show(tmdbId, title, voteAverage, releaseDateInMillseconds, imageUrl);
            show.setVoteCount(voteCount);
            show.setOverview(overview);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "extractShowDataFromJson - Problem parsing json results", e);
        } catch (ParseException e) {
            Log.e("LOG_TAG", "extractShowDataFromJson - stuff happened");
        }

        return show;
    }

    private static List<VideoMainItem> extractRecommendedMoviesFromJson(ArrayList<String> jsonResponses) {
        List<VideoMainItem> movies = new ArrayList<>();

        if (jsonResponses.isEmpty()) {
            return null;
        }

        for (String moviesJSON:jsonResponses) {
            if (TextUtils.isEmpty(moviesJSON)) {
                break;
            }

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

                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                    long dateInMillseconds = date.getTime();

                    Movie movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                    boolean isAlreadyInMovies = false;
                    for (VideoMainItem savedMovie:movies) {
                        if (savedMovie.getTmdbId() == movie.getTmdbId())
                            isAlreadyInMovies = true;
                    }

                    if (!isAlreadyInMovies) {
                        movies.add(movie);
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing json results", e);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Order by rating
        Collections.sort(movies, new Comparator<VideoMainItem>() {
            @Override
            public int compare(VideoMainItem m1, VideoMainItem m2) {
                if (m1.getVote() > m2.getVote())
                    return -1;
                else if (m1.getVote() < m2.getVote())
                    return 1;
                else
                    return 0;
            }
        });
        return movies;
    }
}
