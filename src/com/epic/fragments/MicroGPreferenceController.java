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
 * Controller for MicroG preference.
 * Only shows the preference if MicroG is installed.
 */
public class MicroGPreferenceController extends BasePreferenceController {

    private static final String[] MICROG_PACKAGES = {
        "com.google.android.gms",
        "org.microg.gms.droidguard",
        "com.google.android.gsf"
    };

    public MicroGPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private String findInstalledMicroGPackage() {
        PackageManager pm = mContext.getPackageManager();
        for (String packageName : MICROG_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, 0);
                return packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // Continue checking other packages
            }
        }
        return null;
    }

    @Override
    public boolean handlePreferenceTreeClick(androidx.preference.Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        String packageName = findInstalledMicroGPackage();
        if (packageName == null) {
            android.widget.Toast.makeText(mContext,
                "MicroG/GmsCore is not installed. Please install MicroG or GmsCore first.",
                android.widget.Toast.LENGTH_LONG).show();
            return true;
        }

        try {
            // First try to launch the app's main activity
            android.content.Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            }
            
            // Fallback: Open app's settings page
            intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            android.util.Log.e("MicroGPreferenceController", "Error opening MicroG", e);
            android.widget.Toast.makeText(mContext,
                "Unable to open MicroG settings", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
