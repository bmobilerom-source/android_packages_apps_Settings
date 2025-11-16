/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.ExpandableBottomBarMenuItem;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Wires the Custom Dashboard bottom bar (ExpandableBottomBar with Material fallback).
 */
public final class CustomDashboardBottomBarHelper {

    private static final String TAG = "CustomDashboardBottomBar";

    public interface Listener {
        void onStylesSelected();

        void onLayoutModeSelected();

        void onApplyAndHome();
    }

    private CustomDashboardBottomBarHelper() {}

    public static void bind(@NonNull View root, @NonNull Context context,
            @NonNull Listener listener) {
        final ExpandableBottomBar expandable = root.findViewById(
                R.id.custom_dashboard_expandable_bottom_bar);
        if (expandable != null) {
            bindExpandableBar(expandable, context, listener);
            return;
        }
        final BottomNavigationView material = root.findViewById(R.id.custom_dashboard_bottom_nav);
        if (material != null) {
            bindMaterialBar(material, listener);
            return;
        }
        Log.w(TAG, "No bottom bar view found in layout");
    }

    private static void bindExpandableBar(@NonNull ExpandableBottomBar bar,
            @NonNull Context context, @NonNull Listener listener) {
        try {
            final int accent = context.getColor(android.R.color.system_accent1_200);
            final int stylesColor = context.getColor(android.R.color.system_accent2_200);
            final int layoutColor = context.getColor(android.R.color.system_accent3_200);
            bar.addItems(new ExpandableBottomBarMenuItem.Builder(context)
                    .addItem(R.id.custom_dashboard_nav_styles,
                            R.drawable.ic_custom_dashboard,
                            R.string.custom_dashboard_nav_styles, stylesColor)
                    .addItem(R.id.custom_dashboard_nav_layout_mode,
                            R.drawable.ic_settings_display_filled,
                            R.string.custom_dashboard_nav_layout, layoutColor)
                    .addItem(R.id.custom_dashboard_nav_apply,
                            R.drawable.ic_restore,
                            R.string.custom_dashboard_nav_apply, accent)
                    .build());
            bar.setOnItemSelectedListener(
                    (Function2<View, ExpandableBottomBarMenuItem, Unit>) (view, menuItem) -> {
                        dispatchSelection(menuItem.getItemId(), listener);
                        return Unit.INSTANCE;
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to configure ExpandableBottomBar", e);
        }
    }

    private static void bindMaterialBar(@NonNull BottomNavigationView nav,
            @NonNull Listener listener) {
        nav.setOnItemSelectedListener(item -> {
            dispatchSelection(item.getItemId(), listener);
            return true;
        });
    }

    private static void dispatchSelection(int itemId, @NonNull Listener listener) {
        if (itemId == R.id.custom_dashboard_nav_styles) {
            listener.onStylesSelected();
        } else if (itemId == R.id.custom_dashboard_nav_layout_mode) {
            listener.onLayoutModeSelected();
        } else if (itemId == R.id.custom_dashboard_nav_apply) {
            listener.onApplyAndHome();
        }
    }

    @Nullable
    public static View findBottomBar(@NonNull View root) {
        View expandable = root.findViewById(R.id.custom_dashboard_expandable_bottom_bar);
        if (expandable != null) {
            return expandable;
        }
        return root.findViewById(R.id.custom_dashboard_bottom_nav);
    }
}
