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

import android.content.ContentResolver;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

/**
 * Helper class for Display Customizations features.
 * Manages wallpaper blur, display effects, and related settings.
 */
public class DisplayCustomizationsHelper {

    /**
     * Check if wallpaper background is enabled.
     */
    public static boolean isWallpaperBackgroundEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getIntForUser(resolver,
                Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, 
                0,
                UserHandle.USER_CURRENT) == 1;
    }

    /**
     * Set wallpaper background enabled state.
     */
    public static boolean setWallpaperBackgroundEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        // Use putIntForUser to ensure it works for the current user and triggers observers
        return Settings.System.putIntForUser(resolver,
                Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, 
                enabled ? 1 : 0,
                UserHandle.USER_CURRENT);
    }

    /**
     * Check if wallpaper blur is enabled.
     */
    public static boolean isWallpaperBlurEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.SETTINGS_WALLPAPER_BLUR_ENABLED, 0) == 1;
    }

    /**
     * Set wallpaper blur enabled state.
     */
    public static boolean setWallpaperBlurEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver,
                Settings.System.SETTINGS_WALLPAPER_BLUR_ENABLED, enabled ? 1 : 0);
    }

    /**
     * Get wallpaper blur radius.
     */
    public static int getWallpaperBlurRadius(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.SETTINGS_WALLPAPER_BLUR_RADIUS, 20);
    }

    /**
     * Set wallpaper blur radius.
     */
    public static boolean setWallpaperBlurRadius(Context context, int radius) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver,
                Settings.System.SETTINGS_WALLPAPER_BLUR_RADIUS, radius);
    }

    /**
     * Check if display effects are enabled.
     */
    public static boolean isDisplayEffectsEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.SETTINGS_DISPLAY_EFFECTS_ENABLED, 0) == 1;
    }

    /**
     * Set display effects enabled state.
     */
    public static boolean setDisplayEffectsEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver,
                Settings.System.SETTINGS_DISPLAY_EFFECTS_ENABLED, enabled ? 1 : 0);
    }

    /**
     * Check if dark mode is enabled.
     */
    public static boolean isDarkMode(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Check if wallpaper features are active (background enabled and dark mode).
     */
    public static boolean isWallpaperFeaturesActive(Context context) {
        return isWallpaperBackgroundEnabled(context) && isDarkMode(context);
    }
}

