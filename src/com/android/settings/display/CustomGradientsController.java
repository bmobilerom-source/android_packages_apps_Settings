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

public class CustomGradientsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public CustomGradientsController(Context context, String preferenceKey) {
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
            String currentGradient = android.provider.Settings.System.getString(
                    mContext.getContentResolver(), "monet_gradient_type");
            if (currentGradient != null) {
                listPreference.setValue(currentGradient);
            } else {
                listPreference.setValue("none");
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String gradientType = (String) newValue;

        // Get gradient colors
        String[] colors = getGradientColors(gradientType);
        if (colors != null) {
            String colorString = String.join(",", colors);

            // Save to system settings
            return Settings.System.putString(mContext.getContentResolver(),
                        "monet_gradient_type", gradientType) &&
                   Settings.System.putString(mContext.getContentResolver(),
                        "monet_gradient_colors", colorString) &&
                   Settings.System.putInt(mContext.getContentResolver(),
                        "monet_gradient_enabled", 1);
        }

        return false;
    }

    private String[] getGradientColors(String gradientType) {
        switch (gradientType) {
            case "sunrise":
                return new String[]{"#FFF8E1", "#FFE0B2", "#FFCC02", "#FF9800"};
            case "ocean":
                return new String[]{"#E3F2FD", "#90CAF9", "#42A5F5", "#1976D2"};
            case "forest":
                return new String[]{"#E8F5E8", "#81C784", "#4CAF50", "#388E3C"};
            case "lavender":
                return new String[]{"#F3E5F5", "#BA68C8", "#8E24AA", "#6A1B9A"};
            default:
                return new String[]{"#FFFFFF", "#E0E0E0", "#BDBDBD", "#757575"};
        }
    }
}
