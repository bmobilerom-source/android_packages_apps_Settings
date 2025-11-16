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

package com.android.settings.homepage;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Helper class for managing dashboard style settings.
 */
public class DashboardStyleHelper {
    private static final String TAG = "DashboardStyleHelper";
    private static final int DEFAULT_STYLE = 7; // BMobile Home Settings (Fun Display)

    public static int getDefaultStyle() {
        return DEFAULT_STYLE;
    }

    public static int getDashboardStyle(Context context) {
        if (context == null) {
            Log.w(TAG, "Context is null, returning default style");
            return DEFAULT_STYLE;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                Log.w(TAG, "ContentResolver is null, returning default style");
                return DEFAULT_STYLE;
            }

            int style = DashboardSystemKeys.getDashboardStyle(resolver, DEFAULT_STYLE);
            if (!isValidStyle(style)) {
                Log.w(TAG, "Invalid stored style " + style + ", using default");
                return DEFAULT_STYLE;
            }
            Log.d(TAG, "Read dashboard style: " + style);
            return style;
        } catch (Exception e) {
            Log.e(TAG, "Error reading dashboard style", e);
            return DEFAULT_STYLE;
        }
    }

    public static int getPreferenceScreenResId(Context context) {
        return getPreferenceScreenResIdForStyle(getDashboardStyle(context));
    }

    /**
     * Maps {@code settings_dashboard_style} id → homepage XML.
     * Picker strings: {@code dashboard_style_top_level_*} in system_basic_strings.xml
     * (name matches XML basename). Removed ids: 3 Compact, 9 Security Extended.
     */
    public static int getPreferenceScreenResIdForStyle(int style) {
        switch (style) {
            case 0: // dashboard_style_top_level_settings — NOT aosp file
                return com.android.settings.R.xml.top_level_settings;
            case 1: // dashboard_style_top_level_epic
                return com.android.settings.R.xml.top_level_settings_epic;
            case 2: // dashboard_style_top_level_v2
                return com.android.settings.R.xml.top_level_settings_v2;
            case 4: // dashboard_style_top_level_material
                return com.android.settings.R.xml.top_level_settings_material;
            case 5: // dashboard_style_top_level_classic
                return com.android.settings.R.xml.top_level_settings_classic;
            case 6: // dashboard_style_top_level_custom_v2
                return com.android.settings.R.xml.top_level_settings_custom_v2;
            case 7: // dashboard_style_top_level_fun_display (default)
                return com.android.settings.R.xml.top_level_settings_fun_display;
            case 8: // dashboard_style_top_level_bmobile_expressive
                return com.android.settings.R.xml.top_level_settings_bmobile_expressive;
            case 10: // dashboard_style_top_level_aosp → top_level_settings_aosp.xml
                return com.android.settings.R.xml.top_level_settings_aosp;
            case 11: // dashboard_style_top_level_oos11
                return com.android.settings.R.xml.top_level_settings_oos11;
            case 12: // dashboard_style_top_level_afterlabs_tab
                return com.android.settings.R.xml.top_level_settings_afterlabs_tab;
            case 13: // dashboard_style_top_level_afterlabs_grid
                return com.android.settings.R.xml.top_level_settings_afterlabs_grid;
            case 14: // dashboard_style_top_level_yr_expressive
                return com.android.settings.R.xml.top_level_settings_yr_expressive;
            case 15: // dashboard_style_top_level_bmobile_neo
                return com.android.settings.R.xml.top_level_settings_bmobile_neo;
            case 16: // dashboard_style_top_level_ks_fun
                return com.android.settings.R.xml.top_level_settings_ks_fun;
            default:
                Log.w(TAG, "Unknown or removed style " + style + ", using default");
                return com.android.settings.R.xml.top_level_settings_fun_display;
        }
    }

    public static boolean hasStyleChanged(Context context, int cachedStyle) {
        if (context == null) {
            return false;
        }
        return getDashboardStyle(context) != cachedStyle;
    }

    public static boolean isValidStyle(int style) {
        if (style == 3 || style == 9) {
            return false;
        }
        return (style >= 0 && style <= 8) || (style >= 10 && style <= 16);
    }

    /**
     * Resolves the user-visible label for a dashboard style id.
     */
    @NonNull
    public static String getStyleLabel(@NonNull Context context, int style) {
        String[] values = context.getResources()
                .getStringArray(com.android.settings.R.array.settings_dashboard_style_values);
        String[] entries = context.getResources()
                .getStringArray(com.android.settings.R.array.settings_dashboard_style_entries);
        for (int i = 0; i < values.length && i < entries.length; i++) {
            try {
                if (Integer.parseInt(values[i]) == style) {
                    return entries[i];
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return context.getString(com.android.settings.R.string.dashboard_style_summary);
    }
}
