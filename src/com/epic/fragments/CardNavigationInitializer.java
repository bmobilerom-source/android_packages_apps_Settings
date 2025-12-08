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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceScreen;

/**
 * Standalone, independent initializer for card navigation.
 * 
 * This class can be used on ANY Settings page and is completely transferable
 * to other ROMs. It automatically sets up card navigation when called from
 * any fragment's onViewCreated method.
 * 
 * <h3>Usage:</h3>
 * <pre>{@code
 * // In your fragment's onViewCreated:
 * @Override
 * public void onViewCreated(View view, Bundle savedInstanceState) {
 *     super.onViewCreated(view, savedInstanceState);
 *     CardNavigationInitializer.initialize(
 *         this,  // Your SettingsPreferenceFragment
 *         "your_preference_key",  // Key of LayoutPreference
 *         new CardNavigationConfig[] {
 *             new CardNavigationConfig(R.id.card_1, "com.epic.fragments.GestureSettings", R.string.gestures_title),
 *             new CardNavigationConfig(R.id.card_2, "com.epic.fragments.ExtraSettings", R.string.extras_title),
 *             new CardNavigationConfig(R.id.card_3, "com.epic.fragments.QuickSettings", R.string.quicksettings_title),
 *             new CardNavigationConfig(R.id.card_4, "com.epic.fragments.StatusBarSettings", R.string.statusbar_title)
 *         }
 *     );
 * }
 * }</pre>
 * 
 * <h3>Features:</h3>
 * <ul>
 *   <li>Completely independent - no ROM-specific dependencies</li>
 *   <li>Works on any SettingsPreferenceFragment</li>
 *   <li>Auto-handles view inflation timing</li>
 *   <li>Safe error handling - won't crash if layout not found</li>
 *   <li>Transferable to any ROM - just copy the files</li>
 * </ul>
 */
public class CardNavigationInitializer {
    
    private static final String TAG = "CardNavigationInit";

    /**
     * Initialize card navigation for a fragment.
     * This is the main entry point - call this from your fragment's onViewCreated.
     * 
     * @param fragment The SettingsPreferenceFragment containing the layout
     * @param preferenceKey The key of the LayoutPreference in your XML
     * @param cardConfigs Array of card configurations (1-4 cards)
     */
    public static void initialize(androidx.preference.PreferenceFragmentCompat fragment,
            String preferenceKey, CardNavigationConfig... cardConfigs) {
        if (fragment == null || preferenceKey == null) {
            Log.e(TAG, "Fragment or preferenceKey is null");
            return;
        }

        if (cardConfigs == null || cardConfigs.length == 0) {
            Log.w(TAG, "No card configurations provided");
            return;
        }

        try {
            View rootView = fragment.getView();
            if (rootView == null) {
                Log.e(TAG, "Fragment view is null - call this from onViewCreated");
                return;
            }

            // Use post to ensure layout is fully inflated
            rootView.post(() -> {
                try {
                    PreferenceScreen screen = fragment.getPreferenceScreen();
                    if (screen == null) {
                        Log.e(TAG, "PreferenceScreen is null");
                        return;
                    }

                    Context context = fragment.getContext();
                    if (context == null || !(context instanceof Activity)) {
                        Log.e(TAG, "Context is null or not an Activity");
                        return;
                    }

                    int metricsCategory = getMetricsCategory(fragment);

                    // Delegate to CardNavigationHelper
                    CardNavigationHelper.setup(
                        context,
                        screen,
                        preferenceKey,
                        metricsCategory,
                        cardConfigs
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing card navigation", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up card navigation initializer", e);
        }
    }

    /**
     * Get metrics category from fragment using reflection (ROM-independent)
     */
    private static int getMetricsCategory(androidx.preference.PreferenceFragmentCompat fragment) {
        try {
            // Try to get metrics category using reflection
            java.lang.reflect.Method method = fragment.getClass().getMethod("getMetricsCategory");
            Object result = method.invoke(fragment);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
            // If method doesn't exist or fails, use default
            Log.d(TAG, "Could not get metrics category, using default", e);
        }
        // Default metrics category (Settings.METRICS_CATEGORY_UNKNOWN = 0)
        return 0;
    }
}

