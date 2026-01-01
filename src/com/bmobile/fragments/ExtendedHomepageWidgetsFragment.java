/*
 * Copyright (C) 2025 The EpicROM Project
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.widget.LayoutPreference;

public class ExtendedHomepageWidgetsFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extended_homepage_widgets_screen);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindWidgets();
    }

    private void bindWidgets() {
        Activity activity = getActivity();
        if (activity == null) return;
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) return;

        Preference pref = screen.findPreference("extended_homepage_widgets_layout");
        if (!(pref instanceof LayoutPreference)) return;

        LayoutPreference lp = (LayoutPreference) pref;
        View battery = lp.findViewById(R.id.battery_widget);
        View storage = lp.findViewById(R.id.storage_widget);
        View search = lp.findViewById(R.id.search_widget);
        View system = lp.findViewById(R.id.system_widget);

        if (battery != null) {
            battery.setOnClickListener(v -> launchSubsetting(activity,
                    "com.android.settings.fuelgauge.PowerUsageSummary",
                    R.string.power_usage_summary_title));
        }
        if (storage != null) {
            storage.setOnClickListener(v -> launchSubsetting(activity,
                    "com.android.settings.deviceinfo.StorageDashboardFragment",
                    R.string.storage_settings));
        }
        if (search != null) {
            search.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_APP_SEARCH_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    } catch (Exception ignore) { }
                }
            });
        }
        if (system != null) {
            system.setOnClickListener(v -> launchSubsetting(activity,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.string.connected_devices_title));
        }
    }

    private void launchSubsetting(Activity activity, String dest, int titleRes) {
        try {
            new SubSettingLauncher(activity)
                    .setDestination(dest)
                    .setTitleRes(titleRes)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .launch();
        } catch (Exception ignored) { }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

