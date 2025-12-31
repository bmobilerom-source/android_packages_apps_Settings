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

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for applying ColorBlendr-style color modifications
 * Applies saturation, lightness, and pitch black to colors
 */
public class ColorBlendrHelper {
    private static final String TAG = "ColorBlendrHelper";
    private static final boolean DEBUG = false;
    
    private Context mContext;
    private WallpaperManager mWallpaperManager;
    
    public ColorBlendrHelper(Context context) {
        mContext = context;
        mWallpaperManager = WallpaperManager.getInstance(context);
    }
    
    /**
     * Apply color modifications based on saturation, lightness, and pitch black settings
     */
    public void applyModifications(int accentSaturation, int backgroundSaturation,
            int backgroundLightness, boolean pitchBlack, int manualColor) {
        
        if (DEBUG) {
            Log.d(TAG, "Applying modifications: accentSat=" + accentSaturation +
                ", bgSat=" + backgroundSaturation + ", bgLight=" + backgroundLightness +
                ", pitchBlack=" + pitchBlack + ", manualColor=" + manualColor);
        }
        
        // Get current wallpaper colors
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        android.app.WallpaperColors wallpaperColors = wallpaperManager.getWallpaperColors(
            WallpaperManager.FLAG_SYSTEM);
        
        if (wallpaperColors == null) {
            Log.w(TAG, "Wallpaper colors not available");
            return;
        }
        
        // Extract base colors
        int primaryColor = wallpaperColors.getPrimaryColor().toArgb();
        int secondaryColor = wallpaperColors.getSecondaryColor() != null ?
            wallpaperColors.getSecondaryColor().toArgb() : primaryColor;
        int tertiaryColor = wallpaperColors.getTertiaryColor() != null ?
            wallpaperColors.getTertiaryColor().toArgb() : primaryColor;
        
        // Apply manual color override if set
        if (manualColor != 0) {
            primaryColor = manualColor;
        }
        
        // Apply accent saturation
        primaryColor = applySaturation(primaryColor, accentSaturation);
        secondaryColor = applySaturation(secondaryColor, accentSaturation);
        tertiaryColor = applySaturation(tertiaryColor, accentSaturation);
        
        // Store modified colors for SystemUI to read
        saveModifiedColors(primaryColor, secondaryColor, tertiaryColor,
            backgroundSaturation, backgroundLightness, pitchBlack);
    }
    
    /**
     * Apply saturation adjustment to a color
     * @param color Original color
     * @param saturationPercent Saturation percentage (0-200%, 100% = no change)
     * @return Modified color
     */
    private int applySaturation(int color, int saturationPercent) {
        if (saturationPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust saturation (0-200% range, 100% = no change)
        float saturationMultiplier = saturationPercent / 100f;
        hsv[1] = Math.max(0f, Math.min(1f, hsv[1] * saturationMultiplier));
        
        return Color.HSVToColor(hsv);
    }
    
    /**
     * Apply lightness adjustment to a color
     * @param color Original color
     * @param lightnessPercent Lightness percentage (0-200%, 100% = no change)
     * @return Modified color
     */
    private int applyLightness(int color, int lightnessPercent) {
        if (lightnessPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust lightness/value (0-200% range, 100% = no change)
        float lightnessMultiplier = lightnessPercent / 100f;
        hsv[2] = Math.max(0f, Math.min(1f, hsv[2] * lightnessMultiplier));
        
        return Color.HSVToColor(hsv);
    }
    
    /**
     * Save modified colors to Settings for SystemUI to read
     */
    private void saveModifiedColors(int primaryColor, int secondaryColor, int tertiaryColor,
            int backgroundSaturation, int backgroundLightness, boolean pitchBlack) {
        
        ContentResolver resolver = mContext.getContentResolver();
        
        // Store color values as hex strings
        String primaryHex = String.format("#%08X", primaryColor);
        String secondaryHex = String.format("#%08X", secondaryColor);
        String tertiaryHex = String.format("#%08X", tertiaryColor);
        
        // Store in Settings.System for SystemUI to read
        Settings.System.putStringForUser(resolver,
            "monet_colorblendr_primary", primaryHex, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
            "monet_colorblendr_secondary", secondaryHex, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
            "monet_colorblendr_tertiary", tertiaryHex, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
            "monet_colorblendr_bg_saturation", backgroundSaturation, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
            "monet_colorblendr_bg_lightness", backgroundLightness, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
            "monet_colorblendr_pitch_black", pitchBlack ? 1 : 0, UserHandle.USER_CURRENT);
        
        if (DEBUG) {
            Log.d(TAG, "Saved modified colors: primary=" + primaryHex +
                ", secondary=" + secondaryHex + ", tertiary=" + tertiaryHex);
        }
    }
}

