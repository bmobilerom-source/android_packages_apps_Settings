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
        final android.widget.TextClock lockscreenClock;
        final android.widget.TextClock aodClock;
        final LinearLayout themePacksButtons;
        final View themePackButtonLeft;
        final View themePackButtonRight;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            // Initialize all fields - findViewById returns null if not found, which is safe
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.colorSwatchesLayout = itemView.findViewById(R.id.color_swatches_layout);
            this.lockscreenClock = itemView.findViewById(R.id.lockscreen_clock_preview);
            this.aodClock = itemView.findViewById(R.id.aod_clock);
            this.themePacksButtons = itemView.findViewById(R.id.theme_pack_buttons_layout);
            this.themePackButtonLeft = itemView.findViewById(R.id.theme_pack_button_left);
            this.themePackButtonRight = itemView.findViewById(R.id.theme_pack_button_right);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SecurityGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
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
            
            // Handle icon - show icons for small cards, hide for others
            // Hide icons if not available to prevent crashes
            if (holder.iconView != null) {
                if (item.cardType == CARD_TYPE_SMALL && item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w("SecurityGridAdapter", "Icon not found: " + item.iconResId, e);
                        holder.iconView.setVisibility(View.GONE);
                    }
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }
            
            // Handle special card types
            if (item.cardType == CARD_TYPE_MONET_COLOR && holder.colorSwatchesLayout != null) {
                // Color swatches are already in layout
                holder.colorSwatchesLayout.setVisibility(View.VISIBLE);
            }
            
            // Handle LockScreen card - TextClock handles time automatically
            if (item.cardType == CARD_TYPE_LOCKSCREEN && holder.lockscreenClock != null) {
                // TextClock will automatically update, no manual setting needed
                holder.lockscreenClock.setVisibility(View.VISIBLE);
            }
            
            // Handle Theme Packs buttons - make them independently clickable
            if (item.cardType == CARD_TYPE_THEME_PACKS) {
                if (holder.themePackButtonLeft != null) {
                    holder.themePackButtonLeft.setOnClickListener(v -> {
                        // Launch left button action (e.g., icon pack)
                        launchDestination(item.destFragment, item.titleResId);
                    });
                }
                if (holder.themePackButtonRight != null) {
                    holder.themePackButtonRight.setOnClickListener(v -> {
                        // Launch right button action (e.g., font pack)
                        launchDestination(item.destFragment, item.titleResId);
                    });
                }
                
                // Make the card itself non-clickable since buttons handle clicks
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);
            }
            
            // Handle AOD card - TextClock handles time automatically
            if (item.cardType == CARD_TYPE_WIDE && holder.aodClock != null) {
                // TextClock will automatically update, no manual setting needed
                holder.aodClock.setVisibility(View.VISIBLE);
            }
            
            // Ensure all cards are visible (no placeholder cards)
            holder.itemView.setVisibility(View.VISIBLE);
            
            // Set click listener - following InfinitySuite pattern with proper error handling
            holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
        } catch (Exception e) {
            Log.e("SecurityGridAdapter", "Error in onBindViewHolder at position " + position, e);
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
            Log.e("SecurityGridAdapter", "Error in getItemViewType", e);
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
                            Log.e("SecurityGridAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("SecurityGridAdapter", "Error in onAttachedToRecyclerView", e);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
    
    /**
     * Launch destination fragment or activity - following InfinitySuite pattern
     * Adapted for Settings app using SubSettingLauncher
     */
    private void launchDestination(String destFragment, int titleResId) {
        // If no destination, use Anatolia main page as fallback
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("SecurityGridAdapter", "Empty destination fragment, using Anatolia as fallback");
            destFragment = "com.epic.Anatolia";
        }
        
        try {
            // Handle Wallpaper picker activity (like InfinitySuite)
            if (destFragment.contains("WallpaperSettings") || destFragment.contains("wallpaper")) {
                launchWallpaperPickerActivity();
                return;
            }
            
            // Handle LineageParts activities
            if (destFragment.startsWith("org.lineageos.lineageparts.")) {
                launchLineagePartsActivity(destFragment);
            } else if (destFragment.contains("UserBackupSettingsActivity")) {
                launchUserBackupActivity();
            } else {
                // Launch fragment via SubSettingLauncher (Settings app standard)
                // If fragment doesn't exist, it will fall back to Anatolia
                try {
                    launchSettingsFragment(destFragment, titleResId);
                } catch (Exception e) {
                    Log.w("SecurityGridAdapter", "Fragment not found: " + destFragment + ", using Anatolia fallback", e);
                    // Fallback to Anatolia main page
                    try {
                        launchSettingsFragment("com.epic.Anatolia", R.string.anatolia_settings_title);
                    } catch (Exception ex) {
                        // If even Anatolia fails, show error
                        showErrorToast();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SecurityGridAdapter", "Failed to launch: " + destFragment, e);
            // Final fallback to Anatolia
            try {
                launchSettingsFragment("com.epic.Anatolia", R.string.anatolia_settings_title);
            } catch (Exception fallbackException) {
                showErrorToast();
            }
        }
    }
    
    private void launchWallpaperPickerActivity() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.wallpaper", 
                "com.android.customization.picker.CustomizationPickerActivity");
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e("SecurityGridAdapter", "Failed to launch wallpaper picker", e);
            showErrorToast();
        }
    }
    
    private void launchLineagePartsActivity(String className) throws Exception {
        ComponentName component = new ComponentName("org.lineageos.lineageparts", className);
        PackageManager pm = activity.getPackageManager();
        pm.getActivityInfo(component, 0);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
    
    private void launchUserBackupActivity() throws Exception {
        ComponentName component = new ComponentName("com.android.settings", 
            "com.android.settings.backup.UserBackupSettingsActivity");
        PackageManager pm = activity.getPackageManager();
        pm.getActivityInfo(component, 0);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
    
    private void launchSettingsFragment(String destFragment, int titleResId) {
        new SubSettingLauncher(activity)
            .setDestination(destFragment)
            .setTitleRes(titleResId)
            .setArguments(new Bundle())
            .setSourceMetricsCategory(sourceMetrics)
            .launch();
    }
    
    private void showErrorToast() {
        Toast.makeText(activity, R.string.system_tuner_not_available, 
            Toast.LENGTH_SHORT).show();
    }
}

