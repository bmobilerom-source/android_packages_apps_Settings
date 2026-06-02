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

import org.json.JSONException;
import org.json.JSONObject;

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
            String currentStyleString = getCurrentThemeStyle();
            // Map string to the value expected by the list preference
            String styleValue = mapStyleNameToPreferenceValue(currentStyleString);
            listPreference.setValue(styleValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String styleString = (String) newValue;
        String styleName = mapPreferenceValueToStyleName(styleString);

        // Save to the proper theme customization JSON structure
        boolean styleSaved = saveThemeStyle(styleName);

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

    /**
     * Get the current theme style from Settings
     */
    private String getCurrentThemeStyle() {
        String overlayPackagesJson = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);

        if (overlayPackagesJson != null && !overlayPackagesJson.isEmpty()) {
            try {
                JSONObject object = new JSONObject(overlayPackagesJson);
                return object.optString("android.theme.customization.theme_style", "tonal_spot");
            } catch (JSONException e) {
                // Ignore and return default
            }
        }
        return "tonal_spot"; // Default style
    }

    /**
     * Save the theme style to Settings using the proper JSON structure
     */
    private boolean saveThemeStyle(String styleName) {
        try {
            String overlayPackagesJson = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);

            JSONObject object;
            if (overlayPackagesJson != null && !overlayPackagesJson.isEmpty()) {
                object = new JSONObject(overlayPackagesJson);
            } else {
                object = new JSONObject();
            }

            object.put("android.theme.customization.theme_style", styleName);

            return Settings.Secure.putString(
                    mContext.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    object.toString());

        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Map preference value (from arrays.xml) to style name
     */
    private String mapPreferenceValueToStyleName(String preferenceValue) {
        // The preferenceValue is already the style name from arrays.xml
        return preferenceValue;
    }

    /**
     * Map style name to preference value (for display)
     */
    private String mapStyleNameToPreferenceValue(String styleName) {
        // The preference value is the style name itself
        return styleName;
    }
}
