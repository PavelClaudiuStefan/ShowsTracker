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
import com.pavelclaudiustefan.shadowapps.showstracker.models.Episode;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EpisodesCardsAdapter extends RecyclerView.Adapter<EpisodesCardsAdapter.MyViewHolder> {

    private Context context;
    private List<Episode> episodes;
    private EpisodesCardsAdapter.EpisodesAdapterListener listener;
    private int overflowMenuRes;

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView titleView;
        @BindView(R.id.episode_info)
        TextView episodeNumberView;
        @BindView(R.id.tv_show_title)
        TextView tvShowTitleView;
        @BindView(R.id.date)
        TextView releaseDateView;
        @BindView(R.id.episode_image)
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

    public EpisodesCardsAdapter(Context context, List<Episode> episodes, int overflowMenuRes, EpisodesCardsAdapter.EpisodesAdapterListener listener) {
        this.context = context;
        this.episodes = episodes;
        this.overflowMenuRes = overflowMenuRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EpisodesCardsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_list_item, parent, false);
        return new EpisodesCardsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodesCardsAdapter.MyViewHolder holder, int position) {
        Episode episode = episodes.get(position);

        Picasso.get()
                .load(episode.getThumbnailUrl())
                .into(holder.imageView);

        holder.titleView.setText(episode.getTitle());
        holder.episodeNumberView.setText("Season " + episode.getSeasonNumber() + ", Episode " + episode.getEpisodeNumber());
        holder.tvShowTitleView.setText(episode.getSeason().getTarget().getTvShow().getTarget().getTitle());
        holder.releaseDateView.setText(episode.getReleaseDate());

        holder.overflow.setOnClickListener(view -> showPopupMenu(holder.overflow, position));
        holder.cardView.setOnClickListener(view -> listener.onCardSelected(position, holder.cardView));
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    private void showPopupMenu(View view, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(overflowMenuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(new EpisodesCardsAdapter.MyMenuItemClickListener(position));
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
                case R.id.action_watch_unwatch:
                    listener.onWatchUnwatchSelected(position, menuItem);
                    return true;
                default:
            }
            return false;
        }
    }

    public interface EpisodesAdapterListener {
        void onWatchUnwatchSelected(int position, MenuItem menuItem);

        void onCardSelected(int position, CardView cardView);
    }

}
