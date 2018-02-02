package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Movie {

    private int tmdbId;
    private String title;
    private double vote;
    private long cinemaReleaseDateInMilliseconds;
    private long digitalReleaseDateInMilliseconds;
    private long physicalReleaseDateInMilliseconds;
    private String imdbUrl;
    private String imageId;
    private String thumbnailUrl;
    private String imageUrl;
    private String voteCount;
    private String overview;
    private boolean watched;

    private String thumbnailSize = "w300";
    private String imageSize = "w780";

    private int totalPages;

    // Constructor for the movies in lists in the Discover section
    public Movie(int tmdbId, String title, double vote, long cinemaReleaseDateInMilliseconds, String imageId) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.cinemaReleaseDateInMilliseconds = cinemaReleaseDateInMilliseconds;
        this.imageId = imageId;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/" + thumbnailSize + imageId;
        this.imageUrl = "http://image.tmdb.org/t/p/" + imageSize + imageId;

        this.watched = false;
    }

    // Constructor for the movies in lists in the Discover section
    public Movie(int tmdbId, String title, double vote, long cinemaReleaseDateInMilliseconds, long digitalReleaseDateInMilliseconds,
                 long physicalReleaseDateInMilliseconds, String imageId) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.cinemaReleaseDateInMilliseconds = cinemaReleaseDateInMilliseconds;
        this.digitalReleaseDateInMilliseconds = digitalReleaseDateInMilliseconds;
        this.physicalReleaseDateInMilliseconds = physicalReleaseDateInMilliseconds;
        this.imageId = imageId;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/" + thumbnailSize + imageId;
        this.imageUrl = "http://image.tmdb.org/t/p/" + imageSize + imageId;

        this.watched = false;
    }

    // Constructor for the movie in movie activities
    public Movie(int tmdbId, String title, double vote, long cinemaReleaseDateInMilliseconds,
                 long digitalReleaseDateInMilliseconds, long physicalReleaseDateInMilliseconds,  String imageId, String imdbUrl, int voteCount, String overview) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.cinemaReleaseDateInMilliseconds = cinemaReleaseDateInMilliseconds;
        this.digitalReleaseDateInMilliseconds = digitalReleaseDateInMilliseconds;
        this.physicalReleaseDateInMilliseconds = physicalReleaseDateInMilliseconds;
        this.imageId = imageId;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/" + thumbnailSize + imageId;
        this.imageUrl = "http://image.tmdb.org/t/p/" + imageSize + imageId;
        this.imdbUrl = imdbUrl;
        this.voteCount = voteCount + " votes";
        this.overview = overview;

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

    public long getCinemaReleaseDateInMilliseconds() {
        return cinemaReleaseDateInMilliseconds;
    }

    public String getCinemaReleaseDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cinemaReleaseDateInMilliseconds);
        return formatter.format(calendar.getTime());
    }

    public long getDigitalReleaseDateInMilliseconds() {
        return digitalReleaseDateInMilliseconds;
    }

    public String getDigitalReleaseDate() {
        if (digitalReleaseDateInMilliseconds == Long.MAX_VALUE)
            return "Unknown";

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(digitalReleaseDateInMilliseconds);
        return formatter.format(calendar.getTime());
    }

    public long getPhysicalReleaseDateInMilliseconds() {
        return physicalReleaseDateInMilliseconds;
    }

    public String getPhysicalReleaseDate() {
        if (physicalReleaseDateInMilliseconds == Long.MAX_VALUE)
            return "Unknown";

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(physicalReleaseDateInMilliseconds);
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

    public String getVoteCount() {
        return voteCount;
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

    public int getWatchedIntValue() {
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
        this.totalPages = totalPages;
    }
}
