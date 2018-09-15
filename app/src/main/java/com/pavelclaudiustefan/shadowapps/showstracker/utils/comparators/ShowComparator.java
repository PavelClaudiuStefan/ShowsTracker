package com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators;

import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;

import java.util.Comparator;

public class ShowComparator<T extends Show> implements Comparator<T> {

    public static final int BY_DATE = 1;
    public static final int BY_RATING = 2;
    public static final int ALPHABETICALLY = 3;
    public static final int BY_NUMBER_OF_TIMES_RECOMMENDED = 4;     // Only available to recommended shows

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

        switch (sortBy) {
            case BY_DATE:
                if (sortDirection == ASCENDING) {
                    return Long.compare(show1.getReleaseDateInMilliseconds(), show2.getReleaseDateInMilliseconds());
                } else if (sortDirection == DESCENDING){
                    return Long.compare(show2.getReleaseDateInMilliseconds(), show1.getReleaseDateInMilliseconds());
                }

            case BY_RATING:
                if (sortDirection == ASCENDING) {
                    return Double.compare(show1.getVote(), show2.getVote());
                } else if (sortDirection == DESCENDING){
                    return Double.compare(show2.getVote(), show1.getVote());
                }
            case ALPHABETICALLY:
                if (sortDirection == ASCENDING) {
                    return show1.getTitle().compareTo(show2.getTitle());
                } else if (sortDirection == DESCENDING){
                    return show1.getTitle().compareTo(show2.getTitle()) * (-1);
                }
            case BY_NUMBER_OF_TIMES_RECOMMENDED:
                if (sortDirection == ASCENDING) {
                    if (show1.getNrOfTimesRecommended() == show2.getNrOfTimesRecommended()) {
                        // Recommended the same number of times -> order by average vote
                        return Double.compare(show1.getVote(), show2.getVote());
                    } else {
                        // Order by number of recommended times
                        return Double.compare(show1.getNrOfTimesRecommended(), show2.getNrOfTimesRecommended());
                    }
                } else if (sortDirection == DESCENDING){
                    if (show2.getNrOfTimesRecommended() == show1.getNrOfTimesRecommended()) {
                        // Recommended the same number of times -> order by average vote
                        return Double.compare(show2.getVote(), show1.getVote());
                    } else {
                        // Order by number of recommended times
                        return Double.compare(show2.getNrOfTimesRecommended(), show1.getNrOfTimesRecommended());
                    }
                }
            default:
                Log.e("ShadowDebug", "ShowComprator - Error");
                return 0;
        }
    }

}
