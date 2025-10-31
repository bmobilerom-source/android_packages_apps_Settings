package com.epic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

class SystemGridAdapter extends RecyclerView.Adapter<SystemGridAdapter.CardVH> {
    static class CardItem {
        final int iconResId;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        CardItem(int iconResId, int titleResId, int summaryResId, String destFragment) {
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.destFragment = destFragment;
        }
    }

    static class CardVH extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
        }
    }

    private final Context context;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SystemGridAdapter(Context context, List<CardItem> items, int sourceMetrics) {
        this.context = context;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == 0) 
            ? R.layout.system_grid_card_left 
            : R.layout.system_grid_card_right;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        CardItem item = items.get(position);
        holder.iconView.setImageResource(item.iconResId);
        holder.titleView.setText(item.titleResId);
        holder.summaryView.setText(item.summaryResId);
        holder.itemView.setOnClickListener(v -> {
            new SubSettingLauncher(context)
                    .setDestination(item.destFragment)
                    .setTitleRes(item.titleResId)
                    .setArguments(new Bundle())
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    @Override
    public int getItemCount() { return items.size(); }
}

