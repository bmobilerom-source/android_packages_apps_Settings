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

package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

public class ColorTemperatureController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String COLOR_TEMPERATURE_KEY = "color_temperature";

    public ColorTemperatureController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return COLOR_TEMPERATURE_KEY;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            final SwitchPreference switchPreference = (SwitchPreference) preference;
            final boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                    "color_temperature_enabled", 0) == 1;
            switchPreference.setChecked(enabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                "color_temperature_enabled", enabled ? 1 : 0);
        updateState(preference);
        return true;
    }
}