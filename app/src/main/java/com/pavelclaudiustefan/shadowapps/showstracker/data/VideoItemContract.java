package com.pavelclaudiustefan.shadowapps.showstracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class VideoItemContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private VideoItemContract() {}

    public static final String CONTENT_AUTHORITY = "com.pavelclaudiustefan.shadowapps.showstracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_SHOWS = "shows";
    public static final String PATH_SEASONS = "seasons";
    public static final String PATH_EPISODES = "episodes";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String _ID = BaseColumns._ID;
        public static final String TMDB_ID = "tmdb_id";
        public static final String MOVIE_TITLE = "title";
        public static final String MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS = "cinema_release_date";
        public static final String MOVIE_DIGITAL_RELEASE_DATE_IN_MILLISECONDS = "digital_release_date";
        public static final String MOVIE_PHYSICAL_RELEASE_DATE_IN_MILLISECONDS = "physical_release_date";
        public static final String MOVIE_AVERAGE_VOTE = "average_vote";
        public static final String MOVIE_IMDB_URL = "imdb_url";
        public static final String MOVIE_OVERVIEW = "overview";
        public static final String MOVIE_VOTE_COUNT = "votes_count";
        public static final String MOVIE_WATCHED = "watched";
        public static final String MOVIE_IMAGE_ID = "image_id";
    }

    public static final class ShowEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SHOWS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;

        public static final String TABLE_NAME = "shows";

        public static final String _ID = BaseColumns._ID;
        public static final String TMDB_ID = "tmdb_id";
        public static final String SHOW_TITLE = "title";
        public static final String SHOW_RELEASE_DATE_IN_MILLISECONDS = "release_date";
        public static final String SHOW_AVERAGE_VOTE = "average_vote";
        public static final String SHOW_IMDB_URL = "imdb_url";
        public static final String SHOW_OVERVIEW = "overview";
        public static final String SHOW_STATUS = "status";
        public static final String SHOW_VOTE_COUNT = "votes_count";
        public static final String SHOW_WATCHED = "watched";
        public static final String SHOW_IMAGE_ID = "image_id";
    }

    public static final class SeasonEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SEASONS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEASONS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEASONS;

        public static final String TABLE_NAME = "seasons";

        public static final String _ID = BaseColumns._ID;
        public static final String TMDB_ID = "tmdb_id";
        public static final String SEASON_NUMBER="season_number";
        public static final String SEASON_TITLE = "title";
        public static final String SEASON_RELEASE_DATE_IN_MILLISECONDS = "release_date";
        public static final String SEASON_OVERVIEW = "overview";
        public static final String SEASON_WATCHED = "watched";
        public static final String SEASON_IMAGE_ID = "image_id";
    }

    public static final class EpisodeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EPISODES);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EPISODES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EPISODES;

        public static final String TABLE_NAME = "episodes";

        public static final String _ID = BaseColumns._ID;
        public static final String TMDB_ID = "tmdb_id";
        public static final String SEASON_NUMBER = "season_number";
        public static final String EPISODE_NUMBER = "episode_number";
        public static final String EPISODE_TITLE = "title";
        public static final String EPISODE_RELEASE_DATE_IN_MILLISECONDS = "release_date";
        public static final String EPISODE_AVERAGE_VOTE = "average_vote";
        public static final String EPISODE_OVERVIEW = "overview";
        public static final String EPISODE_VOTE_COUNT = "votes_count";
        public static final String EPISODE_WATCHED = "watched";
        public static final String EPISODE_IMAGE_ID = "image_id";
    }
}
