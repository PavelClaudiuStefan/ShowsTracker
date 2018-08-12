package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;

import java.util.Comparator;

public class ShowComparator<T extends Show> implements Comparator<T> {

    public static final int BY_DATE = 1;
    public static final int BY_RATING = 2;
    public static final int ALPHABETICALLY = 3;

    public static final int ASCENDING = 1;
    public static final int DESCENDING = 2;

    private int sortBy;
    private int sortDirection;

    ShowComparator(int sortBy, int sortDirection) {
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(T show1, T show2) {
        if (sortBy == BY_DATE) {
            if (sortDirection == ASCENDING) {
                return Long.compare(show1.getReleaseDateInMilliseconds(), show2.getReleaseDateInMilliseconds());
            } else if (sortDirection == DESCENDING){
                return Long.compare(show2.getReleaseDateInMilliseconds(), show1.getReleaseDateInMilliseconds());
            }
        } else if (sortBy == BY_RATING) {
            if (sortDirection == ASCENDING) {
                return Double.compare(show1.getVote(), show2.getVote());
            } else if (sortDirection == DESCENDING){
                return Double.compare(show2.getVote(), show1.getVote());
            }
        } else if (sortBy == ALPHABETICALLY) {
            if (sortDirection == ASCENDING) {
                return show1.getTitle().compareTo(show2.getTitle());
            } else if (sortDirection == DESCENDING){
                return show1.getTitle().compareTo(show2.getTitle()) * (-1);
            }
        }
        Log.e("ShadowDebug", "MovieComparator - Error");
        return 0;
    }

}
