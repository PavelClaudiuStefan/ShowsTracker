package com.pavelclaudiustefan.shadowapps.showstracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MovieAdapter extends ArrayAdapter<Movie> {

    MovieAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movies_list_item, parent, false);
        }

        Movie currentMovie = getItem(position);

        TextView titleView = listItemView.findViewById(R.id.movie_title);
        titleView.setText(currentMovie.getTitle());

        TextView averageVoteView = listItemView.findViewById(R.id.average_vote);
        averageVoteView.setText(formatVote(currentMovie.getVote()));

        TextView dateView = listItemView.findViewById(R.id.date);
        dateView.setText(currentMovie.getDate());

        ImageView imageView = listItemView.findViewById(R.id.movie_image);
        Picasso.with(getContext())
                .load(currentMovie.getThumbnailUrl())
                .into(imageView);

        return listItemView;
    }

    private String formatVote(double vote) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.0");
        return magnitudeFormat.format(vote);
    }
}
