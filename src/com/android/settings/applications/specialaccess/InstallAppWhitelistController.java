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
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

import java.util.List;

/**
 * Controller for toggling the install app whitelist restriction.
 * When enabled: Downloads are allowed, but only whitelisted apps (Aurora Store, Google Play) can install apps.
 * When disabled: All apps can download and install apps (default behavior).
 */
public class InstallAppWhitelistController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "install_app_whitelist_enabled";

    public InstallAppWhitelistController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        initializeIfNeeded(mContext);
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
                // When enabling whitelist: Allow downloads but block installations from non-whitelisted apps
                // Don't set global user restrictions - allow downloads, block at install time
                enforceWhitelist();
                Log.d("InstallAppWhitelist", "Whitelist enabled - downloads allowed, installations blocked for non-whitelisted apps");
            } else {
                // When disabling whitelist: Allow everything
                Log.d("InstallAppWhitelist", "Whitelist disabled - all downloads and installations allowed");
            }
        }
        return result;
    }

    /**
     * Note: We don't use global user restrictions anymore.
     * Downloads are allowed, but installations are blocked at PackageInstaller level
     * for non-whitelisted apps. This allows APK downloads while preventing installs.
     */

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
     * With strict global restriction enabled, this ensures whitelisted apps can still install.
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
                Log.w("InstallAppWhitelist", "No allowed packages configured");
                return;
            }

            Log.d("InstallAppWhitelist", "Enforcing whitelist for " + allowedPackages.length + " allowed packages");

            // Ensure whitelisted apps have installation permissions
            for (String allowedPackage : allowedPackages) {
                try {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(allowedPackage,
                            PackageManager.MATCH_ALL);
                    if (appInfo != null) {
                        // Grant installation permission to whitelisted apps
                        appOpsManager.setMode(
                                AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                                appInfo.uid,
                                allowedPackage,
                                AppOpsManager.MODE_ALLOWED);
                        Log.d("InstallAppWhitelist", "Granted install permission for whitelisted app: " + allowedPackage);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Package not installed, skip
                    Log.d("InstallAppWhitelist", "Whitelisted package not installed: " + allowedPackage);
                }
            }

            // Get all installed packages and revoke permissions for non-whitelisted ones
            final List<ApplicationInfo> apps = packageManager.getInstalledApplications(
                    PackageManager.MATCH_ALL);

            int revokedCount = 0;
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

                if (!isWhitelisted) {
                    // Revoke permission for non-whitelisted apps
                    final int mode = appOpsManager.checkOpNoThrow(
                            AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                            appInfo.uid,
                            packageName);

                    if (mode == AppOpsManager.MODE_ALLOWED) {
                        appOpsManager.setMode(
                                AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                                appInfo.uid,
                                packageName,
                                AppOpsManager.MODE_ERRORED);
                        revokedCount++;
                    }
                }
            }

            Log.d("InstallAppWhitelist", "Whitelist enforced - revoked permissions for " + revokedCount + " non-whitelisted apps");

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
     * Returns whether {@code packageName} is in {@link R.array#config_allowed_install_app_packages}.
     *
     * <p>When the whitelist restriction is enabled, only these packages should be able to request
     * install (unknown sources) permission.</p>
     */
    public static boolean isPackageWhitelisted(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        final String[] allowedPackages = context.getResources().getStringArray(
                R.array.config_allowed_install_app_packages);
        if (allowedPackages == null || allowedPackages.length == 0) {
            return false;
        }
        for (String allowedPackage : allowedPackages) {
            if (TextUtils.equals(packageName, allowedPackage)) {
                return true;
            }
        }
        return false;
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
            // Enforce whitelist on first boot (downloads allowed, installs blocked)
            final InstallAppWhitelistController controller =
                    new InstallAppWhitelistController(context, "install_app_whitelist_toggle");
            controller.enforceWhitelistInternal();
            Log.d("InstallAppWhitelist", "Initialized whitelist restriction to enabled by default");
        }
    }
}

