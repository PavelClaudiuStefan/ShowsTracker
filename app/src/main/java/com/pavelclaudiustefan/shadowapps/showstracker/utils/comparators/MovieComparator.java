package com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators;

import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;

public class MovieComparator extends ShowComparator<Movie> {
    public MovieComparator(int sortBy, int sortDirection) {
        super(sortBy, sortDirection);
    }
}
