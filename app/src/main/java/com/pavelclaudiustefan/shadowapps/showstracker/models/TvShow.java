package com.pavelclaudiustefan.shadowapps.showstracker.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.relation.ToMany;

@Entity
public class TvShow extends Show implements Serializable{

    @Backlink
    private ToMany<Season> seasons;

    // TODO
    private String status;  // For example - Returning Series

    public TvShow() {
        super();
    }

    // Constructor for the movies in lists in the Discover section
    public TvShow(int tmdbId, String title, double vote, long releaseDateInMilliseconds, String imageId) {
        super(tmdbId, title, vote, releaseDateInMilliseconds, imageId);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TvShow" + super.toString();
    }

    public void addSeason(Season season) {
        seasons.add(season);
    }

    public void addSeasons(List<Season> seasons) {
        this.seasons.addAll(seasons);
    }

    public ToMany<Season> getSeasons() {
        Collections.sort(seasons, (season1, season2) -> Integer.compare(season1.getSeasonNumber(), season2.getSeasonNumber()));
        return seasons;
    }

    public int getNumberOfSeasons() {
        return seasons.size();
    }
}
