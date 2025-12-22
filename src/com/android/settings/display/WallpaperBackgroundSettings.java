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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.awaken.fragments.DisplayCustomizationsAdapter;
import com.android.settings.awaken.fragments.DisplayCustomizationsHelper;

/**
 * Settings page for Wallpaper Background feature
 * Includes wallpaper background toggle, blur toggle, and blur radius
 */
public class WallpaperBackgroundSettings extends SettingsPreferenceFragment {

    private static final String KEY_WALLPAPER_BACKGROUND = "settings_wallpaper_background";
    private static final String KEY_WALLPAPER_BLUR = "wallpaper_blur";
    private static final String KEY_WALLPAPER_BLUR_RADIUS = "wallpaper_blur_radius";
    
    private DisplayCustomizationsAdapter mAdapter;
    private SwitchPreference mWallpaperBackgroundPreference;
    private SwitchPreference mWallpaperBlurPreference;
    private ListPreference mWallpaperBlurRadiusPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_background_settings);
        
        mAdapter = new DisplayCustomizationsAdapter(getContext());
        initializePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceStates();
    }

    /**
     * Initialize preferences and setup adapters.
     */
    private void initializePreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }
        
        mWallpaperBackgroundPreference = screen.findPreference(KEY_WALLPAPER_BACKGROUND);
        mWallpaperBlurPreference = screen.findPreference(KEY_WALLPAPER_BLUR);
        mWallpaperBlurRadiusPreference = screen.findPreference(KEY_WALLPAPER_BLUR_RADIUS);

        // Setup wallpaper blur preferences using adapter
        if (mWallpaperBlurPreference != null && mWallpaperBlurRadiusPreference != null) {
            mAdapter.setupWallpaperBlurPreference(mWallpaperBlurPreference, mWallpaperBlurRadiusPreference);
            mAdapter.setupWallpaperBlurRadiusPreference(mWallpaperBlurRadiusPreference);
        }
        
        updatePreferenceStates();
    }

    /**
     * Update preference states based on current settings.
     */
    private void updatePreferenceStates() {
        if (mWallpaperBackgroundPreference != null) {
            boolean enabled = DisplayCustomizationsHelper.isWallpaperBackgroundEnabled(getContext());
            mWallpaperBackgroundPreference.setChecked(enabled);
        }
        
        if (mWallpaperBlurPreference != null) {
            boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(getContext());
            mWallpaperBlurPreference.setChecked(blurEnabled);
        }
        
        if (mWallpaperBlurRadiusPreference != null) {
            int radius = DisplayCustomizationsHelper.getWallpaperBlurRadius(getContext());
            mWallpaperBlurRadiusPreference.setValue(String.valueOf(radius));
            mWallpaperBlurRadiusPreference.setSummary(
                    getString(R.string.wallpaper_blur_radius_summary, radius));
            boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(getContext());
            mWallpaperBlurRadiusPreference.setEnabled(blurEnabled);
        }
    }

    @Override
    public int getMetricsCategory() {
        return com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
