package com.pavelclaudiustefan.shadowapps.showstracker;

public class Movie {

    private String title;
    private double vote;
    private String date;
    private String imdbUrl;
    private String thumbnailUrl;
    private String imageUrl;

    public Movie(String title, double vote, String date, String imdbUrl, String imageUrl) {
        this.title = title;
        this.vote = vote;
        this.date = date;
        this.imdbUrl = imdbUrl;
        this.thumbnailUrl = "http://image.tmdb.org/t/p/w300" + imageUrl;
        this.imageUrl = "http://image.tmdb.org/t/p/original" + imageUrl;
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
