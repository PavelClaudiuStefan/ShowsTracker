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
import com.pavelclaudiustefan.shadowapps.showstracker.models.Season;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SeasonsCardsAdapter extends RecyclerView.Adapter<SeasonsCardsAdapter.MyViewHolder> {

    private Context context;
    private List<Season> seasons;
    private SeasonsCardsAdapter.SeasonsAdapterListener listener;
    private int overflowMenuRes;

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView titleView;
        @BindView(R.id.season_info)
        TextView seasonInfo;
        @BindView(R.id.season_image)
        ImageView imageView;
        //@BindView(R.id.overflow)
        //ImageView overflow;
        @BindView(R.id.card_view)
        CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public SeasonsCardsAdapter(Context context, List<Season> seasons, int overflowMenuRes, SeasonsAdapterListener listener) {
        this.context = context;
        this.seasons = seasons;
        this.overflowMenuRes = overflowMenuRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeasonsCardsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.season_list_item, parent, false);
        return new SeasonsCardsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonsCardsAdapter.MyViewHolder holder, int position) {
        Season season = seasons.get(position);

        Picasso.get()
                .load(season.getThumbnailUrl())
                .into(holder.imageView);

        holder.titleView.setText(season.getTitle());
        holder.seasonInfo.setText("Season " + season.getSeasonNumber());

        //holder.overflow.setOnClickListener(view -> showPopupMenu(holder.overflow, position));
        holder.cardView.setOnClickListener(view -> listener.onCardSelected(position, holder.cardView));
    }


    @Override
    public int getItemCount() {
        return seasons.size();
    }

    private void showPopupMenu(View view, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(overflowMenuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(new SeasonsCardsAdapter.MyMenuItemClickListener(position));
        popup.show();
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

    public interface SeasonsAdapterListener {
        void onAddRemoveSelected(int position, MenuItem menuItem);

        void onWatchUnwatchSelected(int position, MenuItem menuItem);

        void onCardSelected(int position, CardView cardView);
    }

}
