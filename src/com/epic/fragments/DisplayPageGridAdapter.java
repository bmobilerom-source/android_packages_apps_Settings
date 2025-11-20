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
import com.android.settings.display.SmartAutoRotatePreferenceFragment;

import java.util.List;

class DisplayPageGridAdapter extends RecyclerView.Adapter<DisplayPageGridAdapter.CardVH> {
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

    DisplayPageGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
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
                        Log.w("DisplayPageGridAdapter", "Icon not found: " + item.iconResId, e);
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
            Log.e("DisplayPageGridAdapter", "Error in onBindViewHolder at position " + position, e);
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
            Log.e("DisplayPageGridAdapter", "Error in getItemViewType", e);
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
                            Log.e("DisplayPageGridAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Error in onAttachedToRecyclerView", e);
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
            Log.w("DisplayPageGridAdapter", "Empty destination fragment, using Anatolia as fallback");
            destFragment = "com.epic.Anatolia";
        }
        
        try {
            // Handle special keys
            if ("auto_rotate".equals(destFragment)) {
                launchAutoRotate();
                return;
            }
            
            if ("ambient_display".equals(destFragment)) {
                launchAmbientDisplay();
                return;
            }
            
            if ("livedisplay".equals(destFragment)) {
                launchLiveDisplay();
                return;
            }
            
            // Handle Wallpaper Background Settings
            if (destFragment.contains("WallpaperBackgroundSettings")) {
                launchSettingsFragment(destFragment, R.string.settings_wallpaper_background_title);
                return;
            }
            
            // Handle Wallpaper picker activity (like InfinitySuite)
            if (destFragment.contains("WallpaperSettings") || destFragment.contains("wallpaper")) {
                launchWallpaperPickerActivity();
                return;
            }
            
            // Handle Custom Themes
            if (destFragment.contains("CustomThemeSettings")) {
                launchSettingsFragment(destFragment, R.string.custom_theme_title);
                return;
            }
            
            // Handle Custom Dashboard
            if (destFragment.contains("DashboardStyleSettings")) {
                launchSettingsFragment(destFragment, R.string.dashboard_style_title);
                return;
            }
            
            // Handle LineageParts activities (dynamic pages)
            if (destFragment.startsWith("org.lineageos.lineageparts.")) {
                launchLineagePartsActivity(destFragment);
                return;
            } else if (destFragment.contains("UserBackupSettingsActivity")) {
                launchUserBackupActivity();
            } else {
                // Launch fragment via SubSettingLauncher (Settings app standard)
                // If fragment doesn't exist, it will fall back to Anatolia
                try {
                    launchSettingsFragment(destFragment, titleResId);
                } catch (Exception e) {
                    Log.w("DisplayPageGridAdapter", "Fragment not found: " + destFragment + ", using Anatolia fallback", e);
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
            Log.e("DisplayPageGridAdapter", "Failed to launch: " + destFragment, e);
            // Final fallback to Anatolia
            try {
                launchSettingsFragment("com.epic.Anatolia", R.string.anatolia_settings_title);
            } catch (Exception fallbackException) {
                showErrorToast();
            }
        }
    }
    
    private void launchAutoRotate() {
        try {
            // Use the same pattern as launchSettingsFragment - this is the most reliable method
            Log.d("DisplayPageGridAdapter", "Launching SmartAutoRotatePreferenceFragment");
            new SubSettingLauncher(activity)
                .setDestination("com.android.settings.display.SmartAutoRotatePreferenceFragment")
                .setTitleRes(R.string.accelerometer_title)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
            Log.d("DisplayPageGridAdapter", "Successfully launched SmartAutoRotatePreferenceFragment");
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch SmartAutoRotatePreferenceFragment", e);
            // Fallback: Try DisplaySettings with auto_rotate key
            try {
                Bundle args = new Bundle();
                args.putString(":settings:fragment_args_key", "auto_rotate");
                new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.DisplaySettings")
                    .setTitleRes(R.string.accelerometer_title)
                    .setArguments(args)
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
                Log.d("DisplayPageGridAdapter", "Fallback: Launched DisplaySettings with auto_rotate key");
            } catch (Exception fallbackException) {
                Log.e("DisplayPageGridAdapter", "All auto rotate launch methods failed", fallbackException);
                showErrorToast();
            }
        }
    }
    
    private void launchAmbientDisplay() {
        try {
            // Try LineageOS ambient display intent first
            Intent intent = new Intent("org.lineageos.settings.device.DOZE_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm = activity.getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                activity.startActivity(intent);
                return;
            }
            
            // Fallback to security lockscreen settings with ambient display
            launchSettingsFragment("com.android.settings.security.LockscreenDashboardFragment", 
                R.string.ambient_display_screen_title);
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch ambient display", e);
            showErrorToast();
        }
    }
    
    private void launchLiveDisplay() {
        try {
            // Method 1: Launch LiveDisplaySettings fragment directly through PartsActivity
            // This is the proper way to launch LineageParts fragments
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.putExtra(":settings:show_fragment", 
                    "org.lineageos.lineageparts.livedisplay.LiveDisplaySettings");
                // Use string title instead of resource ID to avoid cross-package resource issues
                intent.putExtra(":settings:show_fragment_title", 
                    activity.getString(R.string.color_mode_title));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                PackageManager pm = activity.getPackageManager();
                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    Log.d("DisplayPageGridAdapter", "Successfully launched LiveDisplay via PartsActivity");
                    return;
                }
            } catch (Exception e) {
                Log.d("DisplayPageGridAdapter", "PartsActivity LiveDisplay launch failed, trying alternative", e);
            }
            
            // Method 2: Try using PartsList action prefix (if available)
            // Action format: org.lineageos.lineageparts.PARTS_ACTION_PREFIX.livedisplay
            try {
                Intent intent = new Intent("org.lineageos.lineageparts.PART.livedisplay");
                intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                PackageManager pm = activity.getPackageManager();
                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    Log.d("DisplayPageGridAdapter", "Successfully launched LiveDisplay via PART action");
                    return;
                }
            } catch (Exception e) {
                Log.d("DisplayPageGridAdapter", "PartsList action launch failed, trying DisplaySettings", e);
            }
            
            // Method 3: Fallback to DisplaySettings (which has LineagePartsPreference for livedisplay)
            // Navigate to DisplaySettings and let LineagePartsPreference handle the launch
            try {
                Bundle args = new Bundle();
                args.putString(":settings:fragment_args_key", "color_mode");
                new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.DisplaySettings")
                    .setTitleRes(R.string.color_mode_title)
                    .setArguments(args)
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
                Log.d("DisplayPageGridAdapter", "Successfully launched DisplaySettings for LiveDisplay");
            } catch (Exception e) {
                Log.e("DisplayPageGridAdapter", "All LiveDisplay launch methods failed", e);
                showErrorToast();
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch live display", e);
            showErrorToast();
        }
    }
    
    private void launchWallpaperPickerActivity() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.wallpaper", 
                "com.android.customization.picker.CustomizationPickerActivity");
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch wallpaper picker", e);
            showErrorToast();
        }
    }
    
    private void launchLineagePartsActivity(String className) {
        try {
            Log.d("DisplayPageGridAdapter", "Launching LineageParts: " + className);
            
            // Check if it's DisplayRotation fragment - handle it specifically
            if (className.contains("DisplayRotation") || className.equals("org.lineageos.lineageparts.hardware.DisplayRotation")) {
                // Method 1: Launch DisplayRotation fragment through PartsActivity with explicit fragment name
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                        "org.lineageos.lineageparts.PartsActivity"));
                    intent.putExtra(":settings:show_fragment", "org.lineageos.lineageparts.hardware.DisplayRotation");
                    intent.putExtra(":settings:show_fragment_title_resid", R.string.display_rotation_title);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    PackageManager pm = activity.getPackageManager();
                    if (pm.resolveActivity(intent, 0) != null) {
                        Log.d("DisplayPageGridAdapter", "Launching DisplayRotation via PartsActivity");
                        activity.startActivity(intent);
                        return;
                    }
                } catch (Exception e) {
                    Log.w("DisplayPageGridAdapter", "PartsActivity launch failed, trying alternative", e);
                }
                
                // Method 2: Try using the hardware category action
                try {
                    Intent intent = new Intent("org.lineageos.lineageparts.PART");
                    intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                        "org.lineageos.lineageparts.PartsActivity"));
                    intent.putExtra(":settings:show_fragment", "org.lineageos.lineageparts.hardware.DisplayRotation");
                    intent.putExtra(":settings:show_fragment_title_resid", R.string.display_rotation_title);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    PackageManager pm = activity.getPackageManager();
                    if (pm.resolveActivity(intent, 0) != null) {
                        Log.d("DisplayPageGridAdapter", "Launching DisplayRotation via PART action");
                        activity.startActivity(intent);
                        return;
                    }
                } catch (Exception e) {
                    Log.w("DisplayPageGridAdapter", "PART action launch failed, trying direct activity", e);
                }
                
                // Method 3: Try as direct activity (if DisplayRotation is an activity)
                try {
                    ComponentName component = new ComponentName("org.lineageos.lineageparts", 
                        "org.lineageos.lineageparts.hardware.DisplayRotation");
                    PackageManager pm = activity.getPackageManager();
                    pm.getActivityInfo(component, 0);
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("DisplayPageGridAdapter", "Launching DisplayRotation as direct activity");
                    activity.startActivity(intent);
                    return;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("DisplayPageGridAdapter", "DisplayRotation is not an activity, must be a fragment", e);
                } catch (Exception e) {
                    Log.w("DisplayPageGridAdapter", "Direct activity launch failed", e);
                }
                
                // Method 4: Fallback to DisplaySettings with auto-rotate highlight
                Log.w("DisplayPageGridAdapter", "All DisplayRotation launch methods failed, falling back to DisplaySettings");
                try {
                    new SubSettingLauncher(activity)
                        .setDestination("com.android.settings.DisplaySettings")
                        .setTitleRes(R.string.display_rotation_title)
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                    return;
                } catch (Exception ex) {
                    Log.e("DisplayPageGridAdapter", "DisplaySettings fallback also failed", ex);
                }
            }
            
            // For other LineageParts fragments, try standard methods
            // Try as activity first
            ComponentName component = new ComponentName("org.lineageos.lineageparts", className);
            PackageManager pm = activity.getPackageManager();
            try {
                pm.getActivityInfo(component, 0);
                Intent intent = new Intent();
                intent.setComponent(component);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return;
            } catch (PackageManager.NameNotFoundException e) {
                // Not an activity, try as fragment
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.putExtra(":settings:show_fragment", className);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    return;
                }
            }
            
            // Fallback to DisplaySettings if display-related
            if (className.contains("Display") || className.contains("display")) {
                try {
                    new SubSettingLauncher(activity)
                        .setDestination("com.android.settings.DisplaySettings")
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                } catch (Exception ex) {
                    showErrorToast();
                }
            } else {
                showErrorToast();
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch LineageParts: " + className, e);
            showErrorToast();
        }
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

