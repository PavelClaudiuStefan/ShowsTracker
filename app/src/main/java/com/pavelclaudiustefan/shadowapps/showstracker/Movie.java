package com.pavelclaudiustefan.shadowapps.showstracker;

public class Movie {

    private String title;
    private double vote;
    private String date;
    private String imdbUrl;

    public Movie(String title, double vote, String date, String imdbUrl) {
        this.title = title;
        this.vote = vote;
        this.date = date;
        this.imdbUrl = imdbUrl;
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
}
