/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.widget.LayoutPreference;

/**
 * Wires click actions for {@code toplevel_card_navigation.xml} on the Settings homepage.
 */
public final class TopLevelCardNavigationHelper {

    private static final String TAG = "TopLevelCardNavigation";

    private static final String KEY_CARD_NAV = "top_level_card_navigation";
    private static final String KEY_EXTRAS_NAV = "top_level_extras_navigation";

    private static final String FRAGMENT_NETWORK =
            "com.android.settings.network.NetworkDashboardFragment";
    private static final String FRAGMENT_DISPLAY = "com.android.settings.DisplaySettings";
    private static final String FRAGMENT_CUSTOM_DASHBOARD =
            "com.epic.fragments.CustomDashboardSettings";
    private static final String FRAGMENT_CONNECTED_DEVICES =
            "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment";

    private TopLevelCardNavigationHelper() {}

    public static void setup(@NonNull Context context, @Nullable PreferenceScreen screen,
            int sourceMetricsCategory) {
        setup(context, screen, sourceMetricsCategory, false);
    }

    /**
     * @param afterlabsExtrasOnTab when true, {@code top_level_extras_navigation} display card
     *        opens Connected devices instead of Display settings.
     */
    public static void setup(@NonNull Context context, @Nullable PreferenceScreen screen,
            int sourceMetricsCategory, boolean afterlabsExtrasOnTab) {
        if (screen == null || !(context instanceof Activity)) {
            return;
        }
        LayoutPreference layoutPref = screen.findPreference(KEY_CARD_NAV);
        final boolean isExtrasNav = layoutPref == null;
        if (layoutPref == null) {
            layoutPref = screen.findPreference(KEY_EXTRAS_NAV);
        }
        if (layoutPref == null) {
            Log.w(TAG, "Card navigation LayoutPreference not found");
            return;
        }
        final Activity activity = (Activity) context;
        final boolean extrasDisplayOpensConnected =
                afterlabsExtrasOnTab && isExtrasNav;

        bindCard(layoutPref, R.id.card_network, () -> launchFragment(activity,
                FRAGMENT_NETWORK, R.string.network_dashboard_title, sourceMetricsCategory));
        bindCard(layoutPref, R.id.card_display, () -> launchFragment(activity,
                extrasDisplayOpensConnected ? FRAGMENT_CONNECTED_DEVICES : FRAGMENT_DISPLAY,
                extrasDisplayOpensConnected
                        ? R.string.connected_devices_dashboard_title
                        : R.string.display_settings,
                sourceMetricsCategory));
        bindCard(layoutPref, R.id.card_custom_dashboard, () -> launchFragment(activity,
                FRAGMENT_CUSTOM_DASHBOARD, R.string.custom_dashboard_title,
                sourceMetricsCategory));
    }

    private static void bindCard(@NonNull LayoutPreference layoutPref, int viewId,
            @NonNull Runnable action) {
        final View card = layoutPref.findViewById(viewId);
        if (card == null) {
            Log.w(TAG, "Card view not found: " + viewId);
            return;
        }
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> action.run());
    }

    private static void launchFragment(@NonNull Activity activity, @NonNull String fragmentClass,
            int titleResId, int sourceMetricsCategory) {
        try {
            new SubSettingLauncher(activity)
                    .setDestination(fragmentClass)
                    .setTitleRes(titleResId)
                    .setSourceMetricsCategory(sourceMetricsCategory)
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch " + fragmentClass, e);
        }
    }
}
