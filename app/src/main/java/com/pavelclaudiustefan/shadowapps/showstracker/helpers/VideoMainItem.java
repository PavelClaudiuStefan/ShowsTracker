package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A VideoMainItem can be only a movie or a TV show.
 */

public class VideoMainItem {

    private int tmdbId;
    private String title;
    private double vote;
    private long releaseDateInMilliseconds;         // Cinema release date for movies, first TV appearance for TV shows
    private String imdbUrl;
    private String imageId;
    private String thumbnailUrl;
    private String imageUrl;
    private int voteCount;
    private String overview;
    private boolean watched;

    private String thumbnailSize = "w300";
    private String imageSize = "w780";

    //TODO - save the total pages number in a different way
    private static int totalPages;

    // Constructor for the movies in lists in the Discover section
    public VideoMainItem(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imageId) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
        this.imageId = imageId;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/" + thumbnailSize + imageId;
        this.imageUrl = "http://image.tmdb.org/t/p/" + imageSize + imageId;

        this.watched = false;
    }

    public int getTmdbId(){
        return tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public double getVote() {
        return vote;
    }

    public long getReleaseDateInMilliseconds() {
        return releaseDateInMilliseconds;
    }

    public String getReleaseDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(releaseDateInMilliseconds);
        return formatter.format(calendar.getTime());
    }

    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOverview() {
        return overview;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public boolean isWatched() {
        return watched;
    }

    public int getWatchedAsIntValue() {
        if (watched) {
            return 1;
        }
        return 0;
    }

    public void setThumbnailSize(String thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        VideoMainItem.totalPages = totalPages;
    }

}
