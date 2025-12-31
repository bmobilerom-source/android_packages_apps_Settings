/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for contextual colors (Monet adaptive colors based on content)
 */
public class ContextualColorsSettings extends DashboardFragment {

    private static final String TAG = "ContextualColorsSettings";

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.contextual_colors_settings;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    public int getHelpResource() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateLastNotificationInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLastNotificationInfo();
    }

    private void updateLastNotificationInfo() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            Preference lastNotificationPref = screen.findPreference("contextual_last_notification");
            Preference resetColorsPref = screen.findPreference("contextual_reset_colors");

            if (lastNotificationPref != null) {
                String lastNotification = android.provider.Settings.System.getString(
                        getContext().getContentResolver(), "monet_last_notification");
                if (lastNotification != null && !lastNotification.isEmpty()) {
                    lastNotificationPref.setSummary("Last adapted from: " + lastNotification);
                } else {
                    lastNotificationPref.setSummary("No notifications processed yet");
                }
            }

            if (resetColorsPref != null) {
                resetColorsPref.setOnPreferenceClickListener(preference -> {
                    // Reset contextual colors
                    android.provider.Settings.System.putString(
                            getContext().getContentResolver(), "monet_contextual_colors", null);
                    android.provider.Settings.System.putString(
                            getContext().getContentResolver(), "monet_last_notification", null);
                    updateLastNotificationInfo();
                    return true;
                });
            }
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Add contextual colors controller
        try {
            controllers.add(new com.android.settings.display.ContextualColorsController(context, "monet_contextual_enabled"));
        } catch (Exception e) {
            // Controller not available, continue without it
            android.util.Log.e("ContextualColorsSettings", "Failed to add ContextualColorsController", e);
        }

        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.contextual_colors_settings);
}
