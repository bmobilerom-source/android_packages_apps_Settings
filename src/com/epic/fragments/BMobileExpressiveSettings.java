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

package com.epic.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;
import java.util.ArrayList;

public class BMobileExpressiveSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.bmobile_expressive_settings);
        } catch (Exception e) {
            android.util.Log.e("BMobileExpressiveSettings", "Error in onCreate", e);
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
            
            // Setup extended homepage widgets click listeners with delay
            View rootView = getView();
            if (rootView != null) {
                rootView.postDelayed(() -> setupExtendedHomepageWidgetsClickListeners(), 200);
            }
            
            // Hide search bar
            hideSearchBar();
            
            androidx.preference.Preference layoutPref = screen.findPreference("bmobile_expressive_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                android.util.Log.e("BMobileExpressiveSettings", "bmobile_expressive_grid LayoutPreference not found");
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.bmobile_expressive_grid_recycler);
            if (rv == null) {
                android.util.Log.e("BMobileExpressiveSettings", "bmobile_expressive_grid_recycler RecyclerView not found");
                // Try to find it with a delay
                rootView.postDelayed(() -> {
                    androidx.recyclerview.widget.RecyclerView delayedRv = lp.findViewById(R.id.bmobile_expressive_grid_recycler);
                    if (delayedRv != null) {
                        setupGrid(delayedRv, getActivity(), getContext());
                    }
                }, 300);
                return;
            }
            
            setupGrid(rv, getActivity(), getContext());
        } catch (Exception e) {
            android.util.Log.e("BMobileExpressiveSettings", "Error setting up BMobileExpressiveSettings", e);
        }
    }
    
    private void setupGrid(androidx.recyclerview.widget.RecyclerView rv, android.app.Activity activity, Context context) {
        try {
            if (context == null) {
                context = getContext();
            }
            if (context == null) {
                return;
            }
            
            if (activity == null) {
                activity = getActivity();
            }
            if (activity == null) {
                return;
            }
            
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            java.util.List<BMobileExpressiveSettingsAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Network & Internet
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            
            // System Basic Defaults
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.custom_dashboard_title,
                    R.string.custom_dashboard_summary,
                    "com.epic.fragments.CustomDashboardSettings",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            // Connected Devices
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.connected_devices_dashboard_default_summary,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            
            // Notifications
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.notification_dashboard_summary,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications_filled));
            
            // Sound
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.sound_settings,
                    R.string.sound_dashboard_summary_with_dnd,
                    "com.android.settings.notification.SoundSettings",
                    R.drawable.ic_volume_up_filled));
            
            // Display - opens DisplayPageGrid
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.display_settings,
                    R.string.display_dashboard_summary,
                    "com.android.settings.DisplaySettings",
                    R.drawable.ic_settings_display_filled));
            
            // Battery
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_filled));
            
            // System
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.header_category_system,
                    R.string.system_dashboard_summary,
                    "com.android.settings.system.SystemDashboardFragment",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            // Security
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.security_settings_title,
                    R.string.security_dashboard_summary,
                    "com.android.settings.security.SecuritySettings",
                    R.drawable.ic_settings_security_filled));
            
            // Privacy
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_dashboard_title,
                    R.string.privacy_dashboard_summary,
                    "com.android.settings.privacy.PrivacyDashboardFragment",
                    R.drawable.ic_settings_privacy_filled));
            
            // Location
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.location_settings_title,
                    R.string.location_settings_loading_app_permission_stats,
                    "com.android.settings.location.LocationSettings",
                    R.drawable.ic_settings_location_filled));
            
            // Accessibility
            items.add(new BMobileExpressiveSettingsAdapter.CardItem(
                    BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.accessibility_settings_summary,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility_filled));

            rv.setAdapter(new BMobileExpressiveSettingsAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("BMobileExpressiveSettings", "Error setting up grid", e);
        }
    }
    
    private void hideSearchBar() {
        try {
            android.app.Activity activity = getActivity();
            if (activity != null) {
                android.view.View searchBar = activity.findViewById(R.id.search_action_bar);
                if (searchBar != null) {
                    searchBar.setVisibility(android.view.View.GONE);
                }
                android.view.View searchBarTwoPane = activity.findViewById(R.id.search_action_bar_two_pane);
                if (searchBarTwoPane != null) {
                    searchBarTwoPane.setVisibility(android.view.View.GONE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("BMobileExpressiveSettings", "Error hiding search bar", e);
        }
    }

    private void setupExtendedHomepageWidgetsClickListeners() {
        try {
            androidx.preference.PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            
            androidx.preference.Preference homepageWidgetsPref = screen.findPreference("extended_homepage_widgets");
            if (homepageWidgetsPref == null || !(homepageWidgetsPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp = (com.android.settingslib.widget.LayoutPreference) homepageWidgetsPref;
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            // Check if dark mode is enabled
            int nightModeFlags = activity.getResources().getConfiguration().uiMode & 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkMode = (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES);
            
            // Apply dark mode gradients to widgets if needed
            if (isDarkMode) {
                View batteryWidget = lp.findViewById(R.id.battery_widget);
                if (batteryWidget != null) {
                    batteryWidget.setBackgroundResource(R.drawable.pastel_gradient_amber_dark);
                    // Update text colors for dark mode
                    android.widget.TextView batteryTitle = lp.findViewById(R.id.battery_title);
                    if (batteryTitle != null) {
                        batteryTitle.setTextColor(activity.getResources().getColor(android.R.color.white, null));
                    }
                    android.widget.TextView batteryPercent = lp.findViewById(R.id.battery_percent);
                    if (batteryPercent != null) {
                        batteryPercent.setTextColor(activity.getResources().getColor(android.R.color.white, null));
                    }
                    android.widget.TextView batteryStatus = lp.findViewById(R.id.battery_status);
                    if (batteryStatus != null) {
                        batteryStatus.setTextColor(activity.getResources().getColor(android.R.color.white, null));
                    }
                }
                
                View storageWidget = lp.findViewById(R.id.storage_widget);
                if (storageWidget != null) {
                    storageWidget.setBackgroundResource(R.drawable.pastel_gradient_blue_dark);
                    android.widget.TextView storageTitle = lp.findViewById(R.id.storage_title);
                    if (storageTitle != null) {
                        storageTitle.setTextColor(activity.getResources().getColor(android.R.color.white, null));
                    }
                }
                
                View searchWidget = lp.findViewById(R.id.search_widget);
                if (searchWidget != null) {
                    searchWidget.setBackgroundResource(R.drawable.pastel_gradient_green_dark);
                    android.widget.ImageView searchIcon = lp.findViewById(R.id.search_widget_icon);
                    if (searchIcon != null) {
                        searchIcon.setColorFilter(activity.getResources().getColor(android.R.color.white, null),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
                
                View systemWidget = lp.findViewById(R.id.system_widget);
                if (systemWidget != null) {
                    systemWidget.setBackgroundResource(R.drawable.pastel_gradient_purple_dark);
                    android.widget.ImageView systemIcon = lp.findViewById(R.id.system_widget_icon);
                    if (systemIcon != null) {
                        systemIcon.setColorFilter(activity.getResources().getColor(android.R.color.white, null),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
            }
            
            // Setup click listeners
            try {
                View batteryWidget = lp.findViewById(R.id.battery_widget);
                if (batteryWidget != null) {
                    batteryWidget.setClickable(true);
                    batteryWidget.setFocusable(true);
                    batteryWidget.setEnabled(true);
                    batteryWidget.setOnClickListener(null);
                    batteryWidget.setOnClickListener(v -> {
                        if (!v.isEnabled()) return;
                        v.setEnabled(false);
                        try {
                            new com.android.settings.core.SubSettingLauncher(activity)
                                    .setDestination("com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
                                    .setTitleRes(R.string.power_usage_summary_title)
                                    .setSourceMetricsCategory(getMetricsCategory())
                                    .launch();
                        } catch (Exception e) {
                            android.util.Log.e("BMobileExpressiveSettings", "Error launching battery", e);
                        } finally {
                            v.postDelayed(() -> v.setEnabled(true), 500);
                        }
                    });
                }
                
                View storageWidget = lp.findViewById(R.id.storage_widget);
                if (storageWidget != null) {
                    // Remove all existing listeners
                    storageWidget.setOnClickListener(null);
                    // Make sure all child views don't intercept clicks
                    if (storageWidget instanceof android.view.ViewGroup) {
                        android.view.ViewGroup group = (android.view.ViewGroup) storageWidget;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            child.setClickable(false);
                            child.setFocusable(false);
                        }
                    }
                    storageWidget.setClickable(true);
                    storageWidget.setFocusable(true);
                    storageWidget.setEnabled(true);
                    storageWidget.setOnClickListener(v -> {
                        try {
                            com.android.settings.core.SubSettingLauncher launcher = 
                                    new com.android.settings.core.SubSettingLauncher(activity);
                            launcher.setDestination("com.android.settings.deviceinfo.StorageDashboardFragment")
                                    .setTitleRes(R.string.storage_settings)
                                    .setSourceMetricsCategory(getMetricsCategory());
                            launcher.launch();
                        } catch (android.content.ActivityNotFoundException e) {
                            android.util.Log.e("BMobileExpressiveSettings", "Storage fragment not found", e);
                            try {
                                android.content.Intent intent = new android.content.Intent(
                                        android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                                activity.startActivity(intent);
                            } catch (Exception e2) {
                                android.util.Log.e("BMobileExpressiveSettings", "Error opening storage via intent", e2);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("BMobileExpressiveSettings", "Error launching storage", e);
                        }
                    });
                }
                
                View searchWidget = lp.findViewById(R.id.search_widget);
                if (searchWidget != null) {
                    // Remove all existing listeners
                    searchWidget.setOnClickListener(null);
                    // Make sure all child views don't intercept clicks
                    if (searchWidget instanceof android.view.ViewGroup) {
                        android.view.ViewGroup group = (android.view.ViewGroup) searchWidget;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            child.setClickable(false);
                            child.setFocusable(false);
                        }
                    }
                    searchWidget.setClickable(true);
                    searchWidget.setFocusable(true);
                    searchWidget.setEnabled(true);
                    searchWidget.setOnClickListener(v -> {
                        try {
                            android.content.Intent searchIntent = new android.content.Intent();
                            searchIntent.setAction(android.app.SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
                            searchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(searchIntent);
                        } catch (Exception e) {
                            android.util.Log.e("BMobileExpressiveSettings", "Error opening search", e);
                        }
                    });
                }
                
                View systemWidget = lp.findViewById(R.id.system_widget);
                if (systemWidget != null) {
                    // Remove all existing listeners
                    systemWidget.setOnClickListener(null);
                    // Make sure all child views don't intercept clicks
                    if (systemWidget instanceof android.view.ViewGroup) {
                        android.view.ViewGroup group = (android.view.ViewGroup) systemWidget;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            child.setClickable(false);
                            child.setFocusable(false);
                        }
                    }
                    systemWidget.setClickable(true);
                    systemWidget.setFocusable(true);
                    systemWidget.setEnabled(true);
                    systemWidget.setOnClickListener(v -> {
                        try {
                            new com.android.settings.core.SubSettingLauncher(activity)
                                    .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                                    .setTitleRes(R.string.connected_devices_dashboard_title)
                                    .setSourceMetricsCategory(getMetricsCategory())
                                    .launch();
                        } catch (Exception e) {
                            android.util.Log.e("BMobileExpressiveSettings", "Error launching connected devices", e);
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("BMobileExpressiveSettings", "Error setting up extended homepage widgets click listeners", e);
            }
        } catch (Exception e) {
            android.util.Log.e("BMobileExpressiveSettings", "Error in setupExtendedHomepageWidgetsClickListeners", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

