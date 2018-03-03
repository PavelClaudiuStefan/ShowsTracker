package com.pavelclaudiustefan.shadowapps.showstracker.fetchers;

import android.text.TextUtils;
import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.helpers.Show;
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


public class ShowDataFetcher extends DataFetcher {
    
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
                String title = currentItem.getString("name");
                double voteAverage = currentItem.getDouble("vote_average");
                String releaseDate = currentItem.getString("first_air_date");
                String imageUrl = currentItem.getString("backdrop_path");

                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
                long dateInMillseconds = date.getTime();

                Show show = new Show(tmdbId, title, voteAverage, dateInMillseconds, imageUrl);

                if (i == 0)
                    show.setTotalPages(totalPages);

                items.add(show);
            }

        } catch (JSONException e) {
            Log.e("ShowsDataFetcher", "Problem parsing json results", e);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return items;
    }
}
