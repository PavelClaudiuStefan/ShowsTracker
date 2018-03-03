package com.pavelclaudiustefan.shadowapps.showstracker.fetchers;

import android.text.TextUtils;
import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MovieDataFetcher extends DataFetcher {

    @Override
    public List<VideoMainItem> extractItemsFromJson(String itemsJSON) {
        if (TextUtils.isEmpty(itemsJSON)) {
            return null;
        }

        List<VideoMainItem> items = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(itemsJSON);
            int totalPages = baseJsonResponse.getInt("total_pages");
            JSONArray itemsArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject currentItem = itemsArray.getJSONObject(i);

                int tmdbId = currentItem.getInt("id");
                String title = currentItem.getString("title");
                double voteAverage = currentItem.getDouble("vote_average");
                String releaseDate = currentItem.getString("release_date");
                String imageUrl = currentItem.getString("backdrop_path");

                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                long dateInMillseconds = date.getTime();

                Movie movie = new Movie(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                if (i == 0)
                    movie.setTotalPages(totalPages);

                items.add(movie);
            }

        } catch (JSONException e) {
            Log.e("MovieDataFetcher", "Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return items;
    }
}
