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
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.SearchFeatureProvider;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

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
        try {
            // Always bind search widget click handler to ensure it works on top-level dashboard
            // Other widgets are bound conditionally based on preference context
            bindSearchWidget();
            // Only bind other actions if not used within a preference (which handles its own click listeners)
            if (!isInPreference()) {
                bindOtherActions();
            }
        } catch (Exception e) {
            // Silently handle exceptions to prevent crashes during view inflation
            android.util.Log.w("HomepageWidgetsView", "Failed to initialize widgets", e);
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

    private void bindSearchWidget() {
        // Always bind search widget to ensure it works on top-level dashboard
        try {
            Activity activity = findActivity(getContext());
            View search = findViewById(R.id.search_widget);
            if (search != null) {
                setInteractiveClick(search, () -> launchSettingsSearch(activity));
            }
        } catch (Exception e) {
            // Silently handle exceptions to prevent crashes during initialization
            android.util.Log.w("HomepageWidgetsView", "Failed to bind search widget", e);
        }
    }

    private void bindOtherActions() {
        try {
            Activity activity = findActivity(getContext());
            View battery = findViewById(R.id.battery_widget);
            View storage = findViewById(R.id.storage_widget);
            View system = findViewById(R.id.system_widget);

            setInteractiveClick(battery, () -> openBattery(activity));
            setInteractiveClick(storage, () -> openStorage(activity));
            setInteractiveClick(system, () -> openConnectedDevices(activity));
        } catch (Exception e) {
            // Silently handle exceptions to prevent crashes during initialization
            android.util.Log.w("HomepageWidgetsView", "Failed to bind other actions", e);
        }
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

    /** Opens Settings search (Settings Intelligence). */
    public static void launchSettingsSearch(@Nullable Activity activity) {
        if (activity == null) {
            return;
        }
        try {
            final Context context = activity.getApplicationContext();
            final SearchFeatureProvider provider =
                    FeatureFactory.getFeatureFactory().getSearchFeatureProvider();
            final Intent intent = provider.buildSearchIntent(context, SettingsEnums.SETTINGS_HOMEPAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            final PackageManager pm = activity.getPackageManager();
            final java.util.List<ResolveInfo> resolveInfos =
                    pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (!resolveInfos.isEmpty()) {
                final ComponentName component = resolveInfos.get(0).getComponentInfo().getComponentName();
                intent.setComponent(component);
                activity.startActivity(intent);
                return;
            }
            activity.startActivity(new Intent(Settings.ACTION_APP_SEARCH_SETTINGS));
        } catch (Exception e) {
            android.util.Log.w("HomepageWidgetsView", "Failed to launch Settings search", e);
            try {
                activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) { }
        }
    }

    /** @deprecated Use {@link #launchSettingsSearch(Activity)} */
    @Deprecated
    public static void launchSystemLaunchPad(@Nullable Activity activity) {
        launchSettingsSearch(activity);
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

