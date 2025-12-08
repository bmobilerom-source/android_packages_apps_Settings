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
        bindActions();
    }

    private void bindActions() {
        Activity activity = findActivity(getContext());
        if (activity == null) {
            return;
        }
        View battery = findViewById(R.id.battery_widget);
        View storage = findViewById(R.id.storage_widget);
        View search = findViewById(R.id.search_widget);
        View system = findViewById(R.id.system_widget);

        setInteractiveClick(battery, () -> launchSubsetting(activity,
                "com.android.settings.fuelgauge.PowerUsageSummary",
                R.string.power_usage_summary_title));
        setInteractiveClick(storage, () -> launchSubsetting(activity,
                "com.android.settings.deviceinfo.StorageDashboardFragment",
                R.string.storage_settings));
        setInteractiveClick(search, () -> launchSystemLaunchPad(activity));
        setInteractiveClick(system, () -> launchSubsetting(activity,
                "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                R.string.connected_devices_dashboard_title));
    }

    private void setInteractiveClick(View v, Runnable action) {
        if (v == null || action == null) return;
        v.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(false);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> action.run());
    }

    private void launchSystemLaunchPad(Activity activity) {
        if (activity == null) return;
        // Try System Launch Pad app first
        Intent launchPadIntent = new Intent(Intent.ACTION_MAIN);
        launchPadIntent.setClassName("com.devrinth.launchpad",
                "com.devrinth.launchpad.activities.LaunchpadOverlayActivity");
        PackageManager pm = getContext().getPackageManager();
        ResolveInfo ri = pm.resolveActivity(launchPadIntent, 0);
        if (ri != null) {
            try {
                if (activity != null) {
                    activity.startActivity(launchPadIntent);
                } else {
                    launchPadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(launchPadIntent);
                }
                return;
            } catch (Exception ignored) { }
        }

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

    private void launchSubsetting(Activity activity, String dest, int titleRes) {
        if (activity == null) return;
        try {
            new SubSettingLauncher(activity)
                    .setDestination(dest)
                    .setTitleRes(titleRes)
                    .setSourceMetricsCategory(com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();
        } catch (Exception ignored) { }
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

