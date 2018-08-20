package com.pavelclaudiustefan.shadowapps.showstracker.models;

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
    private ToOne<Season> season;
    private int episodeNumber;

    private String title;
    private String overview;

    private long releaseDateInMilliseconds;   // if equals Long.MAX_VALUE -> Unknown release date

    private double vote;
    private int voteCount;

    private String imagePath;
    private String thumbnailSize = "w300";
    private String imageSize = "original";

    public Episode() {}

    public Episode(int episodeNumber, String title, String overview, long releaseDateInMilliseconds, double vote, int voteCount, String imagePath) {
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.overview = overview;
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
        this.vote = vote;
        this.voteCount = voteCount;
        this.imagePath = imagePath;
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
