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
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowItemListAdapter<T extends Show> extends ArrayAdapter<T> {

    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.average_vote)
    TextView averageVoteView;
    @BindView(R.id.date)
    TextView dateView;
    @BindView(R.id.thumbnail)
    ImageView imageView;

    public ShowItemListAdapter(Context context, ArrayList<T> items) {
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

        ButterKnife.bind(this, listItemView);

        T currentItem = getItem(position);

        assert currentItem != null;
        titleView.setText(currentItem.getTitle());
        averageVoteView.setText(formatVote(currentItem.getVote()));
        dateView.setText(currentItem.getReleaseDate());


        Picasso picasso = Picasso.get();
        picasso.setIndicatorsEnabled(true);
        picasso.load(currentItem.getThumbnailUrl())
                .into(imageView);

        Picasso.get()
                .load(currentItem.getThumbnailUrl())
                .into(imageView);

        return listItemView;
    }

    private String formatVote(double vote) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.0");
        return magnitudeFormat.format(vote);
    }

    @SafeVarargs
    @Override
    public final void addAll(T... items) {
        super.addAll(items);
    }
}
