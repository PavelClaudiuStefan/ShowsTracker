package com.pavelclaudiustefan.shadowapps.showstracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.VideoMainItem;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class VideoMainItemListAdapter extends ArrayAdapter<VideoMainItem> {

    public VideoMainItemListAdapter(Context context, ArrayList<VideoMainItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.category_list_item, parent, false);
        }

        VideoMainItem currentItem = getItem(position);

        TextView titleView = listItemView.findViewById(R.id.title);
        assert currentItem != null;
        titleView.setText(currentItem.getTitle());

        TextView averageVoteView = listItemView.findViewById(R.id.average_vote);
        averageVoteView.setText(formatVote(currentItem.getVote()));

        TextView dateView = listItemView.findViewById(R.id.date);
        dateView.setText(currentItem.getReleaseDate());

        ImageView imageView = listItemView.findViewById(R.id.thumbnail);
        Picasso.with(getContext())
                .load(currentItem.getThumbnailUrl())
                .into(imageView);

        return listItemView;
    }

    private String formatVote(double vote) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.0");
        return magnitudeFormat.format(vote);
    }

    @Override
    public void addAll(VideoMainItem... items) {
        super.addAll(items);
    }
}
