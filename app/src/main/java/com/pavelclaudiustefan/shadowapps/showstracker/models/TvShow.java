package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.io.Serializable;

import io.objectbox.annotation.Entity;

@Entity
public class TvShow extends Show implements Serializable{

    // TODO
    private String status;  // For example - Returning Series

    public TvShow() {
        super();
    }

    // Constructor for the movies in lists in the Discover section
    public TvShow(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imageId) {
        super(tmdbId, title, vote, releaseDateInMilliseconds, imageId);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TvShow" + super.toString();
    }
}
