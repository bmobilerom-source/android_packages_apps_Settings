/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.MenuItem;
import github.com.st235.lib_expandablebottombar.MenuItemDescriptor;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

/**
 * Expandable bottom bar on the Settings homepage ({@link TopLevelSettings}).
 * Opens Network, Display, System, and Wallpaper destinations.
 */
public final class TopLevelDashboardBottomBarHelper {

    private static final String TAG = "TopLevelDashboardBottomBar";

    /** Standard top-level preference keys (present on most {@code top_level_settings_*.xml}). */
    public static final String KEY_NETWORK = "top_level_network";
    public static final String KEY_DISPLAY = "top_level_display";
    public static final String KEY_SYSTEM = "top_level_system";
    public static final String KEY_WALLPAPER = "top_level_wallpaper";

    public interface Listener {
        void onNetworkSelected();

        void onDisplaySelected();

        void onSystemSelected();

        void onWallpaperSelected();
    }

    private TopLevelDashboardBottomBarHelper() {}

    /**
     * Inflates the homepage bottom bar, falling back to Material if ExpandableBottomBar fails.
     */
    @Nullable
    public static View inflate(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        try {
            return inflater.inflate(R.layout.dashboard_expandable_bottom_bar, parent, false);
        } catch (Throwable t) {
            Log.e(TAG, "ExpandableBottomBar layout failed, using material fallback", t);
        }
        try {
            return inflater.inflate(R.layout.dashboard_expandable_bottom_bar_material, parent,
                    false);
        } catch (Throwable t) {
            Log.e(TAG, "Material bottom bar layout failed", t);
            return null;
        }
    }

    public static void bind(@NonNull View root, @NonNull Context context,
            @NonNull Listener listener) {
        final ExpandableBottomBar expandable = root.findViewById(R.id.dashboard_expandable_bottom_bar);
        final BottomNavigationView material = root.findViewById(R.id.dashboard_bottom_nav);
        if (expandable != null && bindExpandableBar(expandable, context, listener)) {
            return;
        }
        if (expandable != null) {
            expandable.setVisibility(View.GONE);
        }
        if (material != null) {
            bindMaterialBar(material, listener);
            return;
        }
        Log.w(TAG, "No bottom bar view found in layout");
    }

    private static boolean bindExpandableBar(@NonNull ExpandableBottomBar bar,
            @NonNull Context context, @NonNull Listener listener) {
        try {
            final int networkColor = DashboardExpandableBottomBarUi.readableAccentColor(context, 0);
            final int displayColor = DashboardExpandableBottomBarUi.readableAccentColor(context, 1);
            final int systemColor = DashboardExpandableBottomBarUi.readableAccentColor(context, 2);
            final int wallpaperColor = DashboardExpandableBottomBarUi.readableAccentColor(context, 3);
            bar.getMenu().add(new MenuItemDescriptor.Builder(context)
                    .id(R.id.top_level_dashboard_nav_network)
                    .icon(R.drawable.ic_settings_wireless_filled)
                    .textRes(R.string.top_level_dashboard_nav_network)
                    .color(networkColor)
                    .build());
            bar.getMenu().add(new MenuItemDescriptor.Builder(context)
                    .id(R.id.top_level_dashboard_nav_display)
                    .icon(R.drawable.ic_settings_display_filled)
                    .textRes(R.string.top_level_dashboard_nav_display)
                    .color(displayColor)
                    .build());
            bar.getMenu().add(new MenuItemDescriptor.Builder(context)
                    .id(R.id.top_level_dashboard_nav_system)
                    .icon(R.drawable.ic_settings_system_dashboard_filled)
                    .textRes(R.string.top_level_dashboard_nav_system)
                    .color(systemColor)
                    .build());
            bar.getMenu().add(new MenuItemDescriptor.Builder(context)
                    .id(R.id.top_level_dashboard_nav_wallpaper)
                    .icon(R.drawable.ic_settings_wallpaper_filled)
                    .textRes(R.string.top_level_dashboard_nav_wallpaper)
                    .color(wallpaperColor)
                    .build());
            bar.setOnItemSelectedListener(
                    (Function3<View, MenuItem, Boolean, Unit>) (view, menuItem, reselected) -> {
                        dispatchSelection(menuItem.getId(), listener);
                        return Unit.INSTANCE;
                    });
            // Library crashes in onSaveInstanceState when no item was ever selected.
            bar.setSaveEnabled(false);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to configure ExpandableBottomBar", e);
            return false;
        }
    }

    private static void bindMaterialBar(@NonNull BottomNavigationView nav,
            @NonNull Listener listener) {
        nav.setVisibility(View.VISIBLE);
        nav.getMenu().clear();
        nav.inflateMenu(R.menu.top_level_dashboard_bottom_nav);
        DashboardExpandableBottomBarUi.applyMaterialNavColors(nav);
        nav.setOnItemSelectedListener(item -> {
            dispatchSelection(item.getItemId(), listener);
            return true;
        });
    }

    private static void dispatchSelection(int itemId, @NonNull Listener listener) {
        if (itemId == R.id.top_level_dashboard_nav_network) {
            listener.onNetworkSelected();
        } else if (itemId == R.id.top_level_dashboard_nav_display) {
            listener.onDisplaySelected();
        } else if (itemId == R.id.top_level_dashboard_nav_system) {
            listener.onSystemSelected();
        } else if (itemId == R.id.top_level_dashboard_nav_wallpaper) {
            listener.onWallpaperSelected();
        }
    }

    @Nullable
    public static View findBottomBar(@NonNull View root) {
        View expandable = root.findViewById(R.id.dashboard_expandable_bottom_bar);
        if (expandable != null) {
            return expandable;
        }
        return root.findViewById(R.id.dashboard_bottom_nav);
    }
}
