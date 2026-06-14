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

package com.android.settings.preferences.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

public class HomepageWidgetsPreference extends Preference {

    public HomepageWidgetsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.homepage_widgets);
        setSelectable(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Bind click actions for homepage widgets
        View battery = holder.findViewById(R.id.battery_widget);
        View storage = holder.findViewById(R.id.storage_widget);
        View search = holder.findViewById(R.id.search_widget);
        View system = holder.findViewById(R.id.system_widget);

        bindWidget(battery, () -> openBattery());
        bindWidget(storage, () -> openStorage());
        bindWidget(search, () -> HomepageWidgetsView.launchSettingsSearch(findActivity(getContext())));
        bindWidget(system, () -> openConnectedDevices());
    }

    private void bindWidget(View view, Runnable action) {
        if (view == null) return;
        view.setClickable(true);
        view.setFocusable(true);
        view.setHapticFeedbackEnabled(true);
        view.setOnClickListener(v -> action.run());
    }

    private void openBattery() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;

        // Try stock battery activity first (works for system/priv apps), then fallback fragment.
        try {
            Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new SubSettingLauncher(activity)
                        .setDestination("com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
                        .setTitleRes(R.string.power_usage_summary_title)
                        .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }

    private void openStorage() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;

        // Use exported storage settings activity first to avoid fragment crashes without privilege.
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new SubSettingLauncher(activity)
                        .setDestination("com.android.settings.deviceinfo.StorageDashboardFragment")
                        .setTitleRes(R.string.storage_settings)
                        .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }

    private void openConnectedDevices() {
        Activity activity = findActivity(getContext());
        if (activity == null) return;

        // Prefer exported BT settings for faster, one-tap open; fallback to fragment.
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new SubSettingLauncher(activity)
                        .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                        .setTitleRes(R.string.connected_devices_dashboard_title)
                        .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }
    private Activity findActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}

