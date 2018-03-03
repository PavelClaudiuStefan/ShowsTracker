package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

public class Show extends VideoMainItem{



    // Constructor for the shows in lists in the Discover section
    public Show(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imageId) {
        super(tmdbId, title, vote, releaseDateInMilliseconds, imageId);
    }

}
