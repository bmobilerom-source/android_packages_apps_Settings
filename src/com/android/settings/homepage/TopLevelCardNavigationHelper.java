/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.app.Activity;
import android.content.ComponentName;
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
import com.bmobile.fragments.ZenithController;

/**
 * Wires click actions for {@code toplevel_card_navigation.xml} on the Settings homepage.
 */
public final class TopLevelCardNavigationHelper {

    private static final String TAG = "TopLevelCardNavigation";

    private static final String KEY_CARD_NAV = "top_level_card_navigation";
    private static final String KEY_EXTRAS_NAV = "top_level_extras_navigation";

    private static final String FRAGMENT_NETWORK =
            "com.android.settings.network.NetworkDashboardFragment";
    private static final String AURORA_STORE_PACKAGE = "com.aurora.store";
    private static final String SEED_VAULT_PACKAGE = "com.stevesoltys.seedvault";
    private static final String SEED_VAULT_SETTINGS_ACTIVITY =
            "com.stevesoltys.seedvault.settings.SettingsActivity";

    private TopLevelCardNavigationHelper() {}

    public static void setup(@NonNull Context context, @Nullable PreferenceScreen screen,
            int sourceMetricsCategory) {
        setup(context, screen, sourceMetricsCategory, false);
    }

    /**
     * @param afterlabsExtrasOnTab when true, middle card on DynamicTabs Connect tab is Wellbeing
     *        and opens Zenith.
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
        final boolean displayOpensWellbeing = afterlabsExtrasOnTab && isExtrasNav;
        final int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
        final String displayPageGridFragment =
                DashboardStyleHelper.getBrandDisplayPageGridFragmentClass(currentStyle);

        if (displayOpensWellbeing) {
            final TextView displayTitle = layoutPref.findViewById(R.id.card_display_title);
            if (displayTitle != null) {
                displayTitle.setText(R.string.wellbeing_title);
            }
        }

        bindCard(layoutPref, R.id.card_network, () -> launchFragment(activity,
                FRAGMENT_NETWORK, R.string.network_dashboard_title, sourceMetricsCategory));
        bindCard(layoutPref, R.id.card_display, () -> {
            if (displayOpensWellbeing) {
                ZenithController.launch(activity);
                return;
            }
            launchFragment(activity, displayPageGridFragment,
                    R.string.display_page_grid_title, sourceMetricsCategory);
        });
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

    /** External-app tiles on AfterLabs System tab (no fragment in XML). */
    public static void setupAfterlabsSystemTabPreferences(@NonNull Context context,
            @Nullable PreferenceScreen screen) {
        if (screen == null || !(context instanceof Activity)) {
            return;
        }
        final Activity activity = (Activity) context;

        androidx.preference.Preference auroraPref =
                screen.findPreference("top_level_aurora_store");
        if (auroraPref != null) {
            auroraPref.setOnPreferenceClickListener(preference -> {
                launchAuroraStore(activity);
                return true;
            });
        }

        androidx.preference.Preference backupPref = screen.findPreference("top_level_backup");
        if (backupPref != null) {
            backupPref.setOnPreferenceClickListener(preference -> {
                launchSeedVault(activity);
                return true;
            });
        }
    }

    /** @deprecated Use {@link #setupAfterlabsSystemTabPreferences} */
    public static void setupAuroraStorePreference(@NonNull Context context,
            @Nullable PreferenceScreen screen) {
        setupAfterlabsSystemTabPreferences(context, screen);
    }

    private static void launchSeedVault(@NonNull Activity activity) {
        try {
            Intent launch = new Intent();
            launch.setComponent(new ComponentName(SEED_VAULT_PACKAGE, SEED_VAULT_SETTINGS_ACTIVITY));
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(launch);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Seed Vault", e);
            android.widget.Toast.makeText(activity, R.string.seed_vault_not_installed,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
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
