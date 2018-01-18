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

    public MovieCursorAdapter(Context context, Cursor c) {
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

        int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
        int averageVoteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
        int dateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
        int thumbnailUrlColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_THUMBNAIL_URL);

        String movieName = cursor.getString(titleColumnIndex);
        String movieAverageVote = cursor.getString(averageVoteColumnIndex);
        String movieReleaseDate = cursor.getString(dateColumnIndex);
        String thumbnailUrl = cursor.getString(thumbnailUrlColumnIndex);

        // Update the TextViews with the attributes for the current movie
        titleTextView.setText(movieName);
        averageVoteTextView.setText(movieAverageVote);
        dateTextView.setText(movieReleaseDate);

        // Load movie image (landscape)
        Picasso.with(context)
                .load(thumbnailUrl)
                .into(thumbnailView);
    }
}
