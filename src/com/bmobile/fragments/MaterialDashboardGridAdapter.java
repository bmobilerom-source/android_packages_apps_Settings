/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This adapter makes the Material Dashboard Grid feature independent and transferable.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Copy MaterialDashboardGrid.java
 * 3. Copy display_page_grid_card_*.xml layouts
 * 4. Update fragment class names if needed
 * 5. Update drawable references if needed
 */

package com.bmobile.fragments;

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

/**
 * Adapter for Material Dashboard Grid
 * Based on DisplayPageGridAdapter pattern for consistency
 * Makes the feature independent and transferable
 */
public class MaterialDashboardGridAdapter extends RecyclerView.Adapter<MaterialDashboardGridAdapter.CardVH> {
    public static final int CARD_TYPE_STANDARD = 0;
    public static final int CARD_TYPE_MONET_COLOR = 1;
    public static final int CARD_TYPE_LOCKSCREEN = 2;
    public static final int CARD_TYPE_THEME_PACKS = 3;
    public static final int CARD_TYPE_WIDE = 4;
    public static final int CARD_TYPE_SMALL = 5;

    public static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        final Integer iconResId;
        
        public CardItem(int cardType, int titleResId, int summaryResId, String destFragment, Integer iconResId) {
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
        final android.widget.TextClock lockscreenClock;
        final android.widget.TextClock aodClock;
        final LinearLayout themePacksButtons;
        final View themePackButtonLeft;
        final View themePackButtonRight;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            // Initialize all fields - findViewById returns null if not found, which is safe
            // We check for null before using these views
            // Initialize to null first to ensure fields are always initialized
            ImageView icon = null;
            TextView title = null;
            TextView summary = null;
            LinearLayout colorSwatches = null;
            android.widget.TextClock lockscreen = null;
            android.widget.TextClock aod = null;
            LinearLayout themePacks = null;
            View themeLeft = null;
            View themeRight = null;
            
            try {
                icon = itemView.findViewById(android.R.id.icon);
                title = itemView.findViewById(android.R.id.title);
                summary = itemView.findViewById(android.R.id.summary);
                colorSwatches = itemView.findViewById(R.id.color_swatches_layout);
                lockscreen = itemView.findViewById(R.id.lockscreen_clock_preview);
                aod = itemView.findViewById(R.id.aod_clock);
                themePacks = itemView.findViewById(R.id.theme_pack_buttons_layout);
                themeLeft = itemView.findViewById(R.id.theme_pack_button_left);
                themeRight = itemView.findViewById(R.id.theme_pack_button_right);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing CardVH views", e);
                // Views will be null, but we check for null before using them
            }
            
            // Assign to final fields
            this.iconView = icon;
            this.titleView = title;
            this.summaryView = summary;
            this.colorSwatchesLayout = colorSwatches;
            this.lockscreenClock = lockscreen;
            this.aodClock = aod;
            this.themePacksButtons = themePacks;
            this.themePackButtonLeft = themeLeft;
            this.themePackButtonRight = themeRight;
        }
    }

    private static final String TAG = "MaterialDashboardGrid";
    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    public MaterialDashboardGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        try {
            switch (viewType) {
                case CARD_TYPE_MONET_COLOR:
                    layoutRes = R.layout.display_page_grid_card_monet_color;
                    break;
                case CARD_TYPE_LOCKSCREEN:
                    layoutRes = R.layout.display_page_grid_card_lockscreen;
                    break;
                case CARD_TYPE_THEME_PACKS:
                    layoutRes = R.layout.display_page_grid_card_theme_packs;
                    break;
                case CARD_TYPE_WIDE:
                    layoutRes = R.layout.display_page_grid_card_wide;
                    break;
                case CARD_TYPE_SMALL:
                    layoutRes = R.layout.display_page_grid_card_small;
                    break;
                default:
                    layoutRes = R.layout.display_page_grid_card_standard;
                    break;
            }
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layoutRes, parent, false);
            if (view == null) {
                Log.e(TAG, "Failed to inflate layout: " + layoutRes);
                // Fallback to standard layout
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.display_page_grid_card_standard, parent, false);
            }
            return new CardVH(view);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateViewHolder for viewType: " + viewType, e);
            // Fallback to standard layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.display_page_grid_card_standard, parent, false);
            return new CardVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        try {
            if (position < 0 || position >= items.size()) {
                Log.w(TAG, "Invalid position: " + position + ", items size: " + items.size());
                return;
            }
            
            CardItem item = items.get(position);
            if (item == null) {
                Log.w(TAG, "Item is null at position: " + position);
                return;
            }
            
            // Set title and summary with error handling
            if (holder.titleView != null) {
                try {
                    holder.titleView.setText(item.titleResId);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.w(TAG, "Title resource not found: " + item.titleResId, e);
                    holder.titleView.setText("");
                }
            }
            if (holder.summaryView != null) {
                try {
                    holder.summaryView.setText(item.summaryResId);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.w(TAG, "Summary resource not found: " + item.summaryResId, e);
                    holder.summaryView.setText("");
                }
            }
            
            // Handle icon - show icons for small cards
            if (holder.iconView != null) {
                if (item.cardType == CARD_TYPE_SMALL && item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w(TAG, "Icon not found: " + item.iconResId, e);
                        holder.iconView.setVisibility(View.GONE);
                    }
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }
            
            // Handle special card types
            if (item.cardType == CARD_TYPE_MONET_COLOR && holder.colorSwatchesLayout != null) {
                holder.colorSwatchesLayout.setVisibility(View.VISIBLE);
            }
            
            if (item.cardType == CARD_TYPE_LOCKSCREEN && holder.lockscreenClock != null) {
                holder.lockscreenClock.setVisibility(View.VISIBLE);
            }
            
            if (item.cardType == CARD_TYPE_THEME_PACKS) {
                if (holder.themePackButtonLeft != null) {
                    holder.themePackButtonLeft.setOnClickListener(v -> {
                        launchDestination(item.destFragment, item.titleResId);
                    });
                }
                if (holder.themePackButtonRight != null) {
                    holder.themePackButtonRight.setOnClickListener(v -> {
                        launchDestination(item.destFragment, item.titleResId);
                    });
                }
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);
            }
            
            if (item.cardType == CARD_TYPE_WIDE && holder.aodClock != null) {
                holder.aodClock.setVisibility(View.VISIBLE);
            }
            
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> {
                try {
                    launchDestination(item.destFragment, item.titleResId);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching destination", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position, e);
            // Make sure item is still visible even if binding fails
            if (holder != null && holder.itemView != null) {
                holder.itemView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (position >= 0 && position < items.size()) {
                CardItem item = items.get(position);
                if (item != null) {
                    return item.cardType;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getItemViewType", e);
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
                                if (item != null && item.cardType == CARD_TYPE_WIDE) {
                                    return 2;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onAttachedToRecyclerView", e);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
    
    private void launchDestination(String destFragment, int titleResId) {
        if (activity == null) {
            Log.e(TAG, "Activity is null, cannot launch destination");
            return;
        }
        
        if (destFragment == null || destFragment.isEmpty()) {
            // Handle Support (controller-based, needs intent)
            if (titleResId == com.android.settings.R.string.page_tab_title_support) {
                launchSupport();
                return;
            }
            Log.w(TAG, "Empty destination fragment");
            return;
        }
        
        try {
            // Handle Safety Center (controller-based, needs intent)
            if (destFragment.contains("SafetyCenterActivity")) {
                launchSafetyCenter();
                return;
            }
            
            // Launch fragment via SubSettingLauncher
            new SubSettingLauncher(activity)
                .setDestination(destFragment)
                .setTitleRes(titleResId)
                .setArguments(new Bundle())
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found for: " + destFragment, e);
            showErrorToast();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch: " + destFragment, e);
            showErrorToast();
        }
    }
    
    private void launchSafetyCenter() {
        try {
            Intent intent = new Intent("android.settings.SAFETY_CENTER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Safety Center", e);
            showErrorToast();
        }
    }
    
    private void launchSupport() {
        try {
            Intent intent = new Intent("android.settings.SUPPORT_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Support", e);
            showErrorToast();
        }
    }
    
    private void showErrorToast() {
        Toast.makeText(activity, R.string.system_tuner_not_available, 
            Toast.LENGTH_SHORT).show();
    }
}

