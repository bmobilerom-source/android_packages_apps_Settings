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
                    "monet_color_style", 1); // Default to TONAL_SPOT (1)
            // Map integer back to string value
            String styleValue = mapIntToStyleString(currentStyle);
            listPreference.setValue(styleValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String styleString = (String) newValue;
        int styleValue = mapStyleStringToInt(styleString);
        
        // Save the style setting
        boolean styleSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_color_style", styleValue);
        
        if (styleSaved) {
            // Trigger theme refresh so style change takes effect immediately
            triggerThemeRefresh();
        }
        
        return styleSaved;
    }

    /**
     * Triggers a theme refresh when style changes
     */
    private void triggerThemeRefresh() {
        try {
            android.content.Intent wallpaperIntent = new android.content.Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);
        } catch (Exception e) {
            // Best effort - ignore exceptions
        }
    }

    private String mapIntToStyleString(int styleInt) {
        switch (styleInt) {
            case 0: return "spritz";
            case 1: return "tonal_spot";
            case 2: return "vibrant";
            case 3: return "expressive";
            case 4: return "rainbow";
            case 5: return "fruit_salad";
            case 6: return "content";
            case 7: return "monochromatic";
            default: return "tonal_spot";
        }
    }

    private int mapStyleStringToInt(String styleString) {
        switch (styleString) {
            case "spritz": return 0;
            case "tonal_spot": return 1;
            case "vibrant": return 2;
            case "expressive": return 3;
            case "rainbow": return 4;
            case "fruit_salad": return 5;
            case "content": return 6;
            case "monochromatic": return 7;
            default: return 1; // Default to TONAL_SPOT (1)
        }
    }
}
