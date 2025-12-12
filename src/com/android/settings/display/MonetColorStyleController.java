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

    private static final String KEY_MONET_COLOR_STYLE = "monet_color_style";

    public MonetColorStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isPublicSlice() {
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            String currentStyle = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.MONET_COLOR_STYLE);
            if (currentStyle == null) {
                currentStyle = "tonal_spot";
            }
            listPreference.setValue(currentStyle);
            updateSummary(listPreference, currentStyle);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String style = (String) newValue;
        Settings.Secure.putString(
            mContext.getContentResolver(),
            Settings.Secure.MONET_COLOR_STYLE,
            style);

        // Apply the style change immediately (like wallpaper picker does)
        applyMonetStyleChange(style);

        // Notify system of theme change
        notifyThemeChange();

        if (preference instanceof ListPreference) {
            updateSummary((ListPreference) preference, style);
        }

        return true;
    }

    private void updateSummary(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index >= 0) {
            preference.setSummary(preference.getEntries()[index]);
        }
    }

    private void applyMonetStyleChange(String style) {
        // Apply monet style change like wallpaper picker does
        // This requires SystemUI framework modifications to actually work
        // For now, we save the preference and notify the system

        // The actual implementation would be in SystemUI:
        // 1. Update ColorScheme based on the selected style
        // 2. Rebuild tonal palettes (accent1, accent2, accent3, neutral1, neutral2)
        // 3. Apply colors through ThemeOverlayManager
        // 4. Update all system components that use monet colors

        // Placeholder for framework implementation
        android.util.Log.d("MonetColorStyleController", "Applying monet style: " + style);
    }

    private void notifyThemeChange() {
        // Notify the system that theme has changed (like wallpaper picker does)
        try {
            // Send broadcast to notify theme change
            Intent intent = new Intent("com.android.settings.MONET_THEME_CHANGED");
            intent.setPackage("com.android.systemui");
            mContext.sendBroadcast(intent);

            // Also notify Settings app to refresh theme
            Intent settingsIntent = new Intent("com.android.settings.THEME_CHANGED");
            mContext.sendBroadcast(settingsIntent);
        } catch (Exception e) {
            android.util.Log.e("MonetColorStyleController", "Error notifying theme change", e);
        }
    }
}
