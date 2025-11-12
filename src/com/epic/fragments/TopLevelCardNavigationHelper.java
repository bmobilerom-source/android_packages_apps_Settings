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
            // Find the LayoutPreference
            LayoutPreference layoutPref = (LayoutPreference) screen.findPreference("about_device_info");
            if (layoutPref == null) {
                Log.e("TopLevelCardNavigationHelper", "LayoutPreference not found");
                return;
            }

            android.app.Activity activity = (android.app.Activity) context;

            // Card 1: Gestures
            View card1 = layoutPref.findViewById(R.id.card_1);
            if (card1 != null) {
                card1.setOnClickListener(v -> {
                    launchFragment(activity, "com.epic.fragments.GestureSettings", 
                            R.string.gestures_title, sourceMetrics);
                });
            }

            // Card 2: Extras
            View card2 = layoutPref.findViewById(R.id.card_2);
            if (card2 != null) {
                card2.setOnClickListener(v -> {
                    launchFragment(activity, "com.epic.fragments.ExtraSettings", 
                            R.string.extras_title, sourceMetrics);
                });
            }

            // Card 3: Quick Settings
            View card3 = layoutPref.findViewById(R.id.card_3);
            if (card3 != null) {
                card3.setOnClickListener(v -> {
                    launchFragment(activity, "com.epic.fragments.QuickSettings", 
                            R.string.quicksettings_title, sourceMetrics);
                });
            }

            // Card 4: Status Bar
            View card4 = layoutPref.findViewById(R.id.card_4);
            if (card4 != null) {
                card4.setOnClickListener(v -> {
                    launchFragment(activity, "com.epic.fragments.StatusBarSettings", 
                            R.string.statusbar_title, sourceMetrics);
                });
            }
        } catch (Exception e) {
            Log.e("TopLevelCardNavigationHelper", "Error setting up card navigation", e);
        }
    }

    private static void launchFragment(android.app.Activity activity, String fragmentClass, 
            int titleResId, int sourceMetrics) {
        try {
            new SubSettingLauncher(activity)
                .setDestination(fragmentClass)
                .setTitleRes(titleResId)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
        } catch (Exception e) {
            Log.e("TopLevelCardNavigationHelper", "Failed to launch fragment: " + fragmentClass, e);
        }
    }
}

