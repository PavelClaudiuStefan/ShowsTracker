package com.pavelclaudiustefan.shadowapps.showstracker.data.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class Season {

    @Id private long id;
    private long tmdbId;
    private ToOne<TvShow> tvShow;
    @Backlink private ToMany<Episode> episodes;
    private int numberOfEpisodes;
    private int seasonNumber;

    private String title;
    private String overview;

    private long releaseDateInMilliseconds;   // if equals Long.MAX_VALUE -> Unknown release date

    private String imagePath;
    private String thumbnailSize = "w342";
    private String imageSize = "w780";

    public Season() {}

    public Season(long tmdbId, int seasonNumber, int numberOfEpisodes, String title, String overview, long releaseDateInMilliseconds, String imagePath) {
        this.tmdbId = tmdbId;
        this.seasonNumber = seasonNumber;
        this.numberOfEpisodes = numberOfEpisodes;
        this.title = title;
        this.overview = overview;
        this.releaseDateInMilliseconds = releaseDateInMilliseconds;
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

    public long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(long tmdbId) {
        this.tmdbId = tmdbId;
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

    public void addEpisodes(List<Episode> episodes) {
        this.episodes.addAll(episodes);
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
                ", imagePath='" + imagePath + '\'' +
                ", thumbnailSize='" + thumbnailSize + '\'' +
                ", imageSize='" + imageSize + '\'' +
                '}';
    }
}
