/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.power;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for OnTheGo service restart option
 */
public class OnTheGoServiceRestartPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "OnTheGoServiceRestartPC";

    public OnTheGoServiceRestartPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Only show if device has front camera (same as camera controller)
        PackageManager pm = mContext.getPackageManager();
        return (pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;

            // Get current service restart setting (default to false = 0)
            int restartService = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_SERVICE_RESTART,
                    0); // 0 = off, 1 = on

            switchPreference.setChecked(restartService == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean restartService = (Boolean) newValue;

        // Save service restart setting (0 = off, 1 = on)
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.ON_THE_GO_SERVICE_RESTART,
                restartService ? 1 : 0);

        return true;
    }
}
