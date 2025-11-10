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

class QuickAccessGridAdapter extends RecyclerView.Adapter<QuickAccessGridAdapter.CardVH> {
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

    private final Context context;
    private final List<CardItem> items;
    private final int sourceMetrics;

    QuickAccessGridAdapter(Context context, List<CardItem> items, int sourceMetrics) {
        this.context = context;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quick_access_card, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        CardItem item = items.get(position);
        
        holder.titleView.setText(item.titleResId);
        holder.summaryView.setText(item.summaryResId);
        holder.iconView.setImageResource(item.iconResId);
        
        holder.itemView.setOnClickListener(v -> {
            if (item.destFragment == null || item.destFragment.isEmpty()) {
                return;
            }
            
            try {
                // Handle LineageParts activities
                if (item.destFragment.startsWith("org.lineageos.lineageparts.")) {
                    ComponentName component = new ComponentName("org.lineageos.lineageparts", 
                        item.destFragment);
                    PackageManager pm = context.getPackageManager();
                    pm.getActivityInfo(component, 0);
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    // Launch fragment via SubSettingLauncher
                    new SubSettingLauncher(context)
                        .setDestination(item.destFragment)
                        .setTitleRes(item.titleResId)
                        .setArguments(new Bundle())
                        .setSourceMetricsCategory(sourceMetrics)
                        .launch();
                }
            } catch (Exception e) {
                Log.e("QuickAccessGridAdapter", "Failed to launch: " + item.destFragment, e);
                Toast.makeText(context, R.string.system_tuner_not_available, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }
}

