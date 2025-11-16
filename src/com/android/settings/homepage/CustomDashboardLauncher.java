/*
 * Copyright (C) 2025 BashaMobile
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

package com.android.settings.homepage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.epic.fragments.CustomDashboardSettings;

/**
 * Single entry point to open {@link CustomDashboardSettings} from any Settings screen,
 * grid card, external app, or deep link.
 */
public final class CustomDashboardLauncher {

    /** Broadcast when dashboard style value changes. */
    public static final String ACTION_DASHBOARD_STYLE_CHANGED =
            "com.android.settings.DASHBOARD_STYLE_CHANGED";

    /** Intent action for launcher / search / external apps. */
    public static final String ACTION_CUSTOM_DASHBOARD =
            "com.android.settings.CUSTOM_DASHBOARD_SETTINGS";

    public static final String FRAGMENT_CLASS = CustomDashboardSettings.class.getName();

    private CustomDashboardLauncher() {
    }

    /**
     * Opens Custom Dashboard using the standard SubSettings flow (works from any fragment).
     */
    public static void launch(@NonNull Context context) {
        launch(context, MetricsProto.MetricsEvent.DASHBOARD_SUMMARY);
    }

    public static void launch(@NonNull Context context, int sourceMetricsCategory) {
        if (context == null) {
            return;
        }
        new SubSettingLauncher(context)
                .setDestination(FRAGMENT_CLASS)
                .setTitleRes(R.string.custom_dashboard_title)
                .setSourceMetricsCategory(sourceMetricsCategory)
                .launch();
    }

    /**
     * Builds an intent that opens Custom Dashboard via {@link Settings.CustomDashboardActivity}.
     */
    @NonNull
    public static Intent buildIntent(@NonNull Context context) {
        Intent intent = new Intent(ACTION_CUSTOM_DASHBOARD);
        intent.setClass(context, Settings.CustomDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Starts Custom Dashboard as a top-level Settings activity (e.g. from Infinity Suite).
     */
    public static void launchAsActivity(@NonNull Context context) {
        context.startActivity(buildIntent(context));
    }

    /**
     * After changing style inside Custom Dashboard, reopen Settings home so the new layout applies.
     */
    public static void reopenSettingsHome(@Nullable Activity activity) {
        if (activity == null) {
            return;
        }
        Intent home = new Intent(activity, Settings.class);
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(home);
        activity.finish();
    }

    /**
     * Human-readable label for the current dashboard style.
     */
    @NonNull
    public static String getCurrentStyleLabel(@NonNull Context context) {
        return DashboardStyleHelper.getStyleLabel(context,
                DashboardStyleHelper.getDashboardStyle(context));
    }

    public static boolean isCustomDashboardDestination(@Nullable String fragmentName) {
        return !TextUtils.isEmpty(fragmentName)
                && (FRAGMENT_CLASS.equals(fragmentName)
                || fragmentName.contains("CustomDashboardSettings")
                || fragmentName.contains("DashboardStyleSettings"));
    }
}
