/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bmobile.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.view.View;

import com.android.settings.R;
import com.android.settings.homepage.RestrictedDashboardContentHelper;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;
import java.util.ArrayList;

public class FunDisplaySettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.fun_display_settings);
        } catch (Exception e) {
            android.util.Log.e("FunDisplaySettings", "Error in onCreate", e);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            androidx.preference.PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            
            androidx.preference.Preference layoutPref = screen.findPreference("fun_display_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.fun_display_grid_recycler);
            if (rv == null) {
                return;
            }
            
            Context context = getContext();
            if (context == null) {
                return;
            }
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            java.util.List<FunDisplaySettingsAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Map all HomepagePreferences from top_level_settings_v2.xml to grid cards
            // Network & Internet
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            
            // System Basic Defaults
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.bmobile_dashboard_title,
                    R.string.bmobile_dashboard_summary,
                    "com.bmobile.fragments.BMobileDashboardSettings",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            // Connected Devices
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.connected_devices_dashboard_default_summary,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            
            // Notifications
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.notification_dashboard_summary,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications_filled));
            
            // Sound
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.sound_settings,
                    R.string.sound_dashboard_summary_with_dnd,
                    "com.android.settings.notification.SoundSettings",
                    R.drawable.ic_volume_up_filled));
            
            // Display - opens BMobile DisplayPageGrid
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.display_page_grid_title,
                    R.string.display_page_grid_summary,
                    "com.bmobile.fragments.BmobileDisplayPageGrid",
                    R.drawable.ic_settings_display_filled));
            
            // Battery
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_filled));
            
            // System
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.header_category_system,
                    R.string.system_dashboard_summary,
                    "com.android.settings.system.SystemDashboardFragment",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            // Security and Privacy cards removed per user request
            
            // Location
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.location_settings_title,
                    R.string.location_settings_loading_app_permission_stats,
                    "com.android.settings.location.LocationSettings",
                    R.drawable.ic_settings_location_filled));
            
            // Accessibility
            items.add(new FunDisplaySettingsAdapter.CardItem(
                    FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.accessibility_settings_summary,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility_filled));

            RestrictedDashboardContentHelper.filterFunDisplayGridItems(context, items);
            rv.setAdapter(new FunDisplaySettingsAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("FunDisplaySettings", "Error setting up FunDisplaySettings", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

