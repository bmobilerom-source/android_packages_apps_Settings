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
    static final int CARD_TYPE_SECURITY_HEADER = 10; // Security info header card

    static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        final Integer iconResId;
        final boolean isToggle;
        final String toggleKey; // Settings.Secure or Settings.System key for toggle state
        
        CardItem(int cardType, int titleResId, int summaryResId, String destFragment, Integer iconResId) {
            this(cardType, titleResId, summaryResId, destFragment, iconResId, false, null);
        }
        
        CardItem(int cardType, int titleResId, int summaryResId, String destFragment, Integer iconResId, boolean isToggle, String toggleKey) {
            this.cardType = cardType;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.destFragment = destFragment;
            this.iconResId = iconResId;
            this.isToggle = isToggle;
            this.toggleKey = toggleKey;
        }
    }

    static class CardVH extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        final LinearLayout colorSwatchesLayout;
        final android.widget.TextClock lockscreenClock;
        final TextView timeDisplay;
        final LinearLayout themePacksButtons;
        final View themePackButtonLeft;
        final View themePackButtonRight;
        final android.widget.TextClock aodClock;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.colorSwatchesLayout = itemView.findViewById(R.id.color_swatches_layout);
            this.lockscreenClock = itemView.findViewById(R.id.lockscreen_clock_preview);
            this.timeDisplay = itemView.findViewById(R.id.time_display);
            this.themePacksButtons = itemView.findViewById(R.id.theme_pack_buttons_layout);
            this.themePackButtonLeft = itemView.findViewById(R.id.theme_pack_button_left);
            this.themePackButtonRight = itemView.findViewById(R.id.theme_pack_button_right);
            this.aodClock = itemView.findViewById(R.id.card_clock);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SecurityPrivacyGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
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
                layoutRes = R.layout.securitygrid_card_monet_color;
                break;
            case CARD_TYPE_LOCKSCREEN:
                layoutRes = R.layout.securitygrid_card_lockscreen;
                break;
            case CARD_TYPE_THEME_PACKS:
                layoutRes = R.layout.securitygrid_card_theme_packs;
                break;
            case CARD_TYPE_WALLPAPERS:
                layoutRes = R.layout.securitygrid_card_wallpapers;
                break;
            case CARD_TYPE_WIDE:
                layoutRes = R.layout.securitygrid_card_wide;
                break;
            case CARD_TYPE_SMALL:
                layoutRes = R.layout.securitygrid_card_small;
                break;
            case 100: // Left button
                layoutRes = R.layout.securitygrid_card_button_left;
                break;
            case 101: // Right button
                layoutRes = R.layout.securitygrid_card_button_right;
                break;
            case CARD_TYPE_TIME_DISPLAY:
                layoutRes = R.layout.securitygrid_card_time_display;
                break;
            case CARD_TYPE_PLACEHOLDER:
                layoutRes = R.layout.securitygrid_card_placeholder;
                break;
            case CARD_TYPE_SECURITY_HEADER:
                layoutRes = R.layout.securitygrid_card_header;
                break;
            default:
                layoutRes = R.layout.securitygrid_card_standard;
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
                // For toggle cards, update summary based on current state
                if (item.isToggle && item.toggleKey != null) {
                    boolean isEnabled = isToggleEnabled(item.toggleKey);
                    String summary = activity.getString(item.summaryResId);
                    String state = isEnabled ? activity.getString(android.R.string.yes) : activity.getString(android.R.string.no);
                    holder.summaryView.setText(summary + " • " + state);
                    holder.summaryView.setVisibility(View.GONE);
                } else if (item.summaryResId != 0) {
                    // Show summary if it exists
                    holder.summaryView.setText(item.summaryResId);
                    holder.summaryView.setVisibility(View.GONE);
                } else {
                    holder.summaryView.setVisibility(View.GONE);
                }
            }
            
            // Handle icon - show icons for small cards, hide for others
            // Hide icons if not available to prevent crashes
            if (holder.iconView != null) {
                if (item.cardType == CARD_TYPE_SMALL && item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w("SecurityPrivacyGridAdapter", "Icon not found: " + item.iconResId, e);
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
            
            // Handle Time Display card
            if (item.cardType == CARD_TYPE_TIME_DISPLAY && holder.timeDisplay != null) {
                // For time display card, we can use TextClock or update manually
                // TextClock is already in the layout, it will update automatically
            }
            
            // Handle Security Header card - non-clickable, just displays security info
            if (item.cardType == CARD_TYPE_SECURITY_HEADER) {
                // Make it non-clickable since it's just informational
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);
                // The xd_about_phone_header layout will be populated by SecurityInfoHeaderController
                // which should be initialized in the fragment
            }
            
            // Ensure all cards are visible (no placeholder cards)
            holder.itemView.setVisibility(View.VISIBLE);
            
            // Handle toggle cards
            if (item.isToggle && item.toggleKey != null) {
                // Make sure card is clickable
                holder.itemView.setClickable(true);
                holder.itemView.setFocusable(true);
                
                // Update card appearance based on toggle state
                boolean isEnabled = isToggleEnabled(item.toggleKey);
                updateToggleCardAppearance(holder.itemView, isEnabled);
                
                // Update summary with current state
                if (holder.summaryView != null) {
                    String summary = activity.getString(item.summaryResId);
                    String state = isEnabled ? activity.getString(android.R.string.yes) : activity.getString(android.R.string.no);
                    holder.summaryView.setText(summary + " • " + state);
                    holder.summaryView.setVisibility(View.GONE);
                }
                
                // Set click listener to toggle state
                holder.itemView.setOnClickListener(v -> {
                    Log.d("SecurityPrivacyGridAdapter", "Toggle card clicked: " + item.toggleKey);
                    toggleStatusbarFeature(item.toggleKey);
                    
                    // Small delay to ensure Settings write completes
                    holder.itemView.postDelayed(() -> {
                        // Refresh the card appearance
                        boolean newState = isToggleEnabled(item.toggleKey);
                        updateToggleCardAppearance(holder.itemView, newState);
                        
                        // Update summary
                        if (holder.summaryView != null) {
                            String summary = activity.getString(item.summaryResId);
                            String state = newState ? activity.getString(android.R.string.yes) : activity.getString(android.R.string.no);
                            holder.summaryView.setText(summary + " • " + state);
                        }
                        
                        // Notify adapter to refresh this item
                        notifyItemChanged(holder.getAdapterPosition());
                    }, 100);
                });
            } else if (item.cardType != CARD_TYPE_SECURITY_HEADER && item.destFragment != null && !item.destFragment.isEmpty()) {
                // Set click listener for regular cards - skip for security header (non-clickable)
                holder.itemView.setClickable(true);
                holder.itemView.setFocusable(true);
                holder.itemView.setOnClickListener(v -> {
                    try {
                        Log.d("SecurityPrivacyGridAdapter", "Card clicked: " + activity.getString(item.titleResId) + ", launching: " + item.destFragment);
                        launchDestination(item.destFragment, item.titleResId);
                    } catch (Exception e) {
                        Log.e("SecurityPrivacyGridAdapter", "Error launching destination: " + item.destFragment, e);
                        Toast.makeText(activity, "Error opening " + activity.getString(item.titleResId), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (item.cardType != CARD_TYPE_SECURITY_HEADER && (item.destFragment == null || item.destFragment.isEmpty())) {
                // Card has no destination - make it non-clickable
                holder.itemView.setClickable(false);
                holder.itemView.setFocusable(false);
                Log.w("SecurityPrivacyGridAdapter", "Card has no destination: " + activity.getString(item.titleResId));
            }
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
                                    // Wide cards and security header span 2 columns
                                    if (cardType == CARD_TYPE_WIDE || cardType == CARD_TYPE_SECURITY_HEADER) {
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
    
    /**
     * Launch destination fragment or activity - following InfinitySuite pattern
     * Adapted for Settings app using SubSettingLauncher
     */
    private void launchDestination(String destFragment, int titleResId) {
        Log.d("SecurityPrivacyGridAdapter", "Launching destination: " + destFragment + " with title: " + titleResId);
        // If no destination, use Anatolia main page as fallback
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("SecurityPrivacyGridAdapter", "Empty destination fragment, using Anatolia as fallback");
            destFragment = "com.android.settings.security.SecuritySettings";
        }
        
        try {
            // Handle special intent markers
            if (destFragment != null && destFragment.startsWith("INTENT:")) {
                String intentAction = destFragment.substring(7); // Remove "INTENT:" prefix
                if (Intent.ACTION_REVIEW_ACCESSIBILITY_SERVICES.equals(intentAction)) {
                    launchAccessibilityUsageIntent();
                    return;
                }
            }
            
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
                    Log.w("SecurityPrivacyGridAdapter", "Fragment not found: " + destFragment + ", using Anatolia fallback", e);
                    // Fallback to Anatolia main page
                    try {
                        launchSettingsFragment("com.android.settings.security.SecuritySettings",
                                R.string.securitygrid_title);
                    } catch (Exception ex) {
                        // If even Anatolia fails, show error
                        showErrorToast();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Failed to launch: " + destFragment, e);
            // Final fallback to Anatolia
            try {
                launchSettingsFragment("com.android.settings.security.SecuritySettings",
                        R.string.securitygrid_title);
            } catch (Exception fallbackException) {
                showErrorToast();
            }
        }
    }
    
    private void launchAccessibilityUsageIntent() {
        try {
            Intent intent = new Intent(Intent.ACTION_REVIEW_ACCESSIBILITY_SERVICES);
            String packageName = activity.getPackageManager().getPermissionControllerPackageName();
            if (packageName != null) {
                intent.setPackage(packageName);
            }
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Failed to launch accessibility usage", e);
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
            Log.e("SecurityPrivacyGridAdapter", "Failed to launch wallpaper picker", e);
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
        Toast.makeText(activity, R.string.securitygrid_unavailable, 
            Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Check if a toggle is enabled based on Settings key
     */
    private boolean isToggleEnabled(String toggleKey) {
        try {
            // Some settings are in Global, others in Secure
            if ("window_ignore_secure".equals(toggleKey) || "no_storage_restrict".equals(toggleKey)) {
                return android.provider.Settings.Global.getInt(activity.getContentResolver(), toggleKey, 0) == 1;
            } else {
                return android.provider.Settings.Secure.getInt(activity.getContentResolver(), toggleKey, 0) == 1;
            }
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error reading toggle state for " + toggleKey, e);
            return false;
        }
    }
    
    /**
     * Toggle a feature on/off
     */
    private void toggleStatusbarFeature(String toggleKey) {
        try {
            boolean currentState = isToggleEnabled(toggleKey);
            int newState = currentState ? 0 : 1;
            
            // Write to correct Settings location (Global for some, Secure for others)
            boolean result;
            if ("window_ignore_secure".equals(toggleKey) || "no_storage_restrict".equals(toggleKey)) {
                result = android.provider.Settings.Global.putInt(activity.getContentResolver(), toggleKey, newState);
                Log.d("SecurityPrivacyGridAdapter", "Toggled " + toggleKey + " to " + (newState == 1 ? "enabled" : "disabled") + " (Global, result: " + result + ")");
            } else {
                result = android.provider.Settings.Secure.putInt(activity.getContentResolver(), toggleKey, newState);
                Log.d("SecurityPrivacyGridAdapter", "Toggled " + toggleKey + " to " + (newState == 1 ? "enabled" : "disabled") + " (Secure, result: " + result + ")");
            }
            
            // Verify the write was successful
            if (!result) {
                Log.e("SecurityPrivacyGridAdapter", "Failed to write toggle state! putInt returned false");
                // Show error toast
                Toast.makeText(activity, "Failed to update setting", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Verify the value was actually written
            int verifyState;
            if ("window_ignore_secure".equals(toggleKey) || "no_storage_restrict".equals(toggleKey)) {
                verifyState = android.provider.Settings.Global.getInt(activity.getContentResolver(), toggleKey, -1);
            } else {
                verifyState = android.provider.Settings.Secure.getInt(activity.getContentResolver(), toggleKey, -1);
            }
            if (verifyState != newState) {
                Log.e("SecurityPrivacyGridAdapter", "Failed to write toggle state! Expected " + newState + " but got " + verifyState);
                // Show error toast
                Toast.makeText(activity, "Failed to update setting", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Broadcast change so SystemUI/framework can respond
            String action = getBroadcastActionForToggle(toggleKey);
            if (action != null) {
                Intent intent = new Intent(action);
                intent.putExtra("enabled", newState == 1);
                intent.putExtra("toggle_key", toggleKey);
                activity.sendBroadcast(intent);
                Log.d("SecurityPrivacyGridAdapter", "Broadcast sent: " + action);
            }
            
            // Show confirmation toast
            String message = toggleKey + " " + (newState == 1 ? "enabled" : "disabled");
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error toggling " + toggleKey, e);
            Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get the broadcast action for a specific toggle key
     */
    private String getBroadcastActionForToggle(String toggleKey) {
        if ("secure_lockscreen_qs_disabled".equals(toggleKey)) {
            return "com.android.settings.DISABLE_QS_TOGGLE_CHANGED";
        } else if ("no_storage_restrict".equals(toggleKey)) {
            return "com.android.settings.DISABLE_SAF_TOGGLE_CHANGED";
        } else if ("window_ignore_secure".equals(toggleKey)) {
            return "com.android.settings.WINDOW_IGNORE_SECURE_TOGGLE_CHANGED";
        } else if ("statusbar_enabled".equals(toggleKey)) {
            return "com.android.settings.STATUSBAR_TOGGLE_CHANGED";
        }
        return null;
    }
    
    /**
     * Update card appearance based on toggle state
     * When enabled: green border and brighter background
     * When disabled: normal appearance
     */
    private void updateToggleCardAppearance(View cardView, boolean isEnabled) {
        try {
            // Create a more visible visual indicator
            if (isEnabled) {
                // Enabled: Add green border and brighter background
                cardView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.argb(40, 76, 175, 80))); // Light green background tint
                
                // Add border using padding and background
                android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                border.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                border.setCornerRadius(12f); // Match card corner radius
                border.setColor(android.graphics.Color.argb(40, 76, 175, 80)); // Light green background
                border.setStroke(3, android.graphics.Color.rgb(76, 175, 80)); // Green border
                cardView.setBackground(border);
                
                // Increase elevation
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    cardView.setElevation(8f);
                }
            } else {
                // Disabled: Normal appearance
                // Reset to default card background
                cardView.setBackgroundResource(R.drawable.custom_preference_background);
                
                // Reset elevation
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    cardView.setElevation(0f);
                }
            }
            
            // Also update title text color for better visibility
            TextView titleView = cardView.findViewById(android.R.id.title);
            if (titleView != null) {
                if (isEnabled) {
                    titleView.setTextColor(android.graphics.Color.rgb(76, 175, 80)); // Green text when enabled
                } else {
                    titleView.setTextColor(activity.getResources().getColor(
                        com.android.settings.R.color.mtx_text_color_primary, null)); // Default text color
                }
            }
        } catch (Exception e) {
            Log.e("SecurityPrivacyGridAdapter", "Error updating toggle card appearance", e);
        }
    }
}
