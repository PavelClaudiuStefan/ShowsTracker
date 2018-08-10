package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;

import java.util.Comparator;

public class MovieComparator implements Comparator<Movie> {

    public static final int BY_DATE = 1;
    public static final int BY_RATING = 2;

    public static final int ASCENDING = 1;
    public static final int DESCENDING = 2;

    private int sortBy;
    private int sortDirection;

    public MovieComparator(int sortBy, int sortDirection) {
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Movie m1, Movie m2) {
        if (sortBy == BY_DATE) {
            if (sortDirection == ASCENDING) {
                return Long.compare(m1.getReleaseDateInMilliseconds(), m2.getReleaseDateInMilliseconds());
            } else if (sortDirection == DESCENDING){
                return Long.compare(m2.getReleaseDateInMilliseconds(), m1.getReleaseDateInMilliseconds());
            }
        } else if (sortBy == BY_RATING) {
            if (sortDirection == ASCENDING) {
                return Double.compare(m1.getVote(), m2.getVote());
            } else if (sortDirection == DESCENDING){
                return Double.compare(m2.getVote(), m1.getVote());
            }
        }
        Log.e("ShadowDebug", "MovieComparator - Error");
        return 0;
    }

}
