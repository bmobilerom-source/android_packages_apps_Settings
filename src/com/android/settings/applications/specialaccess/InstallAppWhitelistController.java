/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.settings.applications.specialaccess;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

import java.util.List;

/**
 * Controller for toggling the install app whitelist restriction.
 * When enabled, only whitelisted apps (Aurora Store, Google Play) can install apps.
 * When disabled, all apps can install apps (default behavior).
 */
public class InstallAppWhitelistController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "install_app_whitelist_enabled";

    public InstallAppWhitelistController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Always available - can be shown in anatolia.xml or special_access.xml
        // The visibility is controlled by settings:isPreferenceVisible in XML
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY,
                isChecked ? 1 : 0);
        if (result && isChecked) {
            // When enabling whitelist, revoke permissions for all non-whitelisted apps
            enforceWhitelist();
        }
        return result;
    }

    /**
     * Enforce the whitelist by revoking permissions for all non-whitelisted apps.
     * This is called when the whitelist toggle is enabled.
     */
    private void enforceWhitelist() {
        try {
            final AppOpsManager appOpsManager = mContext.getSystemService(AppOpsManager.class);
            final PackageManager packageManager = mContext.getPackageManager();
            final Resources res = mContext.getResources();
            final String[] allowedPackages = res.getStringArray(
                    R.array.config_allowed_install_app_packages);

            if (allowedPackages == null || allowedPackages.length == 0) {
                return; // No whitelist defined
            }

            // Get all installed packages
            final List<ApplicationInfo> apps = packageManager.getInstalledApplications(
                    PackageManager.MATCH_ALL);

            for (ApplicationInfo appInfo : apps) {
                final String packageName = appInfo.packageName;
                // Skip whitelisted packages
                boolean isWhitelisted = false;
                for (String allowedPackage : allowedPackages) {
                    if (TextUtils.equals(packageName, allowedPackage)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                if (isWhitelisted) {
                    continue; // Keep permission for whitelisted apps
                }

                // Check if app has the permission
                final int mode = appOpsManager.checkOpNoThrow(
                        AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                        appInfo.uid,
                        packageName);

                // If app has permission (MODE_ALLOWED), revoke it
                if (mode == AppOpsManager.MODE_ALLOWED) {
                    appOpsManager.setMode(
                            AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                            appInfo.uid,
                            packageName,
                            AppOpsManager.MODE_ERRORED);
                    Log.d("InstallAppWhitelist", "Revoked install permission for: " + packageName);
                }
            }
        } catch (Exception e) {
            Log.e("InstallAppWhitelist", "Error enforcing whitelist", e);
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_apps;
    }

    /**
     * Check if the whitelist restriction is enabled.
     * This is a static method that can be called from other classes.
     */
    public static boolean isWhitelistEnabled(Context context) {
        if (context == null) {
            return false;
        }
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }
}

