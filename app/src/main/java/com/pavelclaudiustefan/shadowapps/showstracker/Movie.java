package com.pavelclaudiustefan.shadowapps.showstracker;

public class Movie {

    private int tmdbId;
    private String title;
    private double vote;
    private String date;
    private String imdbUrl;
    private String thumbnailUrl;
    private String imageUrl;
    private int voteCount;
    private String overview;
    private boolean watched;

    private String thumbnailSize = "w300";
    private String imageSize = "original";

    public Movie(int tmdbId, String title, double vote, String date, String imageUrl) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.date = date;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/" + thumbnailSize + imageUrl;
        this.imageUrl = "http://image.tmdb.org/t/p/" + imageSize + imageUrl;
    }

    public Movie(int tmdbId, String title, double vote, String date, String imageUrl, String imdbId) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.vote = vote;
        this.date = date;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/w300" + imageUrl;
        this.imageUrl = "http://image.tmdb.org/t/p/original" + imageUrl;
        this.imdbUrl = "http://www.imdb.com/title/" + imdbId;
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

    public String getDate() {
        return date;
    }

    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
