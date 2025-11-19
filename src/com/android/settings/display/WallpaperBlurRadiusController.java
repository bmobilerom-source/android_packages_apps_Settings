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

package com.android.settings.display;

import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.awaken.fragments.DisplayCustomizationsHelper;

/**
 * Controller for Wallpaper Blur Radius preference
 */
public class WallpaperBlurRadiusController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public WallpaperBlurRadiusController(Context context, String preferenceKey) {
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
            int radius = DisplayCustomizationsHelper.getWallpaperBlurRadius(mContext);
            listPreference.setValue(String.valueOf(radius));
            listPreference.setSummary(mContext.getString(R.string.wallpaper_blur_radius_summary, radius));
            
            // Enable/disable based on blur toggle
            boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(mContext);
            listPreference.setEnabled(blurEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            int radius = Integer.parseInt((String) newValue);
            DisplayCustomizationsHelper.setWallpaperBlurRadius(mContext, radius);
            updateState(preference);
            return true;
        }
        return false;
    }
}

