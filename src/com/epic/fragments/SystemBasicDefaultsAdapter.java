package com.epic.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

class SystemBasicDefaultsAdapter extends RecyclerView.Adapter<SystemBasicDefaultsAdapter.CardVH> {
    static final int CARD_TYPE_LARGE_LEFT = 0;
    static final int CARD_TYPE_ABOUT_US = 1;
    static final int CARD_TYPE_STATUS_BAR = 2;
    static final int CARD_TYPE_CIRCULAR_BUTTON = 3;

    static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        final Integer iconResId;
        final String destFragment;
        final String buttonText; // For button text
        
        CardItem(int cardType, int titleResId, int summaryResId, Integer iconResId, String destFragment) {
            this.cardType = cardType;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.destFragment = destFragment;
            this.buttonText = null;
        }
        
        CardItem(int cardType, int titleResId, int summaryResId, Integer iconResId, String destFragment, String buttonText) {
            this.cardType = cardType;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.destFragment = destFragment;
            this.buttonText = buttonText;
        }
    }

    static class CardVH extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        final TextView buttonTextView;
        final View buttonContainer;
        
        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.buttonTextView = itemView.findViewById(R.id.system_basic_defaults_button);
            this.buttonContainer = itemView.findViewById(R.id.system_basic_defaults_button_container);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SystemBasicDefaultsAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case CARD_TYPE_LARGE_LEFT:
                layoutRes = R.layout.system_basic_defaults_card_large_left;
                break;
            case CARD_TYPE_ABOUT_US:
                layoutRes = R.layout.system_basic_defaults_card_about_us;
                break;
            case CARD_TYPE_STATUS_BAR:
                layoutRes = R.layout.system_basic_defaults_card_status_bar;
                break;
            case CARD_TYPE_CIRCULAR_BUTTON:
                layoutRes = R.layout.system_basic_defaults_card_circular_button;
                break;
            default:
                layoutRes = R.layout.system_basic_defaults_card_large_left;
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
        
        // Handle icon - hide icons for InfinitySuite-style cards (except where needed)
        // Hide icons if not available to prevent crashes
        if (holder.iconView != null) {
            if (item.iconResId != null && (item.cardType == CARD_TYPE_ABOUT_US || item.cardType == CARD_TYPE_STATUS_BAR || item.cardType == CARD_TYPE_CIRCULAR_BUTTON)) {
                try {
                    holder.iconView.setImageResource(item.iconResId);
                    holder.iconView.setVisibility(View.VISIBLE);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.w("SystemBasicDefaultsAdapter", "Icon not found: " + item.iconResId, e);
                    holder.iconView.setVisibility(View.GONE);
                }
            } else {
                holder.iconView.setVisibility(View.GONE);
            }
        }
        
        // Handle button text for large left card
        if (holder.buttonTextView != null && item.buttonText != null) {
            holder.buttonTextView.setText(item.buttonText);
            holder.buttonTextView.setVisibility(View.VISIBLE);
            // Make button container clickable independently
            if (holder.buttonContainer != null) {
                holder.buttonContainer.setClickable(true);
                holder.buttonContainer.setFocusable(true);
                holder.buttonContainer.setOnClickListener(v -> {
                    launchDestination(item.destFragment, item.titleResId);
                });
            }
        }
        
        // Set click listener for the entire card (except large left card which has button)
        if (item.cardType != CARD_TYPE_LARGE_LEFT || item.buttonText == null) {
            holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
        } else {
            // Large left card - make it non-clickable, only button is clickable
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
        }
    }
    
    /**
     * Launch destination fragment, activity, or intent
     * Handles Aurora Store intent, Settings fragments, and LineageParts activities
     */
    private void launchDestination(String destFragment, int titleResId) {
        // Handle special keys
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("SystemBasicDefaultsAdapter", "Empty destination, using Anatolia as fallback");
            destFragment = "com.epic.Anatolia";
        }
        
        // Special handling for Aurora Store intent
        if ("aurora_store".equals(destFragment)) {
            launchAuroraStore();
            return;
        }
        
        // Special handling for Default Apps intent
        if ("default_apps".equals(destFragment)) {
            launchDefaultApps();
            return;
        }
        
        try {
            // Handle LineageParts activities
            if (destFragment.startsWith("org.lineageos.lineageparts.")) {
                launchLineagePartsActivity(destFragment);
            } else {
                // Launch fragment via SubSettingLauncher (Settings app standard)
                try {
                    launchSettingsFragment(destFragment, titleResId);
                } catch (Exception e) {
                    Log.w("SystemBasicDefaultsAdapter", "Fragment not found: " + destFragment + ", using Anatolia fallback", e);
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
            Log.e("SystemBasicDefaultsAdapter", "Failed to launch: " + destFragment, e);
            // Final fallback to Anatolia
            try {
                launchSettingsFragment("com.epic.Anatolia", R.string.anatolia_settings_title);
            } catch (Exception fallbackException) {
                showErrorToast();
            }
        }
    }
    
    /**
     * Launch Default Apps via intent
     */
    private void launchDefaultApps() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            Log.d("SystemBasicDefaultsAdapter", "Default Apps launched successfully");
        } catch (Exception e) {
            Log.e("SystemBasicDefaultsAdapter", "Failed to launch Default Apps", e);
            showErrorToast();
        }
    }
    
    /**
     * Launch Aurora Store via intent
     */
    private void launchAuroraStore() {
        try {
            // Try common Aurora Store package names
            String[] auroraPackages = {
                "com.aurora.store",
                "com.aurora.services",
                "foundation.e.apps"
            };
            
            PackageManager pm = activity.getPackageManager();
            Intent intent = null;
            
            for (String pkg : auroraPackages) {
                try {
                    Intent testIntent = pm.getLaunchIntentForPackage(pkg);
                    if (testIntent != null) {
                        intent = testIntent;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        break;
                    }
                } catch (Exception e) {
                    // Continue to next package
                }
            }
            
            if (intent != null) {
                activity.startActivity(intent);
                Log.d("SystemBasicDefaultsAdapter", "Aurora Store launched successfully");
            } else {
                // Fallback: Try to open Play Store or show error
                try {
                    Intent playStoreIntent = pm.getLaunchIntentForPackage("com.android.vending");
                    if (playStoreIntent != null) {
                        activity.startActivity(playStoreIntent);
                    } else {
                        showErrorToast();
                    }
                } catch (Exception e) {
                    showErrorToast();
                }
            }
        } catch (Exception e) {
            Log.e("SystemBasicDefaultsAdapter", "Failed to launch Aurora Store", e);
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
                    // Grid has 3 columns:
                    // - Large left card spans 2 columns
                    // - All other cards (including circular buttons) span 1 column
                    int viewType = getItemViewType(position);
                    if (viewType == CARD_TYPE_LARGE_LEFT) {
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

