/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    private static final String FRAGMENT_DISPLAY_PAGE_GRID =
            "com.bmobile.fragments.DisplayPageGrid";
    private static final String FRAGMENT_CONNECTED_DEVICES =
            "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment";
    private static final String AURORA_STORE_PACKAGE = "com.aurora.store";

    private TopLevelCardNavigationHelper() {}

    public static void setup(@NonNull Context context, @Nullable PreferenceScreen screen,
            int sourceMetricsCategory) {
        setup(context, screen, sourceMetricsCategory, false);
    }

    /**
     * @param afterlabsExtrasOnTab when true, middle card label is Bluetooth and opens Connected
     *        devices (AfterLabs tab 0 only).
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
        final boolean displayOpensConnected = afterlabsExtrasOnTab && isExtrasNav;

        if (displayOpensConnected) {
            final TextView displayTitle = layoutPref.findViewById(R.id.card_display_title);
            if (displayTitle != null) {
                displayTitle.setText(R.string.afterlabs_card_bluetooth_title);
            }
        }

        bindCard(layoutPref, R.id.card_network, () -> launchFragment(activity,
                FRAGMENT_NETWORK, R.string.network_dashboard_title, sourceMetricsCategory));
        bindCard(layoutPref, R.id.card_display, () -> launchFragment(activity,
                displayOpensConnected ? FRAGMENT_CONNECTED_DEVICES : FRAGMENT_DISPLAY_PAGE_GRID,
                displayOpensConnected
                        ? R.string.connected_devices_dashboard_title
                        : R.string.display_page_grid_title,
                sourceMetricsCategory));
        bindCard(layoutPref, R.id.card_custom_dashboard, () -> {
            final int style = DashboardStyleHelper.getDashboardStyle(context);
            launchFragment(activity,
                    DashboardStyleHelper.getBrandDashboardFragmentClass(style),
                    DashboardStyleHelper.getBrandDashboardTitleResId(style),
                    sourceMetricsCategory);
        });

        final TextView dashboardTitle = layoutPref.findViewById(R.id.card_custom_dashboard_title);
        if (dashboardTitle != null) {
            final int style = DashboardStyleHelper.getDashboardStyle(context);
            dashboardTitle.setText(DashboardStyleHelper.getBrandDashboardTitleResId(style));
        }
    }

    /** Aurora Store tile on AfterLabs tab 2 (no fragment in XML). */
    public static void setupAuroraStorePreference(@NonNull Context context,
            @Nullable PreferenceScreen screen) {
        if (screen == null || !(context instanceof Activity)) {
            return;
        }
        androidx.preference.Preference pref = screen.findPreference("top_level_aurora_store");
        if (pref == null) {
            return;
        }
        pref.setOnPreferenceClickListener(preference -> {
            launchAuroraStore((Activity) context);
            return true;
        });
    }

    private static void launchAuroraStore(@NonNull Activity activity) {
        try {
            Intent launch = activity.getPackageManager().getLaunchIntentForPackage(
                    AURORA_STORE_PACKAGE);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(launch);
                return;
            }
            Intent market = new Intent(Intent.ACTION_VIEW);
            market.setData(android.net.Uri.parse("market://details?id=" + AURORA_STORE_PACKAGE));
            market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(market);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Aurora Store", e);
            android.widget.Toast.makeText(activity, R.string.aurora_store_summary,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
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
