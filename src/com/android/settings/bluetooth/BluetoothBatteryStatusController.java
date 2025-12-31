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

package com.android.settings.bluetooth;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Bluetooth battery status display setting
 */
public class BluetoothBatteryStatusController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BluetoothBatteryStatusController";

    public BluetoothBatteryStatusController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;

            // Get current setting value
            boolean showBattery = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.BLUETOOTH_SHOW_BATTERY,
                    1) == 1; // Default to true

            switchPreference.setChecked(showBattery);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean showBattery = (Boolean) newValue;

        // Save setting
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.BLUETOOTH_SHOW_BATTERY,
                showBattery ? 1 : 0);

        return true;
    }
}
