package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.objectbox.annotation.Entity;

@Entity
public class Movie extends Show implements Serializable{

    // if equals Long.MAX_VALUE -> Unknown release date
    private long digitalReleaseDateInMilliseconds;
    // if equals Long.MAX_VALUE -> Unknown release date
    private long physicalReleaseDateInMilliseconds;

    public Movie() {
        super();
    }

    public Movie(int tmdbId, String title, double vote, long cinemaReleaseDateInMilliseconds, String imageId) {
        super(tmdbId, title, vote, cinemaReleaseDateInMilliseconds, imageId);
    }

    public void setDigitalReleaseDateInMilliseconds(long digitalReleaseDateInMilliseconds) {
        this.digitalReleaseDateInMilliseconds = digitalReleaseDateInMilliseconds;
    }

    public long getDigitalReleaseDateInMilliseconds() {
        return digitalReleaseDateInMilliseconds;
    }

    public String getDigitalReleaseDate() {
        if (digitalReleaseDateInMilliseconds == Long.MAX_VALUE)
            return "Unknown";

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(digitalReleaseDateInMilliseconds);
        return formatter.format(calendar.getTime());
    }

    public void setPhysicalReleaseDateInMilliseconds(long physicalReleaseDateInMilliseconds) {
        this.physicalReleaseDateInMilliseconds = physicalReleaseDateInMilliseconds;
    }

    public long getPhysicalReleaseDateInMilliseconds() {
        return physicalReleaseDateInMilliseconds;
    }

    public String getPhysicalReleaseDate() {
        if (physicalReleaseDateInMilliseconds == Long.MAX_VALUE)
            return "Unknown";

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(physicalReleaseDateInMilliseconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public String toString() {
        return "Movie" + super.toString();
    }
}
