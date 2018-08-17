package com.pavelclaudiustefan.shadowapps.showstracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.pavelclaudiustefan.shadowapps.showstracker.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupListAdapter extends ArrayAdapter<String> {

    @BindView(R.id.group_title)
    TextView titleView;
    @BindView(R.id.thumbnail)
    ImageView imageView;

    public GroupListAdapter(Context context, ArrayList<String> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.group_list_item, parent, false);
        }

        ButterKnife.bind(this, listItemView);

        String currentGroup = getItem(position);

        assert currentGroup != null;
        titleView.setText(currentGroup);
        imageView.setImageDrawable(getTextDrawable(currentGroup));

        return listItemView;
    }

    private TextDrawable getTextDrawable(String title) {
        ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        int color = colorGenerator.getColor(title);

        return TextDrawable.builder().buildRoundRect(title.substring(0, 1), color, 10);
    }

    @Override
    public final void addAll(String... items) {
        super.addAll(items);
    }
}
