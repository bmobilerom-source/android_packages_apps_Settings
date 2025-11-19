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
import android.widget.Toast;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for Settings Wallpaper Background toggle
 * Only works in dark mode - enforces dark mode requirement
 */
public class SettingsWallpaperBackgroundController extends TogglePreferenceController {

    public SettingsWallpaperBackgroundController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return WallpaperBackgroundHelper.isEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // Only allow enabling if dark mode is active
        if (isChecked && !WallpaperBackgroundHelper.isDarkMode(mContext)) {
            Toast.makeText(mContext, 
                    R.string.settings_wallpaper_background_dark_mode_required, 
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return WallpaperBackgroundHelper.setEnabled(mContext, isChecked);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            // Disable preference if not in dark mode and enabled
            boolean enabled = isChecked();
            boolean darkMode = WallpaperBackgroundHelper.isDarkMode(mContext);
            if (enabled && !darkMode) {
                // Auto-disable if light mode is enabled
                WallpaperBackgroundHelper.setEnabled(mContext, false);
                preference.setEnabled(true);
            }
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return com.android.settings.R.string.menu_key_display;
    }
}
