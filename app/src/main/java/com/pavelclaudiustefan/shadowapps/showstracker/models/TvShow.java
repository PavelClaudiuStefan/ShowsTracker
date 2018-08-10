package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.io.Serializable;

import io.objectbox.annotation.Entity;

@Entity
public class TvShow extends Show implements Serializable{

    public TvShow() {
        super();
    }

    // Constructor for the movies in lists in the Discover section
    public TvShow(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imageId) {
        super(tmdbId, title, vote, releaseDateInMilliseconds, imageId);
    }

    @Override
    public String toString() {
        return "TvShow" + super.toString();
    }
}
