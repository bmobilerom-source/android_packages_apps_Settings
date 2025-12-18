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

import com.android.settings.core.TogglePreferenceController;

public class TimeBasedColorsController extends TogglePreferenceController {

    public TimeBasedColorsController(Context context, String preferenceKey) {
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
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_time_based_enabled", 0) == 1;
            preference.setSummary(isEnabled ?
                "Colors change based on time of day (enabled)" :
                "Colors change based on time of day (disabled)");
        }
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                "monet_time_based_enabled", 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            // Set default time slot colors when enabled
            String defaultColors = "#FFF8E1,#FFE0B2,#FFCC02,#FF9800"; // Sunrise colors
            Settings.System.putString(mContext.getContentResolver(),
                    "monet_time_slot_colors", defaultColors);
            Settings.System.putString(mContext.getContentResolver(),
                    "monet_current_time_slot", "dawn");
        }

        return Settings.System.putInt(mContext.getContentResolver(),
                "monet_time_based_enabled", isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
