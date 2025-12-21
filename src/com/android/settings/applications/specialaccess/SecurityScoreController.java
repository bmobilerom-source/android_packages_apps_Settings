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
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for app security score indicator.
 * Calculates and displays overall security score based on enabled security features.
 */
public class SecurityScoreController extends BasePreferenceController {

    public SecurityScoreController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        int securityScore = calculateSecurityScore();
        String levelText = getSecurityLevelText(securityScore);

        preference.setTitle(mContext.getString(R.string.appsec_security_score, securityScore));
        preference.setSummary(levelText);
    }

    private int calculateSecurityScore() {
        int score = 0;

        // Check each security feature and add points if enabled
        if (isInstallWhitelistEnabled()) score += 25;
        if (isBlockAppDashboardEnabled()) score += 15;
        if (isBlockUsbPopupEnabled()) score += 15;
        if (isBlockLocationSettingsEnabled()) score += 15;
        if (isBlockAccountDashboardEnabled()) score += 15;
        if (isBlockSafetyCenterEnabled()) score += 15;

        return Math.min(100, score);
    }

    private String getSecurityLevelText(int score) {
        if (score >= 80) {
            return mContext.getString(R.string.appsec_security_level_maximum);
        } else if (score >= 60) {
            return mContext.getString(R.string.appsec_security_level_advanced);
        } else {
            return mContext.getString(R.string.appsec_security_level_basic);
        }
    }

    private boolean isInstallWhitelistEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "install_app_whitelist_enabled", 0) == 1;
    }

    private boolean isBlockAppDashboardEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "block_app_dashboard_enabled", 0) == 1;
    }

    private boolean isBlockUsbPopupEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "block_usb_popup_enabled", 0) == 1;
    }

    private boolean isBlockLocationSettingsEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "block_location_settings_enabled", 0) == 1;
    }

    private boolean isBlockAccountDashboardEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "block_account_dashboard_enabled", 0) == 1;
    }

    private boolean isBlockSafetyCenterEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            "block_safety_center_enabled", 0) == 1;
    }
}

