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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SecurityPrivacyGridAdapter extends RecyclerView.Adapter<SecurityPrivacyGridAdapter.CardVH> {
    static final int CARD_TYPE_STANDARD = 0;
    static final int CARD_TYPE_MONET_COLOR = 1;
    static final int CARD_TYPE_LOCKSCREEN = 2;
    static final int CARD_TYPE_THEME_PACKS = 3;
    static final int CARD_TYPE_WIDE = 4;
    static final int CARD_TYPE_SMALL = 5;
    static final int CARD_TYPE_WALLPAPERS = 6;
    static final int CARD_TYPE_BUTTON = 7;
    static final int CARD_TYPE_TIME_DISPLAY = 8;
    static final int CARD_TYPE_PLACEHOLDER = 9; // Empty placeholder for grid alignment

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
        final TextView timeHour;
        final TextView timeMinute;
        final LinearLayout themePacksButtons;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.colorSwatchesLayout = itemView.findViewById(R.id.color_swatches_layout);
            this.timeDisplay = itemView.findViewById(R.id.time_display);
            this.timeHour = itemView.findViewById(R.id.time_hour);
            this.timeMinute = itemView.findViewById(R.id.time_minute);
            this.themePacksButtons = itemView.findViewById(R.id.theme_pack_buttons_layout);
        }
    }

    private final Context context;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SecurityPrivacyGridAdapter(Context context, List<CardItem> items, int sourceMetrics) {
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
                layoutRes = R.layout.security_privacy_grid_card_monet_color;
                break;
            case CARD_TYPE_LOCKSCREEN:
                layoutRes = R.layout.security_privacy_grid_card_lockscreen;
                break;
            case CARD_TYPE_THEME_PACKS:
                layoutRes = R.layout.security_privacy_grid_card_theme_packs;
                break;
            case CARD_TYPE_WALLPAPERS:
                layoutRes = R.layout.security_privacy_grid_card_wallpapers;
                break;
            case CARD_TYPE_WIDE:
                layoutRes = R.layout.security_privacy_grid_card_wide;
                break;
            case CARD_TYPE_SMALL:
                layoutRes = R.layout.security_privacy_grid_card_small;
                break;
            case 100: // Left button
                layoutRes = R.layout.security_privacy_grid_card_button_left;
                break;
            case 101: // Right button
                layoutRes = R.layout.security_privacy_grid_card_button_right;
                break;
            case CARD_TYPE_TIME_DISPLAY:
                layoutRes = R.layout.security_privacy_grid_card_time_display;
                break;
            case CARD_TYPE_PLACEHOLDER:
                layoutRes = R.layout.security_privacy_grid_card_placeholder;
                break;
            default:
                layoutRes = R.layout.security_privacy_grid_card_standard;
                break;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        try {
            if (position < 0 || position >= items.size()) {
                return;
            }
            
            CardItem item = items.get(position);
            if (item == null) {
                return;
            }
            
            // Set title and summary
            if (holder.titleView != null) {
                holder.titleView.setText(item.titleResId);
            }
            if (holder.summaryView != null) {
                holder.summaryView.setText(item.summaryResId);
            }
            
            // Handle icon - hide for all cards (no icons in image)
            if (holder.iconView != null) {
                holder.iconView.setVisibility(View.GONE);
            }
            
            // Handle special card types
            if (item.cardType == CARD_TYPE_MONET_COLOR && holder.colorSwatchesLayout != null) {
                holder.colorSwatchesLayout.setVisibility(View.VISIBLE);
            }
            
            if (item.cardType == CARD_TYPE_LOCKSCREEN && holder.timeDisplay != null) {
                // Set time display dynamically
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = sdf.format(new Date());
                holder.timeDisplay.setText(time);
                // Also update hour and minute if available
                if (holder.timeHour != null && holder.timeMinute != null) {
                    String[] parts = time.split(":");
                    if (parts.length == 2) {
                        holder.timeHour.setText(parts[0]);
                        holder.timeMinute.setText(parts[1]);
                    }
                }
            }
            
            if (item.cardType == CARD_TYPE_TIME_DISPLAY && holder.timeDisplay != null) {
                // Set time display for time display card
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = sdf.format(new Date());
                holder.timeDisplay.setText(time);
            }
            
            // Skip placeholder cards
            if (item.cardType == CARD_TYPE_PLACEHOLDER) {
                holder.itemView.setVisibility(View.INVISIBLE);
                holder.itemView.setClickable(false);
                return;
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
                        Log.e("SecurityPrivacyGridAdapter", "Failed to launch: " + item.destFragment, e);
                        Toast.makeText(context, R.string.system_tuner_not_available, 
                            Toast.LENGTH_SHORT).show();
                    }
                } else if (item.destFragment.contains("UserBackupSettingsActivity")) {
                    // Launch UserBackupSettingsActivity via Intent
                    try {
                        ComponentName component = new ComponentName("com.android.settings", 
                            "com.android.settings.backup.UserBackupSettingsActivity");
                        PackageManager pm = context.getPackageManager();
                        pm.getActivityInfo(component, 0);
                        Intent intent = new Intent();
                        intent.setComponent(component);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e("SecurityPrivacyGridAdapter", "Failed to launch UserBackupSettingsActivity", e);
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
                        Log.e("SecurityPrivacyGridAdapter", "Failed to launch fragment: " + item.destFragment, e);
                        Toast.makeText(context, R.string.system_tuner_not_available, 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (position >= 0 && position < items.size()) {
                CardItem item = items.get(position);
                if (item != null) {
                    // For buttons, use position to determine left/right
                    if (item.cardType == CARD_TYPE_BUTTON) {
                        return (position % 2 == 0) ? 100 : 101; // 100 = left button, 101 = right button
                    }
                    return item.cardType;
                }
            }
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error in getItemViewType", e);
        }
        return CARD_TYPE_STANDARD;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        try {
                            if (position >= 0 && position < items.size()) {
                                CardItem item = items.get(position);
                                if (item != null) {
                                    int cardType = item.cardType;
                                    // Wide cards span 2 columns
                                    if (cardType == CARD_TYPE_WIDE) {
                                        return 2;
                                    }
                                    // LockScreen card spans 2 rows visually (but still 1 column)
                                    // This is handled by making it tall, not by span size
                                }
                            }
                        } catch (Exception e) {
                            Log.e("SecurityPrivacyGridAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error in onAttachedToRecyclerView", e);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
}
