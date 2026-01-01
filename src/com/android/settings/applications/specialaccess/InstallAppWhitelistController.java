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

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.UserHandle;
import android.os.UserManager;
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
        // Default to enabled (1) for security - only allow whitelisted stores initially
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY,
                isChecked ? 1 : 0);
        if (result) {
            if (isChecked) {
                // When enabling whitelist:
                // Revoke permissions for all non-whitelisted apps
                // Note: We don't set DISALLOW_INSTALL_UNKNOWN_SOURCES restriction because
                // it would block ALL apps including whitelisted ones. Instead, we rely on
                // AppOps permission revocation which blocks non-whitelisted apps while
                // allowing whitelisted apps (Aurora Store, Google Play) to install.
                enforceWhitelist();
                Log.d("InstallAppWhitelist", "Whitelist enabled - permissions revoked for non-whitelisted apps");
            } else {
                // When disabling whitelist, allow all apps to install
                Log.d("InstallAppWhitelist", "Whitelist disabled - all apps can install");
            }
        }
        return result;
    }

    /**
     * Enforce the whitelist by revoking permissions for all non-whitelisted apps
     * and ensuring whitelisted apps have permission.
     * This is called when the whitelist toggle is enabled.
     */
    private void enforceWhitelist() {
        enforceWhitelistInternal();
    }
    
    /**
     * Internal method to enforce the whitelist.
     * Made package-private to allow calling from initializeIfNeeded.
     */
    void enforceWhitelistInternal() {
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
                // Check if package is whitelisted
                boolean isWhitelisted = false;
                for (String allowedPackage : allowedPackages) {
                    if (TextUtils.equals(packageName, allowedPackage)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                
                // Check if app has the permission
                final int mode = appOpsManager.checkOpNoThrow(
                        AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                        appInfo.uid,
                        packageName);

                if (isWhitelisted) {
                    // Ensure whitelisted apps have permission to install
                    if (mode != AppOpsManager.MODE_ALLOWED) {
                        // Check if app has REQUEST_INSTALL_PACKAGES permission
                        if (packageManager.checkPermission(
                                android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
                                packageName) == PackageManager.PERMISSION_GRANTED) {
                            appOpsManager.setMode(
                                    AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                                    appInfo.uid,
                                    packageName,
                                    AppOpsManager.MODE_ALLOWED);
                            Log.d("InstallAppWhitelist", "Granted install permission for whitelisted app: " + packageName);
                        }
                    }
                } else {
                    // Revoke permission for non-whitelisted apps
                    if (mode == AppOpsManager.MODE_ALLOWED) {
                        appOpsManager.setMode(
                                AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                                appInfo.uid,
                                packageName,
                                AppOpsManager.MODE_ERRORED);
                        Log.d("InstallAppWhitelist", "Revoked install permission for: " + packageName);
                    }
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
     * Defaults to enabled (1) for security.
     */
    public static boolean isWhitelistEnabled(Context context) {
        if (context == null) {
            return false;
        }
        // Default to enabled (1) - only allow whitelisted stores initially
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 1) != 0;
    }
    
    /**
     * Initialize the whitelist restriction on first boot or when not set.
     * This ensures the restriction is enabled by default.
     */
    public static void initializeIfNeeded(Context context) {
        if (context == null) {
            return;
        }
        // Check if setting exists by trying to get it with a sentinel value
        // If the key doesn't exist, getInt returns the default (-1)
        final int currentValue = Settings.Secure.getInt(context.getContentResolver(), 
                SETTINGS_KEY, -1);
        if (currentValue == -1) {
            // Setting not set yet, initialize to enabled (1) by default
            Settings.Secure.putInt(context.getContentResolver(), SETTINGS_KEY, 1);
            // Enforce whitelist on first boot - use a valid preference key
            final InstallAppWhitelistController controller = 
                    new InstallAppWhitelistController(context, "install_app_whitelist_toggle");
            controller.enforceWhitelistInternal();
            Log.d("InstallAppWhitelist", "Initialized whitelist restriction to enabled by default");
        }
    }
}

