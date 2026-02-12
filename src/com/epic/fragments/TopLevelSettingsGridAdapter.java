package com.epic.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.List;

public class TopLevelSettingsGridAdapter extends RecyclerView.Adapter<TopLevelSettingsGridAdapter.CardViewHolder> {

    public static final int CARD_TYPE_LARGE_LEFT = 0;
    public static final int CARD_TYPE_ABOUT_US = 1;
    public static final int CARD_TYPE_STATUS_BAR = 2;
    public static final int CARD_TYPE_CIRCULAR_BUTTON = 3;

    public static class CardItem {
        public int type;
        public int titleResId;
        public int summaryResId;
        public Integer iconResId;
        public String fragmentClass;
        public String titleString;

        public CardItem(int type, int titleResId, int summaryResId, Integer iconResId) {
            this.type = type;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
        }

        public CardItem(int type, int titleResId, int summaryResId, Integer iconResId, String fragmentClass) {
            this.type = type;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.fragmentClass = fragmentClass;
        }

        public CardItem(int type, int titleResId, int summaryResId, Integer iconResId, String fragmentClass, String titleString) {
            this.type = type;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.fragmentClass = fragmentClass;
            this.titleString = titleString;
        }
    }

    private final Context context;
    private final List<CardItem> items;
    private final int metricsCategory;

    public TopLevelSettingsGridAdapter(Context context, List<CardItem> items, int metricsCategory) {
        this.context = context;
        this.items = items;
        this.metricsCategory = metricsCategory;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case CARD_TYPE_LARGE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.top_level_settings_grid_card_large_left, parent, false);
                break;
            case CARD_TYPE_ABOUT_US:
                view = LayoutInflater.from(context).inflate(R.layout.top_level_settings_grid_card_about_us, parent, false);
                break;
            case CARD_TYPE_STATUS_BAR:
                view = LayoutInflater.from(context).inflate(R.layout.top_level_settings_grid_card_status_bar, parent, false);
                break;
            case CARD_TYPE_CIRCULAR_BUTTON:
                view = LayoutInflater.from(context).inflate(R.layout.top_level_settings_grid_card_circular_button, parent, false);
                break;
            default:
                view = LayoutInflater.from(context).inflate(R.layout.top_level_settings_grid_card_circular_button, parent, false);
                break;
        }
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = items.get(position);
        holder.title.setText(item.titleResId);
        holder.summary.setText(item.summaryResId);
        if (holder.icon != null) {
            holder.icon.setImageResource(item.iconResId);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            summary = itemView.findViewById(R.id.card_summary);
            icon = itemView.findViewById(R.id.card_icon);
        }
    }
}