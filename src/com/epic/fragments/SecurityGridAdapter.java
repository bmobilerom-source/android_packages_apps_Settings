package com.epic.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

class SecurityGridAdapter extends RecyclerView.Adapter<SecurityGridAdapter.CardVH> {
    static final int CARD_TYPE_STANDARD = 0;
    static final int CARD_TYPE_MONET_COLOR = 1;
    static final int CARD_TYPE_LOCKSCREEN = 2;
    static final int CARD_TYPE_THEME_PACKS = 3;
    static final int CARD_TYPE_WIDE = 4;
    static final int CARD_TYPE_SMALL = 5;

    static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        final Integer iconResId;
        
        CardItem(int cardType, int titleResId, int summaryResId, String destFragment, Integer iconResId) {
            this.cardType = cardType;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.destFragment = destFragment;
            this.iconResId = iconResId;
        }
    }

    static class CardVH extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        final LinearLayout colorSwatchesLayout;
        final TextView timeDisplay;
        final LinearLayout themePacksButtons;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.colorSwatchesLayout = itemView.findViewById(R.id.color_swatches_layout);
            this.timeDisplay = itemView.findViewById(R.id.time_display);
            this.themePacksButtons = itemView.findViewById(R.id.theme_packs_buttons);
        }
    }

    private final Context context;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SecurityGridAdapter(Context context, List<CardItem> items, int sourceMetrics) {
        this.context = context;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case CARD_TYPE_MONET_COLOR:
                layoutRes = R.layout.security_grid_card_monet_color;
                break;
            case CARD_TYPE_LOCKSCREEN:
                layoutRes = R.layout.security_grid_card_lockscreen;
                break;
            case CARD_TYPE_THEME_PACKS:
                layoutRes = R.layout.security_grid_card_theme_packs;
                break;
            case CARD_TYPE_WIDE:
                layoutRes = R.layout.security_grid_card_wide;
                break;
            case CARD_TYPE_SMALL:
                layoutRes = R.layout.security_grid_card_small;
                break;
            default:
                layoutRes = R.layout.security_grid_card_standard;
                break;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        CardItem item = items.get(position);
        
        // Set title and summary
        if (holder.titleView != null) {
            holder.titleView.setText(item.titleResId);
        }
        if (holder.summaryView != null) {
            holder.summaryView.setText(item.summaryResId);
        }
        
        // Handle icon
        if (holder.iconView != null) {
            if (item.iconResId != null) {
                holder.iconView.setImageResource(item.iconResId);
                holder.iconView.setVisibility(View.VISIBLE);
            } else {
                holder.iconView.setVisibility(View.GONE);
            }
        }
        
        // Handle special card types
        if (item.cardType == CARD_TYPE_MONET_COLOR && holder.colorSwatchesLayout != null) {
            // Color swatches are already in layout
            holder.colorSwatchesLayout.setVisibility(View.VISIBLE);
        }
        
        if (item.cardType == CARD_TYPE_LOCKSCREEN && holder.timeDisplay != null) {
            // Set time display - you can update this dynamically
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            String time = sdf.format(new java.util.Date());
            holder.timeDisplay.setText(time);
            // Also update hour and minute if available
            TextView timeHour = holder.itemView.findViewById(R.id.time_hour);
            TextView timeMinute = holder.itemView.findViewById(R.id.time_minute);
            if (timeHour != null && timeMinute != null) {
                String[] parts = time.split(":");
                if (parts.length == 2) {
                    timeHour.setText(parts[0]);
                    timeMinute.setText(parts[1]);
                }
            }
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (item.destFragment == null || item.destFragment.isEmpty()) {
                return;
            }
            
            // Handle LineageParts activities
            if (item.destFragment.startsWith("org.lineageos.lineageparts.")) {
                try {
                    ComponentName component = new ComponentName("org.lineageos.lineageparts", 
                        item.destFragment);
                    PackageManager pm = context.getPackageManager();
                    pm.getActivityInfo(component, 0);
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("SecurityGridAdapter", "Failed to launch: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            } else {
                // Launch fragment via SubSettingLauncher
                try {
                    new SubSettingLauncher(context)
                        .setDestination(item.destFragment)
                        .setTitleRes(item.titleResId)
                        .setArguments(new Bundle())
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                } catch (Exception e) {
                    Log.e("SecurityGridAdapter", "Failed to launch fragment: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).cardType;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int cardType = items.get(position).cardType;
                    // Wide cards span 2 columns, others span 1
                    if (cardType == CARD_TYPE_WIDE || cardType == CARD_TYPE_MONET_COLOR) {
                        return 2;
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
}

