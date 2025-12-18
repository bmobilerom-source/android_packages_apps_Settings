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
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class MonetColorStyleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetColorStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int currentStyle = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_color_style", 0); // 0 = tonal_spot
            // Map integer back to string value
            String styleValue = mapIntToStyleString(currentStyle);
            listPreference.setValue(styleValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String styleString = (String) newValue;
        int styleValue = mapStyleStringToInt(styleString);
        return Settings.System.putInt(mContext.getContentResolver(),
                "monet_color_style", styleValue);
    }

    private String mapIntToStyleString(int styleInt) {
        switch (styleInt) {
            case 0: return "tonal_spot";
            case 1: return "vibrant";
            case 2: return "expressive";
            case 3: return "spritz";
            case 4: return "rainbow";
            case 5: return "fruit_salad";
            default: return "tonal_spot";
        }
    }

    private int mapStyleStringToInt(String styleString) {
        switch (styleString) {
            case "tonal_spot": return 0;
            case "vibrant": return 1;
            case "expressive": return 2;
            case "spritz": return 3;
            case "rainbow": return 4;
            case "fruit_salad": return 5;
            default: return 0; // Default to TONAL_SPOT
        }
    }
}
