package com.pavelclaudiustefan.shadowapps.showstracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.MovieEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.ShowEntry;

public class VideoItemProvider extends ContentProvider {

    public static final String LOG_TAG = VideoItemProvider.class.getSimpleName();

    private static final int MOVIES = 100;
    private static final int MOVIE_ID = 101;
    private static final int SHOWS = 200;
    private static final int SHOW_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        sUriMatcher.addURI(VideoItemContract.CONTENT_AUTHORITY, VideoItemContract.PATH_MOVIES, MOVIES);

        sUriMatcher.addURI(VideoItemContract.CONTENT_AUTHORITY, VideoItemContract.PATH_MOVIES + "/#", MOVIE_ID);

        sUriMatcher.addURI(VideoItemContract.CONTENT_AUTHORITY, VideoItemContract.PATH_SHOWS, SHOWS);

        sUriMatcher.addURI(VideoItemContract.CONTENT_AUTHORITY, VideoItemContract.PATH_SHOWS + "/#", SHOW_ID);
    }

    /** Database helper object */
    private DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                cursor = database.query(
                        MovieEntry.TABLE_NAME,
                        projection, selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_ID:
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SHOWS:
                cursor = database.query(
                        ShowEntry.TABLE_NAME,
                        projection, selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SHOW_ID:
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(
                        ShowEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return insertMovie(uri, contentValues);
            case SHOWS:
                return insertShow(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a movie into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertMovie(Uri uri, ContentValues values) {
        // Check that the tmdb id is not null
        String tmdbId = values.getAsString(MovieEntry.TMDB_ID);
        if (tmdbId == null) {
            throw new IllegalArgumentException("Movie requires a tmdb id");
        }

        // Check that the name is not null
        String name = values.getAsString(MovieEntry.MOVIE_TITLE);
        if (name == null) {
            throw new IllegalArgumentException("Movie requires a name");
        }

        // Check that the release date is not null
        String releaseDate = values.getAsString(MovieEntry.MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS);
        if (releaseDate == null) {
            throw new IllegalArgumentException("Movie requires a release date");
        }

        // Check that the average vote is not null
        String averageVote = values.getAsString(MovieEntry.MOVIE_AVERAGE_VOTE);
        if (averageVote == null) {
            throw new IllegalArgumentException("Movie requires an average vote");
        }

        // Check that the imdb id is not null
        String imdbId = values.getAsString(MovieEntry.MOVIE_IMDB_URL);
        if (imdbId == null) {
            throw new IllegalArgumentException("Movie requires an imdb id");
        }

        String voteCount = values.getAsString(MovieEntry.MOVIE_VOTE_COUNT);
        if (voteCount == null) {
            throw new IllegalArgumentException("Movie requires a vote count");
        }

        String overview = values.getAsString(MovieEntry.MOVIE_OVERVIEW);
        if (overview == null) {
            throw new IllegalArgumentException("Movie requires an overview");
        }

        // Check that the image id is not null
        String imageId = values.getAsString(MovieEntry.MOVIE_IMAGE_ID);
        if (imageId == null) {
            throw new IllegalArgumentException("Movie requires an image id");
        }

        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new movie with the given values
        long id = database.insert(MovieEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the movie content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a show into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertShow(Uri uri, ContentValues values) {
        // Check that the tmdb id is not null
        String tmdbId = values.getAsString(ShowEntry.TMDB_ID);
        if (tmdbId == null) {
            throw new IllegalArgumentException("TvShow requires a tmdb id");
        }

        // Check that the name is not null
        String name = values.getAsString(ShowEntry.SHOW_TITLE);
        if (name == null) {
            throw new IllegalArgumentException("TvShow requires a name");
        }

        // Check that the release date is not null
        String releaseDate = values.getAsString(ShowEntry.SHOW_RELEASE_DATE_IN_MILLISECONDS);
        if (releaseDate == null) {
            throw new IllegalArgumentException("TvShow requires a release date");
        }

        // Check that the average vote is not null
        String averageVote = values.getAsString(ShowEntry.SHOW_AVERAGE_VOTE);
        if (averageVote == null) {
            throw new IllegalArgumentException("TvShow requires an average vote");
        }

        String voteCount = values.getAsString(ShowEntry.SHOW_VOTE_COUNT);
        if (voteCount == null) {
            throw new IllegalArgumentException("TvShow requires a vote count");
        }

        String overview = values.getAsString(ShowEntry.SHOW_OVERVIEW);
        if (overview == null) {
            throw new IllegalArgumentException("TvShow requires an overview");
        }

        // Check that the image id is not null
        String imageId = values.getAsString(ShowEntry.SHOW_IMAGE_ID);
        if (imageId == null) {
            throw new IllegalArgumentException("TvShow requires an image id");
        }

        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new movie with the given values
        long id = database.insert(ShowEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the movie content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return updateMovie(uri, contentValues, selection, selectionArgs);
            case MOVIE_ID:
                // For the MOVIE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateMovie(uri, contentValues, selection, selectionArgs);
            case SHOWS:
                return updateShow(uri, contentValues, selection, selectionArgs);
            case SHOW_ID:
                // For the MOVIE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateShow(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateMovie(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the tmdb id is not null
        String tmdbId = values.getAsString(MovieEntry.TMDB_ID);
        if (tmdbId == null) {
            throw new IllegalArgumentException("Movie requires a tmdb id");
        }

        // Check that the name is not null
        String name = values.getAsString(MovieEntry.MOVIE_TITLE);
        if (name == null) {
            throw new IllegalArgumentException("Movie requires a name");
        }

        // Check that the release date is not null
        String releaseDate = values.getAsString(MovieEntry.MOVIE_CINEMA_RELEASE_DATE_IN_MILLISECONDS);
        if (releaseDate == null) {
            throw new IllegalArgumentException("Movie requires a release date");
        }

        // Check that the average vote is not null
        String averageVote = values.getAsString(MovieEntry.MOVIE_AVERAGE_VOTE);
        if (averageVote == null) {
            throw new IllegalArgumentException("Movie requires an average vote");
        }

        // Check that the imdb id is not null
        String imdbId = values.getAsString(MovieEntry.MOVIE_IMDB_URL);
        if (imdbId == null) {
            throw new IllegalArgumentException("Movie requires an imdb id");
        }

        String voteCount = values.getAsString(MovieEntry.MOVIE_VOTE_COUNT);
        if (voteCount == null) {
            throw new IllegalArgumentException("Movie requires a vote count");
        }

        String overview = values.getAsString(MovieEntry.MOVIE_OVERVIEW);
        if (overview == null) {
            throw new IllegalArgumentException("Movie requires an overview");
        }

        String watched = values.getAsString(MovieEntry.MOVIE_WATCHED);
        if (watched == null) {
            throw new IllegalArgumentException("Movie requires a watched value");
        }

        // Check that the image id is not null
        String imageId = values.getAsString(MovieEntry.MOVIE_IMAGE_ID);
        if (imageId == null) {
            throw new IllegalArgumentException("Movie requires an image id");
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    private int updateShow(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the tmdb id is not null
        String tmdbId = values.getAsString(ShowEntry.TMDB_ID);
        if (tmdbId == null) {
            throw new IllegalArgumentException("TvShow requires a tmdb id");
        }

        // Check that the name is not null
        String name = values.getAsString(ShowEntry.SHOW_TITLE);
        if (name == null) {
            throw new IllegalArgumentException("TvShow requires a name");
        }

        // Check that the release date is not null
        String releaseDate = values.getAsString(ShowEntry.SHOW_RELEASE_DATE_IN_MILLISECONDS);
        if (releaseDate == null) {
            throw new IllegalArgumentException("TvShow requires a release date");
        }

        // Check that the average vote is not null
        String averageVote = values.getAsString(ShowEntry.SHOW_AVERAGE_VOTE);
        if (averageVote == null) {
            throw new IllegalArgumentException("TvShow requires an average vote");
        }

        String voteCount = values.getAsString(ShowEntry.SHOW_VOTE_COUNT);
        if (voteCount == null) {
            throw new IllegalArgumentException("TvShow requires a vote count");
        }

        String overview = values.getAsString(ShowEntry.SHOW_OVERVIEW);
        if (overview == null) {
            throw new IllegalArgumentException("TvShow requires an overview");
        }

        String watched = values.getAsString(ShowEntry.SHOW_WATCHED);
        if (watched == null) {
            throw new IllegalArgumentException("TvShow requires a watched value");
        }

        // Check that the image id is not null
        String imageId = values.getAsString(ShowEntry.SHOW_IMAGE_ID);
        if (imageId == null) {
            throw new IllegalArgumentException("TvShow requires an image id");
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ShowEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                // Delete a single row given by the ID in the URI
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SHOWS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ShowEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SHOW_ID:
                // Delete a single row given by the ID in the URI
                selection = ShowEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ShowEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case SHOWS:
                return ShowEntry.CONTENT_LIST_TYPE;
            case SHOW_ID:
                return ShowEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
