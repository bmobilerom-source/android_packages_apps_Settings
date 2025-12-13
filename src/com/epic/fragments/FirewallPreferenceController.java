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
 * Controller for Firewall preference.
 * Only shows the preference if Firewall is installed.
 */
public class FirewallPreferenceController extends BasePreferenceController {

    private static final String PACKAGE_NAME = "com.kin.athena";

    public FirewallPreferenceController(Context context, String preferenceKey) {
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

        if (!isAppInstalled()) {
            // App not installed - show message
            android.widget.Toast.makeText(mContext,
                "Firewall app is not installed", android.widget.Toast.LENGTH_LONG).show();
            return true; // Consume the click
        }
        
        // Try to launch the app
        if (launchApp()) {
            return true;
        }
        
        // Fallback: Open app's settings page
        try {
            android.content.Intent intent = new android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + PACKAGE_NAME));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            android.util.Log.e("FirewallPreferenceController", "Error opening Firewall", e);
            return super.handlePreferenceTreeClick(preference);
        }
    }
}
