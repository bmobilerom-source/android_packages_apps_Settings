/*
 * Copyright (C) 2025 BashaMobile
 * Copyright (C) 2025 LineageOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.settings.display;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.awaken.fragments.DisplayCustomizationsAdapter;
import com.android.settings.awaken.fragments.DisplayCustomizationsHelper;

/**
 * Settings page for Wallpaper Background feature
 * Includes wallpaper background toggle, blur toggle, and blur radius
 * All preferences are managed by their controllers defined in XML
 */
public class WallpaperBackgroundSettings extends SettingsPreferenceFragment {

    private static final String KEY_WALLPAPER_BLUR = "wallpaper_blur";
    private static final String KEY_WALLPAPER_BLUR_RADIUS = "wallpaper_blur_radius";
    
    private DisplayCustomizationsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_background_settings);
        
        mAdapter = new DisplayCustomizationsAdapter(getContext());
        setupBlurPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBlurRadiusState();
    }

    /**
     * Setup blur preferences using adapter for proper dependency handling.
     */
    private void setupBlurPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }
        
        Preference blurPreference = screen.findPreference(KEY_WALLPAPER_BLUR);
        Preference blurRadiusPreference = screen.findPreference(KEY_WALLPAPER_BLUR_RADIUS);

        // Setup wallpaper blur radius preference using adapter
        // The blur toggle is handled by WallpaperBlurController
        if (blurRadiusPreference instanceof androidx.preference.ListPreference) {
            mAdapter.setupWallpaperBlurRadiusPreference(
                (androidx.preference.ListPreference) blurRadiusPreference);
            
            // Set up listener on blur preference to update radius enabled state
            if (blurPreference instanceof androidx.preference.SwitchPreference) {
                androidx.preference.SwitchPreference blurSwitch = 
                    (androidx.preference.SwitchPreference) blurPreference;
                blurSwitch.setOnPreferenceChangeListener((pref, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    blurRadiusPreference.setEnabled(enabled);
                    return true; // Let controller handle the actual state change
                });
            }
        }
    }

    /**
     * Update blur radius enabled state based on blur toggle.
     */
    private void updateBlurRadiusState() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }
        
        Preference blurRadiusPreference = screen.findPreference(KEY_WALLPAPER_BLUR_RADIUS);
        if (blurRadiusPreference != null) {
            boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(getContext());
            blurRadiusPreference.setEnabled(blurEnabled);
        }
    }

    @Override
    public int getMetricsCategory() {
        return com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
