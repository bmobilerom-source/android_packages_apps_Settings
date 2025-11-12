package com.epic.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

class QuickAccessGrid2x2Adapter extends RecyclerView.Adapter<QuickAccessGrid2x2Adapter.CardVH> {

    static class CardItem {
        final int titleResId;
        final int summaryResId;
        final int iconResId;
        final String destFragment;
        
        CardItem(int titleResId, int summaryResId, int iconResId, String destFragment) {
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.destFragment = destFragment;
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

    QuickAccessGrid2x2Adapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quick_access_grid_2x2_card, parent, false);
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
            try {
                holder.iconView.setImageResource(item.iconResId);
                holder.iconView.setVisibility(View.VISIBLE);
            } catch (android.content.res.Resources.NotFoundException e) {
                Log.w("QuickAccessGrid2x2Adapter", "Icon not found: " + item.iconResId, e);
                holder.iconView.setVisibility(View.GONE);
            }
        }
        
        // Set click listener for the entire card
        holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
    }
    
    /**
     * Launch destination fragment or activity
     */
    private void launchDestination(String destFragment, int titleResId) {
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("QuickAccessGrid2x2Adapter", "Empty destination fragment");
            return;
        }
        
        try {
            // Handle LineageParts activities
            if (destFragment.startsWith("org.lineageos.lineageparts.")) {
                launchLineagePartsActivity(destFragment);
            } else {
                // Launch fragment via SubSettingLauncher
                try {
                    launchSettingsFragment(destFragment, titleResId);
                } catch (Exception e) {
                    Log.e("QuickAccessGrid2x2Adapter", "Fragment not found: " + destFragment, e);
                    showErrorToast();
                }
            }
        } catch (Exception e) {
            Log.e("QuickAccessGrid2x2Adapter", "Failed to launch: " + destFragment, e);
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
    public int getItemCount() { 
        return items.size(); 
    }
}

