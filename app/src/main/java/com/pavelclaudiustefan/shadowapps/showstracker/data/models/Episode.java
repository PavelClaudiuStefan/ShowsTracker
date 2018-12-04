package com.pavelclaudiustefan.shadowapps.showstracker.data.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Episode {

    @Id private long id;
    private long tmdbId;
    private ToOne<Season> season;
    private int episodeNumber;
    private int seasonNumber;

    private String title;
    private String overview;

    private long releaseDateInMilliseconds;   // if equals Long.MAX_VALUE -> Unknown release date

    private double vote;
    private int voteCount;

    private String imagePath;
    private String thumbnailSize = "w300";
    private String imageSize = "original";

    private boolean isWatched;

    public Episode() {}

    public Episode(long tmdbId, int episodeNumber, int seasonNumber, String title, String overview, long releaseDateInMilliseconds, double vote, int voteCount, String imagePath) {
        this.tmdbId = tmdbId;
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.overview = overview;
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
        this.vote = vote;
        this.voteCount = voteCount;
        this.imagePath = imagePath;
        isWatched = false;
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

    public String getImageUrl() {
        return "http://image.tmdb.org/t/p/" + imageSize + imagePath;
    }

    public String getThumbnailUrl() {
        return "http://image.tmdb.org/t/p/" + thumbnailSize + imagePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public ToOne<Season> getSeason() {
        return season;
    }

    public void setSeason(ToOne<Season> season) {
        this.season = season;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public long getReleaseDateInMilliseconds() {
        return releaseDateInMilliseconds;
    }

    public void setReleaseDateInMilliseconds(long releaseDateInMilliseconds) {
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
    }

    public double getVote() {
        return vote;
    }

    public void setVote(double vote) {
        this.vote = vote;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getThumbnailSize() {
        return thumbnailSize;
    }

    public void setThumbnailSize(String thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public boolean isWatched() {
        return isWatched;
    }

    public void setWatched(boolean watched) {
        isWatched = watched;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", season=" + season +
                ", episodeNumber=" + episodeNumber +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", releaseDateInMilliseconds=" + releaseDateInMilliseconds +
                ", vote=" + vote +
                ", voteCount=" + voteCount +
                ", imagePath='" + imagePath + '\'' +
                ", thumbnailSize='" + thumbnailSize + '\'' +
                ", imageSize='" + imageSize + '\'' +
                '}';
    }
}
