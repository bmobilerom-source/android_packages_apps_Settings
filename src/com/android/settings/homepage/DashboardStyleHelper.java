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
     * @param style 0–8 BMobile, 9 Security Extended, 10–11 Arcana,
     *              12 AfterLabs tab+V2, 13 AfterLabs grid, 14 Infinity home
     */
    public static int getPreferenceScreenResIdForStyle(int style) {
        switch (style) {
            case 0:
                return com.android.settings.R.xml.top_level_settings;
            case 1:
                return com.android.settings.R.xml.top_level_settings_epic;
            case 2:
                return com.android.settings.R.xml.top_level_settings_v2;
            case 3:
                return com.android.settings.R.xml.top_level_settings_compact;
            case 4:
                return com.android.settings.R.xml.top_level_settings_material;
            case 5:
                return com.android.settings.R.xml.top_level_settings_classic;
            case 6:
                return com.android.settings.R.xml.top_level_settings_custom_v2;
            case 7:
                return com.android.settings.R.xml.top_level_settings_fun_display;
            case 8:
                return com.android.settings.R.xml.top_level_settings_bmobile_expressive;
            case 9:
                return com.android.settings.R.xml.top_level_settings_securityextended;
            case 10:
                return com.android.settings.R.xml.top_level_settings_aosp;
            case 11:
                return com.android.settings.R.xml.top_level_settings_oos11;
            case 12:
                return com.android.settings.R.xml.top_level_settings_afterlabs_tab;
            case 13:
                return com.android.settings.R.xml.top_level_settings_afterlabs_grid;
            case 14:
                return com.android.settings.R.xml.top_level_settings_infinity_home;
            default:
                Log.w(TAG, "Unknown style " + style + ", using default");
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
        return (style >= 0 && style <= 9) || (style >= 10 && style <= 14);
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
