package com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators;

import com.pavelclaudiustefan.shadowapps.showstracker.models.TvShow;

public class TvShowComparator extends ShowComparator<TvShow> {
    public TvShowComparator(int sortBy, int sortDirection) {
        super(sortBy, sortDirection);
    }
}
