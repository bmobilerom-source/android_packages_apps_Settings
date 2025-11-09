/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.settings.applications.appinfo;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY;

import android.app.AppOpsManager;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateInstallAppsBridge;
import com.android.settings.applications.AppStateInstallAppsBridge.InstallAppsState;
import com.android.settings.applications.specialaccess.InstallAppWhitelistController;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ExternalSourcesDetails extends AppInfoWithHeader
        implements OnPreferenceChangeListener {

    private static final String KEY_EXTERNAL_SOURCE_SWITCH = "external_sources_settings_switch";

    private AppStateInstallAppsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private UserManager mUserManager;
    private RestrictedSwitchPreference mSwitchPref;
    private InstallAppsState mInstallAppsState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getActivity();
        mAppBridge = new AppStateInstallAppsBridge(context, mState, null);
        mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mUserManager = UserManager.get(context);

        addPreferencesFromResource(R.xml.external_sources_details);
        mSwitchPref = (RestrictedSwitchPreference) findPreference(KEY_EXTERNAL_SOURCE_SWITCH);
        mSwitchPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppBridge.release();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean checked = (Boolean) newValue;
        if (preference == mSwitchPref) {
            // Block granting permission if package is not in whitelist
            if (checked && !isPackageAllowedToInstallApps()) {
                // Don't allow granting permission for non-whitelisted apps
                return false;
            }
            if (mInstallAppsState != null && checked != mInstallAppsState.canInstallApps()) {
                if (Settings.ManageAppExternalSourcesActivity.class.getName().equals(
                        getIntent().getComponent().getClassName())) {
                    setResult(checked ? RESULT_OK : RESULT_CANCELED);
                }
                setCanInstallApps(checked);
                refreshUi();
            }
            return true;
        }
        return false;
    }

    public static CharSequence getPreferenceSummary(Context context, AppEntry entry) {
        final UserHandle userHandle = UserHandle.getUserHandleForUid(entry.info.uid);
        final UserManager um = UserManager.get(context);
        if (android.security.Flags.aapmFeatureDisableInstallUnknownSources()) {
            if (um.hasBaseUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES, userHandle)) {
                return context.getString(com.android.settingslib.R.string.disabled);
            } else if (um.hasUserRestrictionForUser(DISALLOW_INSTALL_UNKNOWN_SOURCES, userHandle)) {
                return context.getString(
                        com.android.settingslib.widget.restricted.R.string.disabled_by_admin);
            } else if (um.hasUserRestrictionForUser(DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,
                    userHandle)) {
                if (RestrictedLockUtilsInternal.isPolicyEnforcedByAdvancedProtection(context,
                        DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, userHandle.getIdentifier())) {
                    return context.getString(com.android.settingslib.widget.restricted
                            .R.string.disabled_by_advanced_protection);
                } else {
                    return context.getString(
                            com.android.settingslib.widget.restricted.R.string.disabled_by_admin);
                }
            }
        } else {
            final int userRestrictionSource = um.getUserRestrictionSource(
                    DISALLOW_INSTALL_UNKNOWN_SOURCES, userHandle)
                    | um.getUserRestrictionSource(
                            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, userHandle);
            if ((userRestrictionSource & UserManager.RESTRICTION_SOURCE_SYSTEM) != 0) {
                return context.getString(
                        com.android.settingslib.widget.restricted.R.string.disabled_by_admin);
            } else if (userRestrictionSource != 0) {
                return context.getString(com.android.settingslib.R.string.disabled);
            }
        }
        final InstallAppsState appsState = new AppStateInstallAppsBridge(context, null, null)
                .createInstallAppsStateFor(entry.info.packageName, entry.info.uid);
        return context.getString(appsState.canInstallApps()
                ? R.string.app_permission_summary_allowed
                : R.string.app_permission_summary_not_allowed);
    }

    private void setCanInstallApps(boolean newState) {
        // Block granting permission if whitelist is enabled and package is not whitelisted
        if (newState && InstallAppWhitelistController.isWhitelistEnabled(getActivity())) {
            if (!isPackageAllowedToInstallApps()) {
                // Don't grant permission for non-whitelisted apps
                return;
            }
        }
        mAppOpsManager.setMode(AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                mPackageInfo.applicationInfo.uid, mPackageName,
                newState ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
    }

    @Override
    protected boolean refreshUi() {
        if (mPackageInfo == null || mPackageInfo.applicationInfo == null) {
            return false;
        }
        if (mUserManager.hasBaseUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserHandle.of(UserHandle.myUserId()))) {
            mSwitchPref.setChecked(false);
            mSwitchPref.setSummary(com.android.settingslib.R.string.disabled);
            mSwitchPref.setEnabled(false);
            return true;
        }
        mSwitchPref.checkRestrictionAndSetDisabled(DISALLOW_INSTALL_UNKNOWN_SOURCES);
        if (!mSwitchPref.isDisabledByAdmin()) {
            mSwitchPref.checkRestrictionAndSetDisabled(
                    UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY);
        }
        if (mSwitchPref.isDisabledByAdmin()) {
            return true;
        }
        mInstallAppsState = mAppBridge.createInstallAppsStateFor(mPackageName,
                mPackageInfo.applicationInfo.uid);
        if (!mInstallAppsState.isPotentialAppSource()) {
            // Invalid app entry. Should not allow changing permission
            mSwitchPref.setEnabled(false);
            return true;
        }
        // Check if package is in whitelist - disable switch if not allowed
        if (!isPackageAllowedToInstallApps()) {
            mSwitchPref.setEnabled(false);
            mSwitchPref.setSummary(R.string.install_app_permission_restricted_summary);
            // If already granted, revoke it
            if (mInstallAppsState.canInstallApps()) {
                setCanInstallApps(false);
                mSwitchPref.setChecked(false);
            }
            return true;
        }
        // Auto-enable Aurora Store if it's in whitelist and doesn't have permission yet
        if (isPackageAllowedToInstallApps() && !mInstallAppsState.canInstallApps()) {
            setCanInstallApps(true);
            // Refresh state after granting permission
            mInstallAppsState = mAppBridge.createInstallAppsStateFor(mPackageName,
                    mPackageInfo.applicationInfo.uid);
        }
        mSwitchPref.setChecked(mInstallAppsState.canInstallApps());
        return true;
    }

    @Override
    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    /**
     * Check if the current package is allowed to install apps from unknown sources.
     * Only packages in the whitelist (config_allowed_install_app_packages) are allowed.
     * If the whitelist is empty or the toggle is disabled, all packages are allowed (backward compatibility).
     */
    private boolean isPackageAllowedToInstallApps() {
        if (mPackageName == null) {
            return false;
        }
        try {
            final Context context = getActivity();
            if (context == null) {
                return true; // If no context, allow (backward compatibility)
            }
            // Check if whitelist restriction is enabled
            if (!InstallAppWhitelistController.isWhitelistEnabled(context)) {
                // If toggle is disabled, allow all (backward compatibility)
                return true;
            }
            final Resources res = context.getResources();
            final String[] allowedPackages = res.getStringArray(
                    R.array.config_allowed_install_app_packages);
            // If whitelist is empty, allow all (backward compatibility)
            if (allowedPackages == null || allowedPackages.length == 0) {
                return true;
            }
            // Check if current package is in whitelist
            for (String allowedPackage : allowedPackages) {
                if (TextUtils.equals(mPackageName, allowedPackage)) {
                    return true;
                }
            }
            return false;
        } catch (Resources.NotFoundException e) {
            // If resource not found, allow all (backward compatibility)
            return true;
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MANAGE_EXTERNAL_SOURCES;
    }
}
