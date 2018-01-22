package com.pavelclaudiustefan.shadowapps.showstracker.data;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "showstracker.db";

    private static final int DATABASE_VERSION = 1;

    MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the movies table
        String SQL_CREATE_MOVIES_TABLE =  "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieEntry.TMDB_ID + " INTEGER NOT NULL UNIQUE, "
                + MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + " INTEGER NOT NULL, "
                + MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_MOVIE_IMDB_URL + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_VOTE_COUNT + " INTEGER NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_WATCHED + " INTEGER NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_IMAGE_ID + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_IMAGE_URL + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
    }
}
