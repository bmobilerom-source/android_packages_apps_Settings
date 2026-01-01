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
import android.content.pm.PackageManager;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Ad Services preference.
 * Only shows the preference if Ad Services is available.
 */
public class AdServicesPreferenceController extends BasePreferenceController {

    private static final String PACKAGE_NAME = "com.android.adservices.api";

    public AdServicesPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Always available - preference should be visible even if app not installed
        return AVAILABLE;
    }

    private boolean isAppInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean launchApp() {
        try {
            android.content.Intent intent =
                    mContext.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
            if (intent == null) return false;
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(androidx.preference.Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        android.util.Log.d("AdServicesPreferenceController", "Ad Services preference clicked");

        // Try to open Ad Services settings directly
        try {
            android.content.Intent intent = new android.content.Intent();
            intent.setClassName("com.android.adservices.api",
                "com.android.adservices.ui.settings.activities.AdServicesSettingsMainActivity");
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            android.util.Log.d("AdServicesPreferenceController", "Opened Ad Services settings");
            return true;
        } catch (Exception e) {
            android.util.Log.d("AdServicesPreferenceController", "Failed to open Ad Services settings", e);
        }

        // Fallback: Try to open system privacy settings
        try {
            android.content.Intent intent = new android.content.Intent(
                android.provider.Settings.ACTION_PRIVACY_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            android.util.Log.d("AdServicesPreferenceController", "Opened system privacy settings");
            return true;
        } catch (Exception e) {
            android.util.Log.d("AdServicesPreferenceController", "Failed to open system privacy settings", e);
        }

        // Fallback: Try to launch the Ad Services app
        if (isAppInstalled()) {
            android.util.Log.d("AdServicesPreferenceController", "Ad Services app is installed, launching");
            if (launchApp()) {
                android.util.Log.d("AdServicesPreferenceController", "Successfully launched Ad Services app");
                return true;
            }
        } else {
            android.util.Log.d("AdServicesPreferenceController", "Ad Services app is not installed");
        }

        // Final fallback: Open app's settings page if app exists
        if (isAppInstalled()) {
            try {
                android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + PACKAGE_NAME));
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                android.util.Log.d("AdServicesPreferenceController", "Opened Ad Services app settings");
                return true;
            } catch (Exception e) {
                android.util.Log.e("AdServicesPreferenceController", "Error opening Ad Services app settings", e);
            }
        }

        // Last resort: Show message
        android.widget.Toast.makeText(mContext,
            "Ad Services settings are not available on this device", android.widget.Toast.LENGTH_LONG).show();
        return true; // Consume the click
    }
}
