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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settingslib.widget.LayoutPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to easily add QuickAccessGrid2x2 to any SettingsPreferenceFragment
 * 
 * Usage:
 * In your fragment's onCreate or onViewCreated:
 * 
 * QuickAccessGrid2x2Helper.setupQuickAccessGrid(getContext(), getPreferenceScreen(), 
 *     getActivity(), getMetricsCategory());
 */
public class QuickAccessGrid2x2Helper {

    /**
     * Initialize and setup QuickAccessGrid2x2 in a preference screen
     * 
     * @param context The context (must be an Activity for SubSettingLauncher)
     * @param screen The PreferenceScreen to add the grid to
     * @param activity The Activity (required for SubSettingLauncher)
     * @param sourceMetrics The metrics category for tracking
     */
    public static void setupQuickAccessGrid(Context context, PreferenceScreen screen, 
            android.app.Activity activity, int sourceMetrics) {
        if (context == null || screen == null) {
            Log.e("QuickAccessGrid2x2Helper", "Context or PreferenceScreen is null");
            return;
        }

        if (!(context instanceof android.app.Activity)) {
            Log.e("QuickAccessGrid2x2Helper", "Context must be an Activity for SubSettingLauncher");
            return;
        }

        try {
            // Find or create the LayoutPreference
            LayoutPreference layoutPref = (LayoutPreference) screen.findPreference("quick_access_grid");
            if (layoutPref == null) {
                layoutPref = new LayoutPreference(context, null, 
                        android.R.attr.preferenceStyle);
                layoutPref.setKey("quick_access_grid");
                layoutPref.setSelectable(false);
                layoutPref.setLayoutResource(R.layout.quick_access_grid_2x2);
                screen.addPreference(layoutPref);
            }

            // Get the RecyclerView
            RecyclerView rv = layoutPref.findViewById(R.id.quick_access_grid_recycler);
            if (rv == null) {
                Log.e("QuickAccessGrid2x2Helper", "RecyclerView not found");
                return;
            }

            // Setup GridLayoutManager with 2 columns
            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            // Create default items
            List<QuickAccessGrid2x2Adapter.CardItem> items = createDefaultItems();

            // Set adapter
            rv.setAdapter(new QuickAccessGrid2x2Adapter(activity, items, sourceMetrics));
        } catch (Exception e) {
            Log.e("QuickAccessGrid2x2Helper", "Error setting up QuickAccessGrid2x2", e);
        }
    }

    /**
     * Create default items for the 2x2 grid
     * Apps, Notifications, Connected Devices, SystemGrid
     */
    private static List<QuickAccessGrid2x2Adapter.CardItem> createDefaultItems() {
        List<QuickAccessGrid2x2Adapter.CardItem> items = new ArrayList<>();

        // Apps
        items.add(new QuickAccessGrid2x2Adapter.CardItem(
                R.string.apps_dashboard_title,
                R.string.app_and_notification_dashboard_summary,
                R.drawable.ic_apps_filled,
                "com.android.settings.applications.AppDashboardFragment"));

        // Notifications
        items.add(new QuickAccessGrid2x2Adapter.CardItem(
                R.string.configure_notification_settings,
                R.string.notification_dashboard_summary,
                R.drawable.ic_notifications_filled,
                "com.android.settings.notification.ConfigureNotificationSettings"));

        // Connected Devices
        items.add(new QuickAccessGrid2x2Adapter.CardItem(
                R.string.connected_devices_dashboard_title,
                R.string.connected_devices_dashboard_default_summary,
                R.drawable.ic_devices_other_filled,
                "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment"));

        // SystemGrid (DisplayGrid)
        items.add(new QuickAccessGrid2x2Adapter.CardItem(
                R.string.display_grid_title,
                R.string.display_grid_summary,
                R.drawable.ic_settings_system_dashboard_filled,
                "com.epic.fragments.DisplayGrid"));

        return items;
    }

    /**
     * Create custom items for the 2x2 grid
     * 
     * @param items List of CardItem objects to display
     * @return List of CardItem objects
     */
    public static List<QuickAccessGrid2x2Adapter.CardItem> createCustomItems(
            List<QuickAccessGrid2x2Adapter.CardItem> items) {
        return items;
    }
}

