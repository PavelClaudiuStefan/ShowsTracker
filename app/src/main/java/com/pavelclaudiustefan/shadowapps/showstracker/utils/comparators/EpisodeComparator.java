package com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<Episode> {

    public static final int BY_DATE = 1;
    public static final int BY_RATING = 2;
    public static final int ALPHABETICALLY = 3;

    public static final int ASCENDING = 1;
    public static final int DESCENDING = 2;

    private int sortBy;
    private int sortDirection;

    public EpisodeComparator(int sortBy, int sortDirection) {
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Episode episode1, Episode episode2) {
        if (sortBy == BY_DATE) {
            if (sortDirection == ASCENDING) {
                return Long.compare(episode1.getReleaseDateInMilliseconds(), episode2.getReleaseDateInMilliseconds());
            } else if (sortDirection == DESCENDING){
                return Long.compare(episode2.getReleaseDateInMilliseconds(), episode1.getReleaseDateInMilliseconds());
            }
        } else if (sortBy == BY_RATING) {
            if (sortDirection == ASCENDING) {
                return Double.compare(episode1.getVote(), episode2.getVote());
            } else if (sortDirection == DESCENDING){
                return Double.compare(episode2.getVote(), episode1.getVote());
            }
        } else if (sortBy == ALPHABETICALLY) {
            if (sortDirection == ASCENDING) {
                return episode1.getTitle().compareTo(episode2.getTitle());
            } else if (sortDirection == DESCENDING){
                return episode1.getTitle().compareTo(episode2.getTitle()) * (-1);
            }
        }
        Log.e("ShadowDebug", "MovieComparator - Error");
        return 0;
    }

}
