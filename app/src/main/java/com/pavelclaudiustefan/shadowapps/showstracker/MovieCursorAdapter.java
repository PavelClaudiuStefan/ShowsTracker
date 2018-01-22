package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

public class MovieCursorAdapter extends CursorAdapter {

    MovieCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.movies_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = view.findViewById(R.id.movie_title);
        TextView averageVoteTextView = view.findViewById(R.id.average_vote);
        TextView dateTextView = view.findViewById(R.id.date);
        ImageView thumbnailView = view.findViewById(R.id.movie_image);

        int tmdbIdColumnIndex = cursor.getColumnIndex(MovieEntry.TMDB_ID);
        int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
        int averageVoteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
        int dateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS);
        int thumbnailUrlColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL);

        int tmdbId = cursor.getInt(tmdbIdColumnIndex);
        String movieName = cursor.getString(titleColumnIndex);
        double movieAverageVote = cursor.getDouble(averageVoteColumnIndex);
        long movieReleaseDateInMilliseconds = cursor.getLong(dateColumnIndex);
        String thumbnailUrl = cursor.getString(thumbnailUrlColumnIndex);

        Movie movie = new Movie(tmdbId, movieName, movieAverageVote, movieReleaseDateInMilliseconds, thumbnailUrl);

        // Update the TextViews with the attributes for the current movie
        titleTextView.setText(movie.getTitle());
        averageVoteTextView.setText(String.valueOf(movie.getVote()));
        dateTextView.setText(movie.getDate());

        // Load movie image (landscape)
        Picasso.with(context)
                .load(thumbnailUrl)
                .into(thumbnailView);
    }
}
