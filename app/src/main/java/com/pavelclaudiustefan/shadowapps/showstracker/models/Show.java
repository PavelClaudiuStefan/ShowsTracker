package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

/**
 * Show is a hypernym of movie and tv show
 */

@BaseEntity
public class Show {

    @Id(assignable = true)
    private long tmdbId;
    private String title;
    private double vote;
    // if equals Long.MAX_VALUE -> Unknown release date
    private long releaseDateInMilliseconds;         // Cinema release date for movies, first TV appearance for TV shows
    private String imdbUrl;
    private String imagePath;
    private int voteCount;
    private String overview;
    private boolean watched;

    @Transient
    private int nrOfTimesRecommended = 0;

    private String thumbnailSize = "w300";
    private String imageSize = "w780";

    public Show() {}

    // Constructor for the movies in lists in the Discover section
    public Show(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imagePath) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
        this.imagePath = imagePath;

        this.watched = false;
    }

    public long getTmdbId(){
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
        if (releaseDateInMilliseconds == Long.MAX_VALUE) {
            return "Unknown";
        }
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

    public String getImagePath() {
        return imagePath;
    }

    public String getImageUrl() {
        return "http://image.tmdb.org/t/p/" + imageSize + imagePath;
    }

    public String getThumbnailUrl() {
        return "http://image.tmdb.org/t/p/" + thumbnailSize + imagePath;
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

    public void setTmdbId(long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVote(double vote) {
        this.vote = vote;
    }

    public void setReleaseDateInMilliseconds(long releaseDateInMilliseconds) {
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
    }

    public void incrementNrOfTimesRecommended() {
        if (nrOfTimesRecommended == 0) {
            nrOfTimesRecommended++;
        }
        nrOfTimesRecommended++;
    }

    public int getNrOfTimesRecommended() {
        return nrOfTimesRecommended;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getThumbnailSize() {
        return thumbnailSize;
    }

    public String getImageSize() {
        return imageSize;
    }

    // TODO - make this available in settings
    public void setThumbnailSize(String thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    // TODO - make this available in settings
    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String toString() {
        return  "{tmdbId=" + tmdbId +
                ", title='" + title + '\'' +
                ", vote=" + vote +
                ", releaseDateInMilliseconds=" + releaseDateInMilliseconds +
                ", imdbUrl='" + imdbUrl + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", voteCount=" + voteCount +
                ", overview='" + overview + '\'' +
                ", watched=" + watched +
                ", thumbnailSize='" + thumbnailSize + '\'' +
                ", imageSize='" + imageSize + '\'' +
                '}';
    }
}
