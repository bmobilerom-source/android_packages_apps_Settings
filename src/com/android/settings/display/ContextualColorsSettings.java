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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.settings.SettingsEnums;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class ContextualColorsSettings extends DashboardFragment {
    private static final String TAG = "ContextualColorsSettings";

    // Contextual color mappings
    private static final Map<String, ContextualColor> CONTEXTUAL_COLORS = new HashMap<>();

    static {
        // Social media notifications
        CONTEXTUAL_COLORS.put("com.whatsapp", new ContextualColor("whatsapp",
            new int[]{Color.parseColor("#25D366"), Color.parseColor("#128C7E"),
                     Color.parseColor("#075E54"), Color.parseColor("#DCF8C6")}));

        CONTEXTUAL_COLORS.put("com.instagram.android", new ContextualColor("instagram",
            new int[]{Color.parseColor("#E1306C"), Color.parseColor("#F56040"),
                     Color.parseColor("#F77737"), Color.parseColor("#FCAF45")}));

        CONTEXTUAL_COLORS.put("com.facebook.katana", new ContextualColor("facebook",
            new int[]{Color.parseColor("#1877F2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1E88E5"), Color.parseColor("#1565C0")}));

        CONTEXTUAL_COLORS.put("com.twitter.android", new ContextualColor("twitter",
            new int[]{Color.parseColor("#1DA1F2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1976D2"), Color.parseColor("#0D47A1")}));

        // Email
        CONTEXTUAL_COLORS.put("com.google.android.gm", new ContextualColor("gmail",
            new int[]{Color.parseColor("#EA4335"), Color.parseColor("#FBBC05"),
                     Color.parseColor("#34A853"), Color.parseColor("#4285F4")}));

        // Messages
        CONTEXTUAL_COLORS.put("com.google.android.apps.messaging", new ContextualColor("messages",
            new int[]{Color.parseColor("#1A73E8"), Color.parseColor("#4285F4"),
                     Color.parseColor("#1565C0"), Color.parseColor("#0D47A1")}));

        // Music/Entertainment
        CONTEXTUAL_COLORS.put("com.spotify.music", new ContextualColor("spotify",
            new int[]{Color.parseColor("#1DB954"), Color.parseColor("#1ED760"),
                     Color.parseColor("#1DB954"), Color.parseColor("#191414")}));

        CONTEXTUAL_COLORS.put("com.google.android.youtube", new ContextualColor("youtube",
            new int[]{Color.parseColor("#FF0000"), Color.parseColor("#FF4444"),
                     Color.parseColor("#CC0000"), Color.parseColor("#990000")}));

        // System notifications
        CONTEXTUAL_COLORS.put("android", new ContextualColor("system",
            new int[]{Color.parseColor("#1976D2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1E88E5"), Color.parseColor("#1565C0")}));
    }

    private SwitchPreferenceCompat mEnablePreference;
    private NotificationListener mNotificationListener;
    private String mLastNotificationPackage = null;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.contextual_colors_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Initialize notification listener
        mNotificationListener = new NotificationListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.settings.NOTIFICATION_RECEIVED");
        getContext().registerReceiver(mNotificationListener, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNotificationListener != null) {
            getContext().unregisterReceiver(mNotificationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLastNotificationDisplay();
    }

    private void updateLastNotificationDisplay() {
        String lastPackage = android.provider.Settings.Secure.getString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_LAST_NOTIFICATION_PACKAGE);

        if (lastPackage != null && !lastPackage.isEmpty()) {
            ContextualColor color = CONTEXTUAL_COLORS.get(lastPackage);
            if (color != null) {
                // Update UI to show last notification colors
                updateColorPreview(color);
            }
        }
    }

    private void updateColorPreview(ContextualColor color) {
        // This would update the UI to show the contextual colors
        // For now, just save the preference
        String colorString = color.colors[0] + "," + color.colors[1] + "," +
                           color.colors[2] + "," + color.colors[3];
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_CONTEXTUAL_COLORS,
            colorString);
    }

    private void handleNotificationReceived(String packageName) {
        boolean enabled = android.provider.Settings.Secure.getInt(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_CONTEXTUAL_ENABLED, 0) == 1;

        if (!enabled) return;

        ContextualColor color = CONTEXTUAL_COLORS.get(packageName);
        if (color != null) {
            // Apply contextual colors
            applyContextualColors(color);

            // Save last notification package
            android.provider.Settings.Secure.putString(
                getContext().getContentResolver(),
                android.provider.Settings.Secure.MONET_LAST_NOTIFICATION_PACKAGE,
                packageName);

            mLastNotificationPackage = packageName;
            updateColorPreview(color);
        }
    }

    private void applyContextualColors(ContextualColor color) {
        // Apply the contextual colors (this would need framework implementation)
        // For now, just save preference
        String colorString = color.colors[0] + "," + color.colors[1] + "," +
                           color.colors[2] + "," + color.colors[3];
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_CONTEXTUAL_COLORS,
            colorString);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new ContextualColorsController(context, "monet_contextual_enabled"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.contextual_colors_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "contextual_colors_settings";
    }

    private class NotificationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.android.settings.NOTIFICATION_RECEIVED".equals(intent.getAction())) {
                String packageName = intent.getStringExtra("package_name");
                if (packageName != null) {
                    handleNotificationReceived(packageName);
                }
            }
        }
    }

    private static class ContextualColor {
        final String id;
        final int[] colors;

        ContextualColor(String id, int[] colors) {
            this.id = id;
            this.colors = colors;
        }
    }
}
