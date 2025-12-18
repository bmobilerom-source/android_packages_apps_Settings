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

import com.android.settings.core.BasePreferenceController;

public class MonetColorPresetsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetColorPresetsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.ListPreference) {
            androidx.preference.ListPreference listPreference = (androidx.preference.ListPreference) preference;
            String currentPreset = android.provider.Settings.System.getString(
                    mContext.getContentResolver(), "monet_color_preset");
            if (currentPreset != null) {
                listPreference.setValue(currentPreset);
            } else {
                listPreference.setValue("default");
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String presetName = (String) newValue;

        // Get preset colors
        ColorPreset preset = getPresetByName(presetName);
        if (preset != null) {
            String colorString = preset.colors[0] + "," + preset.colors[1] + "," +
                               preset.colors[2] + "," + preset.colors[3];

            // Save to system settings
            return Settings.System.putString(mContext.getContentResolver(),
                        "monet_color_preset", presetName) &&
                   Settings.System.putString(mContext.getContentResolver(),
                        "monet_custom_colors", colorString) &&
                   Settings.System.putInt(mContext.getContentResolver(),
                        "monet_override_enabled", 1);
        }

        return false;
    }

    private ColorPreset getPresetByName(String name) {
        // Preset color definitions
        switch (name) {
            case "default":
                return new ColorPreset("Default", new int[]{0xFF4285F4, 0xFF34A853, 0xFFEA4335, 0xFFFBBC05});
            case "ocean":
                return new ColorPreset("Ocean", new int[]{0xFF1976D2, 0xFF42A5F5, 0xFF90CAF9, 0xFFE3F2FD});
            case "forest":
                return new ColorPreset("Forest", new int[]{0xFF388E3C, 0xFF4CAF50, 0xFF81C784, 0xFFE8F5E8});
            case "sunset":
                return new ColorPreset("Sunset", new int[]{0xFFF57C00, 0xFFFF9800, 0xFFFFB74D, 0xFFFFF3E0});
            default:
                return new ColorPreset("Default", new int[]{0xFF4285F4, 0xFF34A853, 0xFFEA4335, 0xFFFBBC05});
        }
    }

    private static class ColorPreset {
        public final String name;
        public final int[] colors;

        public ColorPreset(String name, int[] colors) {
            this.name = name;
            this.colors = colors;
        }
    }
}