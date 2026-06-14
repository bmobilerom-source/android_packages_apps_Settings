package com.bmobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

public class BMobileExpressiveSettingsAdapter extends RecyclerView.Adapter<BMobileExpressiveSettingsAdapter.CardVH> {
    public static final int CARD_TYPE_STANDARD = 0;
    public static final int CARD_TYPE_WIDE = 1;
    public static final int CARD_TYPE_SMALL = 2;

    // Gradient drawable arrays for light and dark mode
    private static final int[] LIGHT_GRADIENTS = {
        R.drawable.pastel_gradient_amber_light,
        R.drawable.pastel_gradient_blue_light,
        R.drawable.pastel_gradient_green_light,
        R.drawable.pastel_gradient_purple_light,
        R.drawable.pastel_gradient_pink_light,
        R.drawable.pastel_gradient_teal_light,
        R.drawable.pastel_gradient_orange_light,
        R.drawable.pastel_gradient_indigo_light,
        R.drawable.pastel_gradient_cyan_light,
        R.drawable.pastel_gradient_lime_light,
        R.drawable.pastel_gradient_amber_light, // Cycle back
        R.drawable.pastel_gradient_blue_light
    };
    
    private static final int[] DARK_GRADIENTS = {
        R.drawable.pastel_gradient_amber_dark,
        R.drawable.pastel_gradient_blue_dark,
        R.drawable.pastel_gradient_green_dark,
        R.drawable.pastel_gradient_purple_dark,
        R.drawable.pastel_gradient_pink_dark,
        R.drawable.pastel_gradient_teal_dark,
        R.drawable.pastel_gradient_orange_dark,
        R.drawable.pastel_gradient_indigo_dark,
        R.drawable.pastel_gradient_cyan_dark,
        R.drawable.pastel_gradient_lime_dark,
        R.drawable.pastel_gradient_amber_dark, // Cycle back
        R.drawable.pastel_gradient_blue_dark
    };

    public static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        public final String destFragment;
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
        final View cardView;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView;
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;
    private final boolean isDarkMode;

    public BMobileExpressiveSettingsAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
        
        // Check if dark mode is enabled
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & 
                Configuration.UI_MODE_NIGHT_MASK;
        this.isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case CARD_TYPE_WIDE:
                layoutRes = R.layout.fun_display_grid_card_wide;
                break;
            case CARD_TYPE_SMALL:
                layoutRes = R.layout.fun_display_grid_card_small;
                break;
            default:
                layoutRes = R.layout.fun_display_grid_card_standard;
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
            
            // Check theme dynamically on each bind to handle theme changes
            int nightModeFlags = activity.getResources().getConfiguration().uiMode & 
                    Configuration.UI_MODE_NIGHT_MASK;
            boolean currentIsDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
            
            // Apply gradient background based on position and current theme
            if (holder.cardView != null) {
                int gradientIndex = position % (currentIsDarkMode ? DARK_GRADIENTS.length : LIGHT_GRADIENTS.length);
                int gradientRes = currentIsDarkMode ? DARK_GRADIENTS[gradientIndex] : LIGHT_GRADIENTS[gradientIndex];
                
                try {
                    holder.cardView.setBackgroundResource(gradientRes);
                } catch (Exception e) {
                    Log.w("BMobileExpressiveSettingsAdapter", "Failed to set gradient background", e);
                }
            }
            
            // Set title and summary with high contrast colors for visibility
            if (holder.titleView != null) {
                holder.titleView.setText(item.titleResId);
                // Use black text on light gradients, white on dark
                holder.titleView.setTextColor(currentIsDarkMode ? 
                    activity.getResources().getColor(android.R.color.white, null) :
                    activity.getResources().getColor(android.R.color.black, null));
            }
            if (holder.summaryView != null) {
                holder.summaryView.setText(item.summaryResId);
                // Use darker gray on light, lighter gray on dark for better contrast
                holder.summaryView.setTextColor(currentIsDarkMode ?
                    activity.getResources().getColor(android.R.color.white, null) :
                    activity.getResources().getColor(android.R.color.black, null));
                holder.summaryView.setAlpha(0.8f);
            }
            
            // Handle icon with appropriate tint
            if (holder.iconView != null) {
                if (item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                        // Tint icon for visibility
                        holder.iconView.setColorFilter(currentIsDarkMode ?
                            activity.getResources().getColor(android.R.color.white, null) :
                            activity.getResources().getColor(android.R.color.black, null),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w("BMobileExpressiveSettingsAdapter", "Icon not found: " + item.iconResId, e);
                        holder.iconView.setVisibility(View.GONE);
                    }
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
        } catch (Exception e) {
            Log.e("BMobileExpressiveSettingsAdapter", "Error in onBindViewHolder at position " + position, e);
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
            Log.e("BMobileExpressiveSettingsAdapter", "Error in getItemViewType", e);
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
                            Log.e("BMobileExpressiveSettingsAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("BMobileExpressiveSettingsAdapter", "Error in onAttachedToRecyclerView", e);
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
            Log.w("BMobileExpressiveSettingsAdapter", "Empty destination fragment");
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
            Log.e("BMobileExpressiveSettingsAdapter", "Failed to launch: " + destFragment, e);
            showErrorToast();
        }
    }
    
    private void showErrorToast() {
        Toast.makeText(activity, R.string.system_tuner_not_available, 
            Toast.LENGTH_SHORT).show();
    }
}


