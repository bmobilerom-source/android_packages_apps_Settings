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

/**
 * Controller for Monet wallpaper source selection
 * Inspired by MonetCompat's wallpaperSource feature
 * Allows choosing between home screen and lock screen wallpapers
 */
public class MonetWallpaperSourceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetWallpaperSourceController(Context context, String preferenceKey) {
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
            String currentSource = Settings.Secure.getString(
                    mContext.getContentResolver(), "monet_wallpaper_source");

            if (currentSource == null || currentSource.isEmpty()) {
                currentSource = "system"; // Default to home screen wallpaper
            }
            listPreference.setValue(currentSource);

            // Update summary based on current selection
            updateSummary(preference, currentSource);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String wallpaperSource = (String) newValue;

        // Save to secure settings for theme preferences
        boolean settingSaved = Settings.Secure.putString(mContext.getContentResolver(),
                "monet_wallpaper_source", wallpaperSource);

        if (settingSaved) {
            // Apply the wallpaper source change
            applyWallpaperSourceChange(wallpaperSource);

            // Update summary immediately
            updateSummary(preference, wallpaperSource);
        }

        return settingSaved;
    }

    private void updateSummary(Preference preference, String source) {
        String summary;
        if ("system".equals(source)) {
            summary = "Uses home screen wallpaper for color theming";
        } else if ("lock_screen".equals(source)) {
            summary = "Uses lock screen wallpaper for color theming";
        } else {
            summary = "Choose which wallpaper provides color theme";
        }
        preference.setSummary(summary);
    }

    private void applyWallpaperSourceChange(String source) {
        try {
            // Send theme change broadcast with wallpaper source info
            android.content.Intent themeIntent = new android.content.Intent("android.intent.action.THEME_CHANGED");
            themeIntent.putExtra("monet_wallpaper_source", source);
            themeIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // Also send wallpaper changed broadcast to trigger color re-extraction
            android.content.Intent wallpaperIntent = new android.content.Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            // Force configuration change
            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

        } catch (Exception e) {
            android.util.Log.e("MonetWallpaperSource", "Failed to apply wallpaper source change", e);
        }
    }
}
