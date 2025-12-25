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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This helper class makes the wallpaper background feature independent and transferable.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Ensure Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED is defined
 * 3. Update the setting key if needed (currently "settings_wallpaper_background_enabled")
 * 4. Copy AdaptiveWallpaperBackgroundView.java
 */

package com.android.settings.display;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.os.UserHandle;
import android.provider.Settings;

/**
 * Helper class for Wallpaper Background feature
 * Provides utility methods for checking and managing wallpaper background settings
 * ENFORCES: Wallpaper background only works in dark mode
 */
public class WallpaperBackgroundHelper {

    /**
     * Setting key for wallpaper background enabled state
     * Update this if your ROM uses a different key
     */
    public static final String SETTING_KEY = Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED;

    /**
     * Check if wallpaper background is enabled
     * @param context The context
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(Context context) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getIntForUser(resolver, SETTING_KEY, 0, UserHandle.USER_CURRENT) != 0;
    }

    /**
     * Set wallpaper background enabled state
     * Only allows enabling if dark mode is active
     * @param context The context
     * @param enabled true to enable, false to disable
     * @return true if successful, false if dark mode is required but not active
     */
    public static boolean setEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        
        // If trying to enable, check dark mode first
        if (enabled && !isDarkMode(context)) {
            return false; // Cannot enable without dark mode
        }
        
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putIntForUser(resolver, SETTING_KEY, enabled ? 1 : 0, UserHandle.USER_CURRENT);
        return true;
    }

    /**
     * Check if device is in dark mode
     * @param context The context
     * @return true if dark mode is active
     */
    public static boolean isDarkMode(Context context) {
        if (context == null) {
            return false;
        }
        int nightMode = context.getResources().getConfiguration().uiMode 
            & Configuration.UI_MODE_NIGHT_MASK;
        return (nightMode == Configuration.UI_MODE_NIGHT_YES);
    }

    /**
     * Check if wallpaper background is active (enabled AND dark mode)
     * This is the actual check for whether the wallpaper should be displayed
     * @param context The context
     * @return true if both conditions are met
     */
    public static boolean isActive(Context context) {
        return isEnabled(context) && isDarkMode(context);
    }

    /**
     * Get summary string resource based on current state
     * @param context The context
     * @return Resource ID for appropriate summary string
     */
    public static int getSummaryResId(Context context) {
        if (!isEnabled(context)) {
            return com.android.settings.R.string.settings_wallpaper_background_summary;
        }
        
        if (isDarkMode(context)) {
            return com.android.settings.R.string.settings_wallpaper_background_summary_enabled;
        } else {
            return com.android.settings.R.string.settings_wallpaper_background_summary_enabled_light_mode;
        }
    }
}
