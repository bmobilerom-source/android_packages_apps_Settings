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

public class MonetResetController extends BasePreferenceController {

    public MonetResetController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary("Reset all Monet settings to wallpaper-based defaults");
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        // Reset to monochrome theme as requested
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_color_style", 7); // Monochromatic style
        Settings.System.putString(mContext.getContentResolver(),
                "monet_color_preset", "gray"); // Gray/monochrome preset
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_preset_enabled", 1); // Enable preset mode
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_override_enabled", 0);
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_time_based_enabled", 0);
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_contextual_enabled", 0);

        // Reset gradient and background settings
        Settings.System.putString(mContext.getContentResolver(),
                "monet_gradient_type", "none");
        Settings.System.putString(mContext.getContentResolver(),
                "settings_background_mode", "wallpaper");

        // Set monochrome seed color
        Settings.System.putInt(mContext.getContentResolver(),
                "monet_seed_color", 0xFF757575); // Gray color

        return true;
    }
}
