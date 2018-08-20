package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Season {

    @Id private long id;
    private ToOne<TvShow> tvShow;
    @Backlink private ToMany<Episode> episodes;
    private int numberOfEpisodes;
    private int seasonNumber;

    private String title;
    private String overview;

    private long releaseDateInMilliseconds;   // if equals Long.MAX_VALUE -> Unknown release date

    private double vote;
    private int voteCount;

    private String imagePath;
    private String thumbnailSize = "w342";
    private String imageSize = "w780";

    public Season() {}

    public Season(int seasonNumber, String title, String overview, long releaseDateInMilliseconds, double vote, int voteCount, String imagePath) {
        this.seasonNumber = seasonNumber;
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

    public ToOne<TvShow> getTvShow() {
        return tvShow;
    }

    public void setTvShow(ToOne<TvShow> tvShow) {
        this.tvShow = tvShow;
    }

    public ToMany<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(ToMany<Episode> episodes) {
        this.episodes = episodes;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(int numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
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

    @Override
    public String toString() {
        return "Season{" +
                "id=" + id +
                ", tvShow=" + tvShow +
                ", episodes=" + episodes +
                ", seasonNumber=" + seasonNumber +
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
