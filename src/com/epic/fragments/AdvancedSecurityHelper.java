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

package com.epic.fragments;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for Advanced Security Settings.
 * Provides utility methods for checking security feature states.
 * Note: Most features are managed by their respective controllers,
 * this helper provides convenience methods for status checking.
 */
public class AdvancedSecurityHelper {
    private static final String TAG = "AdvancedSecurityHelper";

    /**
     * Check if USB popup is blocked.
     * Delegates to BlockUsbPopupController.
     */
    public static boolean isUsbPopupBlocked(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return com.android.settings.applications.specialaccess.BlockUsbPopupController.isBlocked(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check USB popup block status", e);
            return false;
        }
    }

    /**
     * Check if install app whitelist is enabled.
     * Delegates to InstallAppWhitelistController.
     */
    public static boolean isInstallAppWhitelistEnabled(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return com.android.settings.applications.specialaccess.InstallAppWhitelistController.isWhitelistEnabled(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check install app whitelist status", e);
            return false;
        }
    }

    /**
     * Get overall security status summary.
     */
    public static String getSecurityStatusSummary(Context context) {
        if (context == null) {
            return "Unknown";
        }
        int enabledCount = 0;
        int totalCount = 0;

        if (isUsbPopupBlocked(context)) enabledCount++;
        totalCount++;

        if (isInstallAppWhitelistEnabled(context)) enabledCount++;
        totalCount++;

        return enabledCount + " of " + totalCount + " features enabled";
    }
}

