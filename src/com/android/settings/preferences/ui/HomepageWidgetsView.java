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

package com.android.settings.preferences.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.ComponentName;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

/**
 * Self-contained widget container that wires click actions for battery, storage,
 * search, and connected devices. Drop this layout anywhere; no fragment wiring needed.
 */
public class HomepageWidgetsView extends LinearLayout {

    public HomepageWidgetsView(Context context) {
        super(context);
    }

    public HomepageWidgetsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomepageWidgetsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Only bind actions if not used within a preference (which handles its own click listeners)
        if (!isInPreference()) {
            bindActions();
        }
    }

    private boolean isInPreference() {
        // Check if this view is inside a Preference by walking up the view hierarchy
        android.view.ViewParent parent = getParent();
        while (parent != null) {
            if (parent.getClass().getName().contains("Preference")) {
                return true;
            }
            if (parent instanceof android.view.View) {
                parent = ((android.view.View) parent).getParent();
            } else {
                break;
            }
        }
        return false;
    }

    private void bindActions() {
        Activity activity = findActivity(getContext());
        View battery = findViewById(R.id.battery_widget);
        View storage = findViewById(R.id.storage_widget);
        View search = findViewById(R.id.search_widget);
        View system = findViewById(R.id.system_widget);

        setInteractiveClick(battery, () -> openBattery(activity));
        setInteractiveClick(storage, () -> openStorage(activity));
        setInteractiveClick(search, () -> launchSystemLaunchPad(activity));
        setInteractiveClick(system, () -> openConnectedDevices(activity));
    }

    private void setInteractiveClick(View v, Runnable action) {
        if (v == null || action == null) return;
        v.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(false);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> action.run());
    }

    private void openBattery(Activity activity) {
        // Try stock battery activity first (works for system/priv apps), then fallback fragment.
        try {
            Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            (activity != null ? activity : getContext()).startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new com.android.settings.core.SubSettingLauncher(activity)
                        .setDestination("com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
                        .setTitleRes(R.string.power_usage_summary_title)
                        .setSourceMetricsCategory(com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }

    private void openStorage(Activity activity) {
        // Use exported storage settings activity first to avoid fragment crashes without privilege.
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            (activity != null ? activity : getContext()).startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new com.android.settings.core.SubSettingLauncher(activity)
                        .setDestination("com.android.settings.deviceinfo.StorageDashboardFragment")
                        .setTitleRes(R.string.storage_settings)
                        .setSourceMetricsCategory(com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }

    private void openConnectedDevices(Activity activity) {
        // Prefer exported BT settings for faster, one-tap open; fallback to fragment.
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            (activity != null ? activity : getContext()).startActivity(intent);
            return;
        } catch (Exception ignored) { }

        if (activity != null) {
            try {
                new com.android.settings.core.SubSettingLauncher(activity)
                        .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                        .setTitleRes(R.string.connected_devices_dashboard_title)
                        .setSourceMetricsCategory(com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .launch();
                return;
            } catch (Exception ignored) { }
        }
    }

    private void launchSystemLaunchPad(Activity activity) {
        if (activity == null) return;
        // Try System Launch Pad app first
        Intent launchPadIntent = new Intent(Intent.ACTION_MAIN);
        launchPadIntent.setClassName("com.devrinth.launchpad",
                "com.devrinth.launchpad.activities.LaunchpadOverlayActivity");
        launchPadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            if (activity != null) {
                activity.startActivity(launchPadIntent);
            } else {
                getContext().startActivity(launchPadIntent);
            }
            return;
        } catch (Exception ignored) { }

        // Fallback to Settings search, then main Settings
        try {
            Intent intent = new Intent(Settings.ACTION_APP_SEARCH_SETTINGS);
            if (activity != null) {
                activity.startActivity(intent);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                if (activity != null) {
                    activity.startActivity(intent);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            } catch (Exception ignored) { }
        }
    }

    private Activity findActivity(Context context) {
        Context ctx = context;
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                return (Activity) ctx;
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return null;
    }
}

