/*
 * Copyright (C) 2025 LineageOS
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

package com.android.settings.awaken.fragments;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import com.android.settings.R;

/**
 * Adapter for managing Display Customizations preference changes.
 * Handles dependencies between preferences (e.g., blur radius enabled/disabled based on blur toggle).
 */
public class DisplayCustomizationsAdapter {

    private final Context mContext;

    public DisplayCustomizationsAdapter(Context context) {
        mContext = context;
    }

    /**
     * Setup wallpaper blur radius preference.
     * Enables/disables based on blur toggle state.
     */
    public void setupWallpaperBlurRadiusPreference(ListPreference preference) {
        if (preference == null) {
            return;
        }
        boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(mContext);
        preference.setEnabled(blurEnabled);
        updateBlurRadiusSummary(preference);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                int radius = Integer.parseInt((String) newValue);
                DisplayCustomizationsHelper.setWallpaperBlurRadius(mContext, radius);
                updateBlurRadiusSummary(preference);
                return true;
            }
        });
    }

    /**
     * Setup wallpaper blur toggle preference.
     * Updates blur radius preference enabled state.
     */
    public void setupWallpaperBlurPreference(SwitchPreference preference,
            ListPreference blurRadiusPreference) {
        if (preference == null) {
            return;
        }
        boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(mContext);
        preference.setChecked(blurEnabled);
        if (blurRadiusPreference != null) {
            blurRadiusPreference.setEnabled(blurEnabled);
        }

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                DisplayCustomizationsHelper.setWallpaperBlurEnabled(mContext, enabled);
                if (blurRadiusPreference != null) {
                    blurRadiusPreference.setEnabled(enabled);
                }
                return true;
            }
        });
    }

    /**
     * Update blur radius preference summary.
     */
    private void updateBlurRadiusSummary(ListPreference preference) {
        int radius = DisplayCustomizationsHelper.getWallpaperBlurRadius(mContext);
        preference.setSummary(mContext.getString(R.string.wallpaper_blur_radius_summary, radius));
        preference.setValue(String.valueOf(radius));
    }

    /**
     * Setup display effects preference.
     */
    public void setupDisplayEffectsPreference(SwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = DisplayCustomizationsHelper.isDisplayEffectsEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                DisplayCustomizationsHelper.setDisplayEffectsEnabled(mContext, enabled);
                return true;
            }
        });
    }
}

