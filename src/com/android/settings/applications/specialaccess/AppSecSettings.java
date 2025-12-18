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

import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class AppSecSettings extends DashboardFragment {

    private static final String TAG = "AppSecSettings";

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
        final Context context = getContext();

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
