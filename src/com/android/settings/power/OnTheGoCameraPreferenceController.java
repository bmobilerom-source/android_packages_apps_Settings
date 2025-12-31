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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for OnTheGo camera selection (front/rear)
 */
public class OnTheGoCameraPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "OnTheGoCameraPC";

    public OnTheGoCameraPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Only show if device has front camera
        PackageManager pm = mContext.getPackageManager();
        return (pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;

            // Get current camera setting (default to rear camera = 0)
            int currentCamera = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_CAMERA,
                    0); // 0 = rear, 1 = front

            switchPreference.setChecked(currentCamera == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean useFrontCamera = (Boolean) newValue;

        // Save camera setting (0 = rear, 1 = front)
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.ON_THE_GO_CAMERA,
                useFrontCamera ? 1 : 0);

        // Send broadcast to update the service
        sendCameraBroadcast();

        return true;
    }

    private void sendCameraBroadcast() {
        try {
            Intent cameraBroadcast = new Intent();
            cameraBroadcast.setAction("com.android.systemui.epic.onthego.OnTheGoService.ACTION_TOGGLE_CAMERA");
            mContext.sendBroadcast(cameraBroadcast);
        } catch (Exception e) {
            // Ignore broadcast failures
        }
    }
}
