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
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for power menu On-The-Go mode toggle
 */
public class PowerMenuOnTheGoController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PowerMenuOnTheGoController";
    private static final String KEY_POWER_MENU_ONTHEGO = "power_menu_onthego_enabled";

    public PowerMenuOnTheGoController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_POWER_MENU_ONTHEGO;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.POWER_MENU_ONTHEGO_ENABLED, 0) == 1;
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        boolean success = Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.POWER_MENU_ONTHEGO_ENABLED,
                enabled ? 1 : 0);
        if (success) {
            // Update the preference state
            updateState(preference);
        }
        return success;
    }
}