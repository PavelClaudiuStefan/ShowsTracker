package com.pavelclaudiustefan.shadowapps.showstracker;

public class Movie {

    private String title;
    private double vote;
    private String date;

    public Movie(String title, double vote, String date) {
        this.title = title;
        this.vote = vote;
        this.date = date;
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
}
