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
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This is a completely independent card navigation system that can be
 * transferred to any Android ROM. To transfer:
 * 
 * 1. Copy these files:
 *    - CardNavigationHelper.java
 *    - CardNavigationConfig.java
 *    - CardNavigationInitializer.java
 *    - res/layout/toplevel_card_navigation.xml
 * 
 * 2. Ensure these dependencies exist in target ROM:
 *    - androidx.preference.PreferenceFragmentCompat
 *    - androidx.preference.PreferenceScreen
 *    - com.android.settingslib.widget.LayoutPreference
 *    - com.android.settings.core.SubSettingLauncher (or equivalent)
 * 
 * 3. If SubSettingLauncher path differs, update the import in CardNavigationHelper.java
 * 
 * 4. Use CardNavigationInitializer.initialize() in any fragment's onViewCreated
 * 
 * That's it! The system is completely self-contained and ROM-independent.
 */

package com.epic.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.widget.LayoutPreference;

import java.util.List;

/**
 * Independent, reusable helper for setting up card navigation on any Settings page.
 * 
 * <h3>Setup Steps:</h3>
 * <ol>
 *   <li>Add the layout to your preference XML:
 *       <pre>{@code
 *       <com.android.settingslib.widget.LayoutPreference
 *           android:layout="@layout/toplevel_card_navigation"
 *           android:key="my_card_navigation"
 *           android:selectable="false" />
 *       }</pre>
 *   </li>
 *   <li>In your fragment's onViewCreated, call setup():
 *       <pre>{@code
 *       @Override
 *       public void onViewCreated(View view, Bundle savedInstanceState) {
 *           super.onViewCreated(view, savedInstanceState);
 *           view.post(() -> {
 *               CardNavigationHelper.setup(
 *                   getContext(),
 *                   getPreferenceScreen(),
 *                   "my_card_navigation",  // Must match XML key
 *                   getMetricsCategory(),
 *                   new CardNavigationConfig(R.id.card_1, "com.epic.fragments.GestureSettings", R.string.gestures_title),
 *                   new CardNavigationConfig(R.id.card_2, "com.epic.fragments.ExtraSettings", R.string.extras_title),
 *                   new CardNavigationConfig(R.id.card_3, "com.epic.fragments.QuickSettings", R.string.quicksettings_title),
 *                   new CardNavigationConfig(R.id.card_4, "com.epic.fragments.StatusBarSettings", R.string.statusbar_title)
 *               );
 *           });
 *       }
 *       }</pre>
 *   </li>
 * </ol>
 * 
 * <h3>Notes:</h3>
 * <ul>
 *   <li>You can use 1-4 cards (provide fewer CardNavigationConfig objects if needed)</li>
 *   <li>Card IDs must be: R.id.card_1, R.id.card_2, R.id.card_3, or R.id.card_4</li>
 *   <li>Fragment class name must be the full package path</li>
 *   <li>Works on any SettingsPreferenceFragment</li>
 * </ul>
 * 
 * @see CardNavigationConfig
 */
public class CardNavigationHelper {
    
    private static final String TAG = "CardNavigationHelper";

    /**
     * Setup card navigation for a LayoutPreference containing toplevel_card_navigation layout.
     * 
     * @param context The context (must be an Activity)
     * @param screen The PreferenceScreen containing the layout preference
     * @param preferenceKey The key of the LayoutPreference (e.g., "display_grid", "about_device_info")
     * @param sourceMetrics The metrics category for tracking
     * @param cardConfigs Array of card configurations (up to 4 cards)
     */
    public static void setup(Context context, PreferenceScreen screen, String preferenceKey,
            int sourceMetrics, CardNavigationConfig... cardConfigs) {
        if (context == null || screen == null || preferenceKey == null) {
            Log.e(TAG, "Context, PreferenceScreen, or preferenceKey is null");
            return;
        }

        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context must be an Activity");
            return;
        }

        if (cardConfigs == null || cardConfigs.length == 0) {
            Log.w(TAG, "No card configurations provided");
            return;
        }

        try {
            // Find the LayoutPreference by key
            LayoutPreference layoutPref = (LayoutPreference) screen.findPreference(preferenceKey);
            if (layoutPref == null) {
                Log.e(TAG, "LayoutPreference with key '" + preferenceKey + "' not found");
                return;
            }

            Activity activity = (Activity) context;

            // Setup each card
            for (CardNavigationConfig config : cardConfigs) {
                if (config == null) continue;
                
                View card = layoutPref.findViewById(config.cardId);
                if (card != null) {
                    Log.d(TAG, "Found card with ID " + config.cardId + ", setting click listener");
                    card.setOnClickListener(v -> {
                        Log.d(TAG, "Card " + config.cardId + " clicked, launching " + config.fragmentClass);
                        launchFragment(activity, config.fragmentClass, config.titleResId, sourceMetrics);
                    });
                } else {
                    Log.w(TAG, "Card with ID " + config.cardId + " not found in layout");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up card navigation", e);
        }
    }

    /**
     * Setup card navigation with a list of configurations (alternative API)
     */
    public static void setup(Context context, PreferenceScreen screen, String preferenceKey,
            int sourceMetrics, List<CardNavigationConfig> cardConfigs) {
        if (cardConfigs == null || cardConfigs.isEmpty()) {
            Log.w(TAG, "No card configurations provided");
            return;
        }
        setup(context, screen, preferenceKey, sourceMetrics, 
              cardConfigs.toArray(new CardNavigationConfig[0]));
    }

    /**
     * Launch a Settings fragment using SubSettingLauncher
     */
    private static void launchFragment(Activity activity, String fragmentClass, 
            int titleResId, int sourceMetrics) {
        try {
            Log.d(TAG, "Launching fragment: " + fragmentClass);
            new SubSettingLauncher(activity)
                .setDestination(fragmentClass)
                .setTitleRes(titleResId)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
            Log.d(TAG, "Fragment launch initiated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch fragment: " + fragmentClass, e);
            android.widget.Toast.makeText(activity, 
                    "Failed to open: " + fragmentClass, 
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

