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
import android.content.SharedPreferences;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class CustomGradientsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_FILE = "monet_prefs";
    private static final String KEY_GRADIENT_TYPE = "monet_gradient_type";
    private static final String KEY_GRADIENT_COLORS = "monet_gradient_colors";
    private static final String KEY_GRADIENT_ENABLED = "monet_gradient_enabled";

    public CustomGradientsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String gradientType = (String) newValue;

        // Simplified gradient implementation
        String[] colors = getGradientColors(gradientType);
        if (colors != null) {
            String colorString = String.join(",", colors);

            SharedPreferences prefs = mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return prefs.edit()
                    .putString(KEY_GRADIENT_TYPE, gradientType)
                    .putString(KEY_GRADIENT_COLORS, colorString)
                    .putBoolean(KEY_GRADIENT_ENABLED, true)
                    .commit();
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
