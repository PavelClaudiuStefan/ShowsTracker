package com.pavelclaudiustefan.shadowapps.showstracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowsCardsAdapter<T extends Show> extends RecyclerView.Adapter<ShowsCardsAdapter.MyViewHolder> {

    private Context context;
    private List<T> shows;
    private ShowsAdapterListener listener;
    private int overflowMenuRes;

    class MyViewHolder<T> extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView titleView;
        @BindView(R.id.average_vote)
        TextView averageVoteView;
        @BindView(R.id.date)
        TextView releaseDateView;
        @BindView(R.id.image)
        ImageView imageView;
        @BindView(R.id.overflow)
        ImageView overflow;
        @BindView(R.id.card_view)
        CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public ShowsCardsAdapter(Context context, List<T> shows, int overflowMenuRes, ShowsAdapterListener listener) {
        this.context = context;
        this.shows = shows;
        this.overflowMenuRes = overflowMenuRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        T show = shows.get(position);

        Picasso.get()
                .load(show.getThumbnailUrl())
                .into(holder.imageView);

        holder.titleView.setText(show.getTitle());
        holder.averageVoteView.setText(formatVote(show.getVote()));
        holder.releaseDateView.setText(show.getReleaseDate());

        holder.overflow.setOnClickListener(view -> showPopupMenu(holder.overflow, position));
        holder.cardView.setOnClickListener(view -> listener.onCardSelected(position, holder.cardView));
    }


    @Override
    public int getItemCount() {
        return shows.size();
    }

    private void showPopupMenu(View view, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(overflowMenuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    private String formatVote(double vote) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.0");
        return magnitudeFormat.format(vote);
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        int position;

        MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_remove:
                    listener.onAddRemoveSelected(position, menuItem);
                    return true;
                case R.id.action_watch_unwatch:
                    listener.onWatchUnwatchSelected(position, menuItem);
                    return true;
                default:
            }
            return false;
        }
    }

    public interface ShowsAdapterListener {
        void onAddRemoveSelected(int position, MenuItem menuItem);

        void onWatchUnwatchSelected(int position, MenuItem menuItem);

        void onCardSelected(int position, CardView cardView);
    }

}