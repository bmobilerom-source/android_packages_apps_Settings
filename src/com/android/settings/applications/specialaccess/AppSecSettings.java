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

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.password.ConfirmDeviceCredentialActivity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class AppSecSettings extends DashboardFragment {

    private static final String TAG = "AppSecSettings";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1001;
    private boolean mIsAuthenticated = false;

    // Controllers
    private BlockAppDashboardController mBlockAppDashboardController;
    private InstallAppWhitelistController mInstallAppWhitelistController;
    private BlockUsbPopupController mBlockUsbPopupController;
    private BlockLocationSettingsController mBlockLocationSettingsController;
    private BlockAccountDashboardController mBlockAccountDashboardController;
    private BlockSafetyCenterController mBlockSafetyCenterController;

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        // Check authentication first before initializing controllers
        if (icicle != null) {
            mIsAuthenticated = icicle.getBoolean("is_authenticated", false);
        }
        
        final Context context = getContext();

        // Don't initialize controllers if not authenticated
        if (!mIsAuthenticated) {
            return;
        }
        
        // Initialize controllers
        final PreferenceScreen prefScreen = getPreferenceScreen();

        if (prefScreen != null) {
            // Initialize Block App Dashboard controller
            mBlockAppDashboardController = new BlockAppDashboardController(
                    context, "block_app_dashboard_toggle");
            if (mBlockAppDashboardController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference blockPref = prefScreen.findPreference("block_app_dashboard_toggle");
                if (blockPref != null) {
                    mBlockAppDashboardController.updateState(blockPref);
                    blockPref.setOnPreferenceChangeListener(mBlockAppDashboardController);
                }
            }

            // Initialize Install App Whitelist controller
            mInstallAppWhitelistController = new InstallAppWhitelistController(
                    context, "install_app_whitelist_toggle");
            if (mInstallAppWhitelistController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference whitelistPref = prefScreen.findPreference("install_app_whitelist_toggle");
                if (whitelistPref != null) {
                    mInstallAppWhitelistController.updateState(whitelistPref);
                    whitelistPref.setOnPreferenceChangeListener(mInstallAppWhitelistController);
                }
            }

            // Initialize Block USB Popup controller
            mBlockUsbPopupController = new BlockUsbPopupController(
                    context, "block_usb_popup_toggle");
            if (mBlockUsbPopupController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference usbPopupPref = prefScreen.findPreference("block_usb_popup_toggle");
                if (usbPopupPref != null) {
                    mBlockUsbPopupController.displayPreference(prefScreen);
                    mBlockUsbPopupController.updateState(usbPopupPref);
                    usbPopupPref.setOnPreferenceChangeListener(mBlockUsbPopupController);
                }
            }

            // Initialize Block Location Settings controller
            mBlockLocationSettingsController = new BlockLocationSettingsController(
                    context, "block_location_settings_toggle");
            if (mBlockLocationSettingsController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference locationPref = prefScreen.findPreference("block_location_settings_toggle");
                if (locationPref != null) {
                    mBlockLocationSettingsController.updateState(locationPref);
                    locationPref.setOnPreferenceChangeListener(mBlockLocationSettingsController);
                }
            }

            // Initialize Block Account Dashboard controller
            mBlockAccountDashboardController = new BlockAccountDashboardController(
                    context, "block_account_dashboard_toggle");
            if (mBlockAccountDashboardController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference accountPref = prefScreen.findPreference("block_account_dashboard_toggle");
                if (accountPref != null) {
                    mBlockAccountDashboardController.updateState(accountPref);
                    accountPref.setOnPreferenceChangeListener(mBlockAccountDashboardController);
                }
            }

            // Initialize Block Safety Center controller
            mBlockSafetyCenterController = new BlockSafetyCenterController(
                    context, "block_safety_center_toggle");
            if (mBlockSafetyCenterController.getAvailabilityStatus() ==
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference safetyCenterPref = prefScreen.findPreference("block_safety_center_toggle");
                if (safetyCenterPref != null) {
                    mBlockSafetyCenterController.updateState(safetyCenterPref);
                    safetyCenterPref.setOnPreferenceChangeListener(mBlockSafetyCenterController);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_authenticated", mIsAuthenticated);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Check if authentication is required
        if (!mIsAuthenticated) {
            checkAndRequestAuthentication();
            return;
        }
        
        // Refresh USB popup preference state
        if (mBlockUsbPopupController != null) {
            SwitchPreference usbPopupPref = getPreferenceScreen().findPreference("block_usb_popup_toggle");
            if (usbPopupPref != null) {
                mBlockUsbPopupController.updateState(usbPopupPref);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (resultCode == Activity.RESULT_OK) {
                mIsAuthenticated = true;
            } else {
                // Authentication failed or cancelled - finish the activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }

    private void checkAndRequestAuthentication() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null || !km.isKeyguardSecure()) {
            // No lock screen set up - allow access without authentication
            mIsAuthenticated = true;
            return;
        }

        // Request device credential confirmation
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra(KeyguardManager.EXTRA_TITLE,
                context.getString(R.string.appsec_confirm_credential_title));
        intent.putExtra(KeyguardManager.EXTRA_DESCRIPTION,
                context.getString(R.string.appsec_confirm_credential_description));
        intent.putExtra(KeyguardManager.EXTRA_DISALLOW_BIOMETRICS_IF_POLICY_EXISTS, false);

        try {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_CREDENTIAL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch credential confirmation", e);
            // If we can't launch auth, allow access (fallback)
            mIsAuthenticated = true;
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.appsec;
    }

    @Override
    public int getMetricsCategory() {
        return 0; // TODO: Add proper metrics category
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.appsec);
}
