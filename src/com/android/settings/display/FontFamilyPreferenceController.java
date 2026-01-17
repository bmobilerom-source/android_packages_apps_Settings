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
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FontFamilyPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "FontFamilyPreferenceController";

    private ListPreference mPreference;

    // Map of font values to overlay package names
    private static final Map<String, String> FONT_OVERLAY_MAP = new HashMap<>();
    static {
        FONT_OVERLAY_MAP.put("accuratist", "com.bmobileexpression.font.accuratist.overlay");
        FONT_OVERLAY_MAP.put("hercules", "com.bmobileexpression.font.hercules.overlay");
        FONT_OVERLAY_MAP.put("ios", "com.bmobileexpression.font.ios.overlay");
        FONT_OVERLAY_MAP.put("nothing", "com.bmobileexpression.font.nothing.overlay");
        FONT_OVERLAY_MAP.put("oneplus", "com.bmobileexpression.font.oneplus.overlay");
        FONT_OVERLAY_MAP.put("sanfranciscopro", "com.bmobileexpression.font.sanfranciscopro.overlay");
        FONT_OVERLAY_MAP.put("samsungone", "com.bmobileexpression.font.samsungone.overlay");
        FONT_OVERLAY_MAP.put("slim", "com.bmobileexpression.font.slim.overlay");
    }

    public FontFamilyPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return "font_family_selection";
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            updatePreferenceSummary();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String fontValue = (String) newValue;

        android.util.Log.i(TAG, "Font preference changed to: " + fontValue);

        // Update theme customization overlay packages setting
        updateThemeCustomizationOverlays(fontValue);

        updatePreferenceSummary();
        return true;
    }

    private void updateThemeCustomizationOverlays(String fontValue) {
        try {
            JSONObject themeCustomization = new JSONObject();

            if (!"default".equals(fontValue)) {
                String overlayPackage = FONT_OVERLAY_MAP.get(fontValue);
                if (overlayPackage != null) {
                    // Set the font overlay for the android.theme.customization.font category
                    themeCustomization.put("android.theme.customization.font", overlayPackage);
                    android.util.Log.i(TAG, "Setting font overlay: " + overlayPackage + " for font: " + fontValue);
                }
            }
            // For "default", we don't set any font overlay, letting the system use defaults

            String themeJson = themeCustomization.toString();
            Settings.Secure.putStringForUser(mContext.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES, themeJson,
                    UserHandle.myUserId());

            android.util.Log.i(TAG, "Updated THEME_CUSTOMIZATION_OVERLAY_PACKAGES: " + themeJson);

        } catch (JSONException e) {
            android.util.Log.e(TAG, "Failed to update theme customization overlays", e);
        }
    }

    private void updatePreferenceSummary() {
        if (mPreference != null) {
            String currentValue = mPreference.getValue();
            if (currentValue == null || "default".equals(currentValue)) {
                mPreference.setSummary(R.string.font_family_default);
            } else {
                // Find the display name for the current value
                String[] values = mContext.getResources().getStringArray(R.array.font_family_values);
                String[] entries = mContext.getResources().getStringArray(R.array.font_family_entries);

                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(currentValue)) {
                        mPreference.setSummary(entries[i]);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        // Read current theme customization setting to determine active font
        String currentFont = "default";
        try {
            String themeJson = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    UserHandle.myUserId());
            if (themeJson != null && !themeJson.isEmpty()) {
                JSONObject themeCustomization = new JSONObject(themeJson);
                String fontOverlay = themeCustomization.optString("android.theme.customization.font", null);
                if (fontOverlay != null) {
                    // Find which font this overlay corresponds to
                    for (Map.Entry<String, String> entry : FONT_OVERLAY_MAP.entrySet()) {
                        if (entry.getValue().equals(fontOverlay)) {
                            currentFont = entry.getKey();
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Failed to parse current theme customization", e);
        }

        if (mPreference != null) {
            mPreference.setValue(currentFont);
        }

        updatePreferenceSummary();
    }
}