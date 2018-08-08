package com.pavelclaudiustefan.shadowapps.showstracker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.VideoItemContract.ShowEntry;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.squareup.picasso.Picasso;

public class ShowCursorAdapter extends CursorAdapter {

    public ShowCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = view.findViewById(R.id.title);
        TextView averageVoteTextView = view.findViewById(R.id.average_vote);
        TextView dateTextView = view.findViewById(R.id.date);
        ImageView thumbnailView = view.findViewById(R.id.thumbnail);

        int tmdbIdColumnIndex = cursor.getColumnIndex(ShowEntry.TMDB_ID);
        int titleColumnIndex = cursor.getColumnIndex(ShowEntry.SHOW_TITLE);
        int averageVoteColumnIndex = cursor.getColumnIndex(ShowEntry.SHOW_AVERAGE_VOTE);
        int dateColumnIndex = cursor.getColumnIndex(ShowEntry.SHOW_RELEASE_DATE_IN_MILLISECONDS);
        int imageIdColumnIndex = cursor.getColumnIndex(ShowEntry.SHOW_IMAGE_ID);

        int tmdbId = cursor.getInt(tmdbIdColumnIndex);
        String movieName = cursor.getString(titleColumnIndex);
        double movieAverageVote = cursor.getDouble(averageVoteColumnIndex);
        long movieReleaseDateInMilliseconds = cursor.getLong(dateColumnIndex);
        String imageId = cursor.getString(imageIdColumnIndex);

        Show show = new Show(tmdbId, movieName, movieAverageVote, movieReleaseDateInMilliseconds, imageId);

        Log.i("ShadowDebug", show.toString());

        // Update the TextViews with the attributes for the current show
        titleTextView.setText(show.getTitle());
        averageVoteTextView.setText(String.valueOf(show.getVote()));
        dateTextView.setText(show.getReleaseDate());

        // Load show image (landscape)
        Picasso.get()
                .load(show.getThumbnailUrl())
                .into(thumbnailView);
    }
}
