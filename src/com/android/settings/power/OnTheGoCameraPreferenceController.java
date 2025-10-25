/*
 * Copyright (C) 2025 The Android Open Source Project
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
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Controller for OnTheGo camera selection preference
 */
public class OnTheGoCameraPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "OnTheGoCameraController";
    private static final String KEY_ONTHEGO_CAMERA = "onthego_camera";

    public OnTheGoCameraPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ONTHEGO_CAMERA;
    }

    @Override
    public boolean isAvailable() {
        // Only show if device has front camera
        PackageManager pm = mContext.getPackageManager();
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
            boolean useFrontCamera = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_CAMERA, 0) == 1;
            switchPreference.setChecked(useFrontCamera);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SwitchPreferenceCompat) {
            boolean useFrontCamera = (Boolean) newValue;
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_CAMERA, useFrontCamera ? 1 : 0);
            
            // Send broadcast to update the service
            sendCameraBroadcast();
            return true;
        }
        return false;
    }

    private void sendCameraBroadcast() {
        try {
            Intent cameraBroadcast = new Intent();
            cameraBroadcast.setAction("com.android.systemui.epic.onthego.OnTheGoService.ACTION_TOGGLE_CAMERA");
            mContext.sendBroadcast(cameraBroadcast);
            Log.d(TAG, "Sent camera broadcast");
        } catch (Exception e) {
            Log.e(TAG, "Error sending camera broadcast: " + e.getMessage());
        }
    }
}
