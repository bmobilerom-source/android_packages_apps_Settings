package com.epic.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

public class FunDisplaySettingsAdapter extends RecyclerView.Adapter<FunDisplaySettingsAdapter.CardVH> {
    public static final int CARD_TYPE_STANDARD = 0;
    public static final int CARD_TYPE_WIDE = 1;
    public static final int CARD_TYPE_SMALL = 2;

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
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    public FunDisplaySettingsAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
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
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layoutRes, parent, false);
            return new CardVH(view);
        } catch (RuntimeException e) {
            Log.e("FunDisplaySettingsAdapter", "Failed to inflate card layout " + layoutRes, e);
            View fallback = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.display_page_grid_card_standard, parent, false);
            return new CardVH(fallback);
        }
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
            
            // Handle icon
            if (holder.iconView != null) {
                if (item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w("FunDisplaySettingsAdapter", "Icon not found: " + item.iconResId, e);
                        holder.iconView.setVisibility(View.GONE);
                    }
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
        } catch (Exception e) {
            Log.e("FunDisplaySettingsAdapter", "Error in onBindViewHolder at position " + position, e);
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
            Log.e("FunDisplaySettingsAdapter", "Error in getItemViewType", e);
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
                                    // Wide cards span 2 columns
                                    if (item.cardType == CARD_TYPE_WIDE) {
                                        return 2;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("FunDisplaySettingsAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("FunDisplaySettingsAdapter", "Error in onAttachedToRecyclerView", e);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
    
    /**
     * Launch destination fragment or activity
     */
    private void launchDestination(String destFragment, int titleResId) {
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("FunDisplaySettingsAdapter", "Empty destination fragment, checking for controller-based preferences");
            // Handle controller-based preferences (Safety Center, Support)
            handleControllerBasedPreference(titleResId);
            return;
        }
        
        try {
            // Launch fragment via SubSettingLauncher
            new SubSettingLauncher(activity)
                .setDestination(destFragment)
                .setTitleRes(titleResId)
                .setArguments(new android.os.Bundle())
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
        } catch (Exception e) {
            Log.e("FunDisplaySettingsAdapter", "Failed to launch: " + destFragment, e);
            showErrorToast();
        }
    }
    
    /**
     * Handle preferences that use controllers instead of fragments
     */
    private void handleControllerBasedPreference(int titleResId) {
        try {
            String title = activity.getString(titleResId);
            
            // Safety Center - launch SafetyCenterActivity
            if (title.equals(activity.getString(com.android.settings.R.string.safety_center_title))) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", 
                    "com.android.settings.safetycenter.SafetyCenterActivity");
                activity.startActivity(intent);
                return;
            }
            
            // Support - launch SupportDashboardActivity
            if (title.equals(activity.getString(com.android.settings.R.string.page_tab_title_support))) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", 
                    "com.android.settings.support.SupportDashboardActivity");
                activity.startActivity(intent);
                return;
            }
        } catch (Exception e) {
            Log.e("FunDisplaySettingsAdapter", "Failed to handle controller-based preference", e);
            showErrorToast();
        }
    }
    
    private void showErrorToast() {
        Toast.makeText(activity, R.string.system_tuner_not_available, 
            Toast.LENGTH_SHORT).show();
    }
}

