package com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Episode;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<Episode> {

    public static final int ASCENDING = 1;
    public static final int DESCENDING = 2;

    private int sortDirection;

    public EpisodeComparator(int sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(Episode episode1, Episode episode2) {
        if (sortDirection == ASCENDING) {
            return Long.compare(episode1.getReleaseDateInMilliseconds(), episode2.getReleaseDateInMilliseconds());
        } else if (sortDirection == DESCENDING){
            return Long.compare(episode2.getReleaseDateInMilliseconds(), episode1.getReleaseDateInMilliseconds());
        }
        Log.e("ShadowDebug", "EpisodeComparator - Error");
        return 0;
    }

}
