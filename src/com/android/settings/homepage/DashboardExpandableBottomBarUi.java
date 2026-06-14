/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.settings.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.MenuItem;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

/**
 * Shared look-and-feel for Custom Dashboard and TopLevel Settings expandable bottom bars.
 */
public final class DashboardExpandableBottomBarUi {

    /** Invoked when a bottom bar item is selected or re-tapped while already selected. */
    public interface ItemAction {
        void onItem(int itemId);
    }

    private DashboardExpandableBottomBarUi() {}

    /**
     * Wires both first-select and reselect so actions still run after returning from a sub-page
     * with the same tab highlighted.
     */
    public static void bindExpandableBarActions(@NonNull ExpandableBottomBar bar,
            @NonNull ItemAction action) {
        final Function3<View, MenuItem, Boolean, Unit> handler =
                (Function3<View, MenuItem, Boolean, Unit>) (view, menuItem, reselected) -> {
                    action.onItem(menuItem.getId());
                    return Unit.INSTANCE;
                };
        bar.setOnItemSelectedListener(handler);
        bar.setOnItemReselectedListener(handler);
    }

    /** Material fallback — {@link BottomNavigationView} ignores re-taps unless reselect is wired. */
    public static void bindMaterialNavActions(@NonNull BottomNavigationView nav,
            @NonNull ItemAction action) {
        nav.setOnItemSelectedListener(item -> {
            action.onItem(item.getItemId());
            return true;
        });
        nav.setOnItemReselectedListener(item -> action.onItem(item.getItemId()));
    }

    /** Accent tones dark enough for expanded tab labels on light backgrounds. */
    public static int readableAccentColor(@NonNull Context context, int slot) {
        final int[] attrs = {
                android.R.color.system_accent1_600,
                android.R.color.system_accent2_600,
                android.R.color.system_accent3_600,
                android.R.color.system_neutral1_700,
        };
        final int index = Math.max(0, Math.min(slot, attrs.length - 1));
        try {
            return context.getColor(attrs[index]);
        } catch (Exception ignored) {
            final TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary,
                    tv, true)) {
                return tv.data;
            }
            return context.getColor(android.R.color.holo_blue_dark);
        }
    }

    public static void applyMaterialNavColors(@NonNull BottomNavigationView nav) {
        final ColorStateList labels = ContextCompat.getColorStateList(nav.getContext(),
                R.color.dashboard_bottom_nav_label);
        if (labels != null) {
            nav.setItemTextColor(labels);
            nav.setItemIconTintList(labels);
        }
    }
}
