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
import android.os.SystemProperties;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.development.SystemPropPoker;

/**
 * Standalone controller for Color Temperature toggle
 * Works independently of Developer Options
 */
public class ColorTemperatureController extends TogglePreferenceController {

    private static final String COLOR_TEMPERATURE_PROPERTY = "persist.sys.debug.color_temp";

    public ColorTemperatureController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Check if color temperature is enabled in config
        if (mContext.getResources().getBoolean(R.bool.config_enableColorTemperature)) {
            return AVAILABLE;
        }
        return DISABLED_DEPENDENT_SETTING;
    }

    @Override
    public boolean isChecked() {
        return SystemProperties.getBoolean(COLOR_TEMPERATURE_PROPERTY, false);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        SystemProperties.set(COLOR_TEMPERATURE_PROPERTY, Boolean.toString(isChecked));
        SystemPropPoker.getInstance().poke();
        displayColorTemperatureToast();
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof TwoStatePreference) {
            ((TwoStatePreference) preference).setChecked(isChecked());
        }
    }

    private void displayColorTemperatureToast() {
        Toast.makeText(mContext, R.string.color_temperature_toast, Toast.LENGTH_LONG).show();
    }
}

