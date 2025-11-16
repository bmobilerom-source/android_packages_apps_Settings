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
 */

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

public class CompactDashboardGridAdapter extends RecyclerView.Adapter<CompactDashboardGridAdapter.CardVH> {
    public static final int CARD_TYPE_STANDARD = 0;

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
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            // Initialize to null first, then try to find views
            ImageView icon = null;
            TextView title = null;
            TextView summary = null;
            try {
                icon = itemView.findViewById(android.R.id.icon);
                title = itemView.findViewById(android.R.id.title);
                summary = itemView.findViewById(android.R.id.summary);
            } catch (Exception e) {
                Log.e("CompactDashboardGrid", "Error initializing CardVH views", e);
            }
            // Assign to final fields (they will be null if findViewById failed)
            this.iconView = icon;
            this.titleView = title;
            this.summaryView = summary;
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    public CompactDashboardGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            int layoutRes = R.layout.display_page_grid_card_standard;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            if (view == null) {
                Log.e("CompactDashboardGrid", "Failed to inflate card layout");
                // Fallback to a simple FrameLayout
                view = new android.widget.FrameLayout(parent.getContext());
            }
            return new CardVH(view);
        } catch (Exception e) {
            Log.e("CompactDashboardGrid", "Error creating view holder", e);
            // Fallback to a simple FrameLayout
            View fallbackView = new android.widget.FrameLayout(parent.getContext());
            return new CardVH(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        try {
            if (position < 0 || position >= items.size()) {
                Log.w("CompactDashboardGrid", "Invalid position: " + position);
                return;
            }
            
            CardItem item = items.get(position);
            if (item == null) {
                Log.w("CompactDashboardGrid", "Item is null at position: " + position);
                return;
            }
            
            Context context = holder.itemView.getContext();
            if (context == null) {
                Log.w("CompactDashboardGrid", "Context is null");
                return;
            }
            
            // Set title
            if (holder.titleView != null) {
                try {
                    holder.titleView.setText(item.titleResId);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.w("CompactDashboardGrid", "Title resource not found: " + item.titleResId, e);
                } catch (Exception e) {
                    Log.w("CompactDashboardGrid", "Error setting title", e);
                }
            }
            
            // Set summary
            if (holder.summaryView != null) {
                try {
                    holder.summaryView.setText(item.summaryResId);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.w("CompactDashboardGrid", "Summary resource not found: " + item.summaryResId, e);
                } catch (Exception e) {
                    Log.w("CompactDashboardGrid", "Error setting summary", e);
                }
            }
            
            // Handle icon - standard cards don't typically show icons, but handle if present
            if (holder.iconView != null) {
                holder.iconView.setVisibility(View.GONE); // Standard cards don't show icons
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (item.destFragment != null && !item.destFragment.isEmpty()) {
                    try {
                        new SubSettingLauncher(activity)
                                .setDestination(item.destFragment)
                                .setSourceMetricsCategory(sourceMetrics)
                                .setTitleRes(item.titleResId)
                                .launch();
                    } catch (Exception e) {
                        Log.e("CompactDashboardGrid", "Error launching fragment: " + item.destFragment, e);
                        try {
                            Toast.makeText(context, "Error opening settings", Toast.LENGTH_SHORT).show();
                        } catch (Exception toastEx) {
                            // Ignore toast errors
                        }
                    }
                } else {
                    // Handle support or other non-fragment destinations
                    if (item.titleResId == R.string.page_tab_title_support) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://support.google.com/android"));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Log.e("CompactDashboardGrid", "Error opening support", e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("CompactDashboardGrid", "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).cardType;
    }
}


