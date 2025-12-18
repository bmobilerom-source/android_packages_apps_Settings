/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epic.fragments;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.widget.LayoutPreference;

/**
 * Helper class to setup TopLevelCardNavigation layout with click listeners
 * for each card to open Anatolia fragments
 */
public class TopLevelCardNavigationHelper {

    /**
     * Setup click listeners for all cards in toplevel_card_navigation layout
     * 
     * @param context The context (must be an Activity)
     * @param screen The PreferenceScreen containing the layout preference
     * @param sourceMetrics The metrics category for tracking
     */
    public static void setupCardNavigation(Context context, PreferenceScreen screen, 
            int sourceMetrics) {
        if (context == null || screen == null) {
            Log.e("TopLevelCardNavigationHelper", "Context or PreferenceScreen is null");
            return;
        }

        if (!(context instanceof android.app.Activity)) {
            Log.e("TopLevelCardNavigationHelper", "Context must be an Activity");
            return;
        }

        try {
            // Find the LayoutPreference - try both possible keys
            LayoutPreference layoutPref = (LayoutPreference) screen.findPreference("display_grid");
            if (layoutPref == null) {
                // Fallback to about_device_info for TopLevelSettings
                layoutPref = (LayoutPreference) screen.findPreference("about_device_info");
            }
            if (layoutPref == null) {
                Log.e("TopLevelCardNavigationHelper", "LayoutPreference not found (tried 'display_grid' and 'about_device_info')");
                return;
            }

            android.app.Activity activity = (android.app.Activity) context;

            // Card 1: My Journal - Launch journal app
            View card1 = layoutPref.findViewById(R.id.card_1);
            if (card1 != null) {
                Log.d("TopLevelCardNavigationHelper", "Found card_1, setting click listener for journal app");
                card1.setOnClickListener(v -> {
                    Log.d("TopLevelCardNavigationHelper", "Card 1 clicked, launching journal app");
                    launchApp(activity, "com.demizo.daily_you", "com.demizo.daily_you.MainActivity");
                });
            } else {
                Log.e("TopLevelCardNavigationHelper", "card_1 not found in layout");
            }

            // Card 2: MySession - Launch session app
            View card2 = layoutPref.findViewById(R.id.card_2);
            if (card2 != null) {
                Log.d("TopLevelCardNavigationHelper", "Found card_2, setting click listener for session app");
                card2.setOnClickListener(v -> {
                    Log.d("TopLevelCardNavigationHelper", "Card 2 clicked, launching session app");
                    launchApp(activity, "network.loki.messenger", "network.loki.messenger.RoutingActivity");
                });
            } else {
                Log.e("TopLevelCardNavigationHelper", "card_2 not found in layout");
            }

            // Card 3: Private Space - Launch private space
            View card3 = layoutPref.findViewById(R.id.card_3);
            if (card3 != null) {
                Log.d("TopLevelCardNavigationHelper", "Found card_3, setting click listener for private space");
                card3.setOnClickListener(v -> {
                    Log.d("TopLevelCardNavigationHelper", "Card 3 clicked, launching private space");
                    launchFragment(activity, "com.android.settings.privatespace.PrivateSpaceSetupActivity",
                            R.string.private_space_title, sourceMetrics);
                });
            } else {
                Log.e("TopLevelCardNavigationHelper", "card_3 not found in layout");
            }

            // Card 4: Duress - Leave as is for now
            View card4 = layoutPref.findViewById(R.id.card_4);
            if (card4 != null) {
                Log.d("TopLevelCardNavigationHelper", "Found card_4, duress card - leaving as is");
                // Duress functionality not implemented yet
            } else {
                Log.e("TopLevelCardNavigationHelper", "card_4 not found in layout");
            }
        } catch (Exception e) {
            Log.e("TopLevelCardNavigationHelper", "Error setting up card navigation", e);
        }
    }

    private static void launchFragment(android.app.Activity activity, String fragmentClass,
            int titleResId, int sourceMetrics) {
        try {
            Log.d("TopLevelCardNavigationHelper", "Launching fragment: " + fragmentClass);
            new SubSettingLauncher(activity)
                .setDestination(fragmentClass)
                .setTitleRes(titleResId)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
            Log.d("TopLevelCardNavigationHelper", "Fragment launch initiated successfully");
        } catch (Exception e) {
            Log.e("TopLevelCardNavigationHelper", "Failed to launch fragment: " + fragmentClass, e);
            android.widget.Toast.makeText(activity,
                    "Failed to open: " + fragmentClass,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private static void launchApp(android.app.Activity activity, String packageName, String className) {
        try {
            Log.d("TopLevelCardNavigationHelper", "Launching app: " + packageName + "/" + className);
            android.content.Intent intent = new android.content.Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            Log.d("TopLevelCardNavigationHelper", "App launch initiated successfully");
        } catch (Exception e) {
            Log.e("TopLevelCardNavigationHelper", "Failed to launch app: " + packageName + "/" + className, e);
            android.widget.Toast.makeText(activity,
                    "App not installed: " + packageName,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

