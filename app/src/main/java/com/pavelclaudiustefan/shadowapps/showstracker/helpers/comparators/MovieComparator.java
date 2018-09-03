package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;

public class MovieComparator extends ShowComparator<Movie> {
    public MovieComparator(int sortBy, int sortDirection) {
        super(sortBy, sortDirection);
    }
}
