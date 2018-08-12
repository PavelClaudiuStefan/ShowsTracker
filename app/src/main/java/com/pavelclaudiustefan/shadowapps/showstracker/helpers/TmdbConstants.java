package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

public class TmdbConstants {

    private static final String BASE_URL = "https://api.themoviedb.org/";

    public static final String MOVIES_URL = BASE_URL + "3/movie/";
    public static final String TOP_RATED_MOVIES_URL = MOVIES_URL + "top_rated";
    public static final String POPULAR_MOVIES_URL = MOVIES_URL + "popular";

    public static final String TV_SHOWS_URL = BASE_URL + "3/tv/";
    public static final String TOP_RATED_TV_SHOWS_URL = TV_SHOWS_URL + "top_rated";
    public static final String POPULAR_TV_SHOWS_URL = TV_SHOWS_URL + "popular";

    public final static String API_KEY = "e0ff28973a330d2640142476f896da04";

}
