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

class SystemGridAdapter extends RecyclerView.Adapter<SystemGridAdapter.CardVH> {
    static class CardItem {
        final int iconResId;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        CardItem(int iconResId, int titleResId, int summaryResId, String destFragment) {
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
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

    private final Context context;
    private final List<CardItem> items;
    private final int sourceMetrics;

    SystemGridAdapter(Context context, List<CardItem> items, int sourceMetrics) {
        this.context = context;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if (viewType == 2) {
            // Pastel red card for reset setting
            layoutRes = R.layout.reset_options_pastel_red_card;
        } else {
            layoutRes = (viewType == 0) 
                ? R.layout.system_grid_card_left 
                : R.layout.system_grid_card_right;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        CardItem item = items.get(position);
        if (holder.iconView != null) {
            holder.iconView.setVisibility(View.GONE);
        }
        holder.titleView.setText(item.titleResId);
        holder.summaryView.setText(item.summaryResId);
        holder.itemView.setOnClickListener(v -> {
            // Check if destination is an Activity (starts with package name but not a fragment)
            if (item.destFragment.contains("com.android.systemui.tuner.TunerActivity")) {
                // Launch SystemUI TunerActivity via Intent
                try {
                    ComponentName component = new ComponentName("com.android.systemui", 
                        "com.android.systemui.tuner.TunerActivity");
                    // Verify component exists before launching
                    PackageManager pm = context.getPackageManager();
                    pm.getActivityInfo(component, 0);
                Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("SystemGridAdapter", "TunerActivity not found", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Log.e("SystemGridAdapter", "Permission denied to launch TunerActivity", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("SystemGridAdapter", "Failed to launch TunerActivity", e);
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
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("SystemGridAdapter", "UserBackupSettingsActivity not found", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Log.e("SystemGridAdapter", "Permission denied to launch UserBackupSettingsActivity", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("SystemGridAdapter", "Failed to launch UserBackupSettingsActivity", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            } else if (item.destFragment.contains("TransportActivity")) {
                // Launch TransportFragment via SubSettingLauncher (proper Settings flow)
                try {
                    // Always use SubSettingLauncher for proper Settings navigation
                    // This ensures the fragment is properly loaded and avoids force closes
                    new SubSettingLauncher(context)
                        .setDestination(com.android.settings.backup.transport.TransportFragment.class.getName())
                        .setTitleRes(R.string.backup_transport_title)
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                } catch (Exception e) {
                    Log.e("SystemGridAdapter", "Failed to launch TransportFragment", e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            } else if (item.destFragment.startsWith("org.lineageos.lineageparts.")) {
                // Launch LineageParts activities via Intent
                try {
                    ComponentName component = new ComponentName("org.lineageos.lineageparts", 
                        item.destFragment);
                    PackageManager pm = context.getPackageManager();
                    pm.getActivityInfo(component, 0);
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("SystemGridAdapter", "LineageParts activity not found: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Log.e("SystemGridAdapter", "Permission denied to launch: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("SystemGridAdapter", "Failed to launch: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            } else if (item.destFragment.contains("AppDashboardFragment") || 
                       item.destFragment.contains("applications.AppDashboardFragment")) {
                // Launch AppDashboardFragment specifically
                try {
                    new SubSettingLauncher(context)
                        .setDestination(com.android.settings.applications.AppDashboardFragment.class.getName())
                        .setTitleRes(item.titleResId)
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                } catch (Exception e) {
                    Log.e("SystemGridAdapter", "Failed to launch AppDashboardFragment", e);
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
                    Log.e("SystemGridAdapter", "Failed to launch fragment: " + item.destFragment, e);
                    Toast.makeText(context, R.string.system_tuner_not_available, 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        CardItem item = items.get(position);
        // Check if this is the reset setting
        if (item.destFragment != null && item.destFragment.contains("ResetDashboardFragment")) {
            return 2; // Pastel red card
        }
        return position % 2; // 0 for left, 1 for right
    }

    @Override
    public int getItemCount() { return items.size(); }
}

