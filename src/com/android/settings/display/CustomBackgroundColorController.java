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
import android.content.Intent;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class CustomBackgroundColorController extends BasePreferenceController {

    public CustomBackgroundColorController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        // For now, cycle through some preset colors when clicked
        String currentColor = Settings.System.getString(mContext.getContentResolver(),
                "settings_background_custom_color");
        String nextColor;
        if ("#FF6B6B".equals(currentColor)) {
            nextColor = "#4ECDC4";
        } else if ("#4ECDC4".equals(currentColor)) {
            nextColor = "#45B7D1";
        } else if ("#45B7D1".equals(currentColor)) {
            nextColor = "#96CEB4";
        } else {
            nextColor = "#FF6B6B"; // Default to first color
        }

        boolean success = Settings.System.putString(mContext.getContentResolver(),
                "settings_background_custom_color", nextColor);
        if (success && preference != null) {
            preference.setSummary("Current color: " + nextColor);
        }
        return success;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            String currentColor = Settings.System.getString(mContext.getContentResolver(),
                    "settings_background_custom_color");
            if (currentColor != null) {
                preference.setSummary("Current color: " + currentColor);
            } else {
                preference.setSummary("Click to set custom color");
            }
        }
    }
}
