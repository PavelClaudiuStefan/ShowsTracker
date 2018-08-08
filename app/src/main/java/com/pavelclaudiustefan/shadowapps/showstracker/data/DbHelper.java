package com.pavelclaudiustefan.shadowapps.showstracker.data;

import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.MovieEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.ShowEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.SeasonEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.EpisodeEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "showstracker.db";

    private static final int DATABASE_VERSION = 1;

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the movies table
        String SQL_CREATE_MOVIES_TABLE =  "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieEntry.TMDB_ID + " INTEGER NOT NULL UNIQUE, "
                + MovieEntry.MOVIE_TITLE + " TEXT NOT NULL,"
                + MovieEntry.MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + MovieEntry.MOVIE_DIGITAL_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + MovieEntry.MOVIE_PHYSICAL_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + MovieEntry.MOVIE_AVERAGE_VOTE + " TEXT NOT NULL, "
                + MovieEntry.MOVIE_IMDB_URL + " TEXT NOT NULL,"
                + MovieEntry.MOVIE_VOTE_COUNT + " INTEGER NOT NULL,"
                + MovieEntry.MOVIE_OVERVIEW + " TEXT NOT NULL,"
                + MovieEntry.MOVIE_WATCHED + " INTEGER NOT NULL,"
                + MovieEntry.MOVIE_IMAGE_ID + " TEXT NOT NULL);";

        // Create a String that contains the SQL statement to create the shows table
        String SQL_CREATE_SHOWS_TABLE =  "CREATE TABLE " + ShowEntry.TABLE_NAME + " ("
                + ShowEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShowEntry.TMDB_ID + " INTEGER NOT NULL UNIQUE, "
                + ShowEntry.SHOW_TITLE + " TEXT NOT NULL, "
                + ShowEntry.SHOW_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + ShowEntry.SHOW_AVERAGE_VOTE + " TEXT NOT NULL, "
                + ShowEntry.SHOW_VOTE_COUNT + " INTEGER NOT NULL,"
                + ShowEntry.SHOW_OVERVIEW + " TEXT NOT NULL,"
                + ShowEntry.SHOW_STATUS + " TEXT NOT NULL,"
                + ShowEntry.SHOW_WATCHED + " INTEGER NOT NULL,"
                + ShowEntry.SHOW_IMAGE_ID + " TEXT NOT NULL);";

        // Create a String that contains the SQL statement to create the seasons table
        String SQL_CREATE_SEASONS_TABLE =  "CREATE TABLE " + SeasonEntry.TABLE_NAME + " ("
                + SeasonEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SeasonEntry.TMDB_ID + " INTEGER NOT NULL UNIQUE, "
                + SeasonEntry.SEASON_TITLE + " TEXT NOT NULL, "
                + SeasonEntry.SEASON_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + SeasonEntry.SEASON_OVERVIEW + " TEXT NOT NULL,"
                + SeasonEntry.SEASON_WATCHED + " INTEGER NOT NULL,"
                + SeasonEntry.SEASON_IMAGE_ID + " TEXT NOT NULL);";

        // Create a String that contains the SQL statement to create the episodes table
        String SQL_CREATE_EPISODES_TABLE =  "CREATE TABLE " + EpisodeEntry.TABLE_NAME + " ("
                + EpisodeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EpisodeEntry.TMDB_ID + " INTEGER NOT NULL UNIQUE, "
                + EpisodeEntry.EPISODE_TITLE + " TEXT NOT NULL, "
                + EpisodeEntry.EPISODE_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + EpisodeEntry.EPISODE_AVERAGE_VOTE + " TEXT NOT NULL, "
                + EpisodeEntry.EPISODE_VOTE_COUNT + " INTEGER NOT NULL,"
                + EpisodeEntry.EPISODE_OVERVIEW + " TEXT NOT NULL,"
                + EpisodeEntry.EPISODE_WATCHED + " INTEGER NOT NULL,"
                + EpisodeEntry.EPISODE_IMAGE_ID + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_SHOWS_TABLE);
        db.execSQL(SQL_CREATE_SEASONS_TABLE);
        db.execSQL(SQL_CREATE_EPISODES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
    }
}
