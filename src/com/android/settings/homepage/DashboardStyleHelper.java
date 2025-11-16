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
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for managing dashboard style settings.
 * Based on Axion 15 and AOSP patterns for preference screen management.
 */
public class DashboardStyleHelper {
    private static final String TAG = "DashboardStyleHelper";
    private static final int DEFAULT_STYLE = 7; // BMobile Home Settings (Fun Display style)

    /**
     * Reads the current dashboard style from Settings.
     * Always reads fresh value to ensure accuracy.
     *
     * @param context Context to access ContentResolver
     * @return Dashboard style value (0=AOSP, 1=Epic, 2=V2)
     */
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

            int style = Settings.System.getIntForUser(resolver,
                    Settings.System.SETTINGS_DASHBOARD_STYLE,
                    DEFAULT_STYLE,
                    UserHandle.USER_CURRENT);
            Log.d(TAG, "Read dashboard style: " + style);
            return style;
        } catch (Exception e) {
            Log.e(TAG, "Error reading dashboard style", e);
            return DEFAULT_STYLE;
        }
    }

    /**
     * Gets the preference screen resource ID based on dashboard style.
     * Based on AOSP DashboardFragment pattern.
     *
     * @param context Context to read dashboard style
     * @return Resource ID of the preference screen XML
     */
    public static int getPreferenceScreenResId(Context context) {
        int style = getDashboardStyle(context);
        return getPreferenceScreenResIdForStyle(style);
    }

    /**
     * Gets the preference screen resource ID for a specific style.
     *
     * @param style Dashboard style (0=Standard/AOSP, 1=Clean/Epic, 2=V2, 3=Compact, 4=Work/Material, 5=KidSecure/Classic, 7=Fun Display, 8=BMobile Expressive)
     * @return Resource ID of the preference screen XML
     */
    public static int getPreferenceScreenResIdForStyle(int style) {
        switch (style) {
            case 0:
                Log.d(TAG, "Returning Standard (AOSP) style XML");
                return com.android.settings.R.xml.top_level_settings;
            case 1:
                Log.d(TAG, "Returning Clean (Epic) style XML");
                return com.android.settings.R.xml.top_level_settings_epic;
            case 2:
                Log.d(TAG, "Returning V2 style XML");
                return com.android.settings.R.xml.top_level_settings_v2;
            case 3:
                Log.d(TAG, "Returning Compact style XML");
                return com.android.settings.R.xml.top_level_settings_compact;
            case 4:
                Log.d(TAG, "Returning Work (Material) style XML");
                return com.android.settings.R.xml.top_level_settings_material;
            case 5:
                Log.d(TAG, "Returning KidSecure (Classic) style XML");
                return com.android.settings.R.xml.top_level_settings_classic;
            case 7:
                Log.d(TAG, "Returning Fun Display style XML");
                return com.android.settings.R.xml.top_level_settings_fun_display;
            case 8:
                Log.d(TAG, "Returning BMobile Expressive style XML");
                return com.android.settings.R.xml.top_level_settings_bmobile_expressive;
            default:
                Log.d(TAG, "Returning default Clean (Epic) style XML");
                return com.android.settings.R.xml.top_level_settings_epic;
        }
    }

    /**
     * Checks if dashboard style has changed.
     * Used to determine if preference screen needs to be reloaded.
     *
     * @param context Context to read current style
     * @param cachedStyle Previously cached style value
     * @return true if style has changed, false otherwise
     */
    public static boolean hasStyleChanged(Context context, int cachedStyle) {
        if (context == null) {
            return false;
        }
        int currentStyle = getDashboardStyle(context);
        boolean changed = (currentStyle != cachedStyle);
        if (changed) {
            Log.d(TAG, "Dashboard style changed: " + cachedStyle + " -> " + currentStyle);
        }
        return changed;
    }

    /**
     * Validates dashboard style value.
     *
     * @param style Style value to validate
     * @return true if valid (0-5, 7-8), false otherwise
     */
    public static boolean isValidStyle(int style) {
        return (style >= 0 && style <= 5) || style == 7 || style == 8;
    }
}

