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

package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for enabling/disabling enhanced power menu
 * Inspired by ClassicPowerMenu's power menu replacement functionality
 */
public class PowerMenuController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public PowerMenuController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "power_menu_enhanced", 0) == 1;
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;

        // Save the setting
        boolean settingSaved = Settings.System.putInt(mContext.getContentResolver(),
                "power_menu_enhanced", isEnabled ? 1 : 0);

        if (settingSaved && isEnabled) {
            // Note: In a full implementation, this would:
            // 1. Register an Accessibility Service (like ClassicPowerMenu)
            // 2. Intercept power button long press
            // 3. Show custom power menu with selected options
            // 4. Handle power menu button actions
            //
            // For this demo, we just save the preference
        }

        return settingSaved;
    }
}
