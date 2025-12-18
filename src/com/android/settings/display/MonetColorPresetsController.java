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

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class MonetColorPresetsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetColorPresetsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.ListPreference) {
            androidx.preference.ListPreference listPreference = (androidx.preference.ListPreference) preference;
            String currentPreset = Settings.System.getString(
                    mContext.getContentResolver(), "monet_color_preset");

            // Default to french_violet if no preset is set
            if (currentPreset == null || currentPreset.isEmpty()) {
                currentPreset = "french_violet";
            }
            listPreference.setValue(currentPreset);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String presetName = (String) newValue;

        // Get preset seed color
        int seedColor = getPresetSeedColor(presetName);

        if (seedColor != 0) {
            // Set the system background color - this acts like changing wallpaper
            boolean colorSaved = Settings.System.putInt(mContext.getContentResolver(),
                    "monet_seed_color", seedColor);

            // Save the selected preset name for display purposes
            boolean presetSaved = Settings.System.putString(mContext.getContentResolver(),
                    "monet_color_preset", presetName);

            // Enable custom color mode
            boolean modeSaved = Settings.System.putInt(mContext.getContentResolver(),
                    "monet_preset_enabled", 1);

            boolean result = colorSaved && presetSaved && modeSaved;

            if (result) {
                // Apply the background color change immediately
                applyBackgroundColorChange(seedColor);
            }

            return result;
        }

        return false;
    }

    private void applyBackgroundColorChange(int color) {
        try {
            // Send wallpaper changed broadcast to trigger Monet recalculation
            Intent wallpaperIntent = new Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            // Send configuration changed broadcast to refresh UI
            Intent configIntent = new Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

            // Force theme refresh by briefly toggling UiMode
            UiModeManager uiModeManager = mContext.getSystemService(UiModeManager.class);
            if (uiModeManager != null) {
                int currentMode = uiModeManager.getNightMode();
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                // Brief delay to ensure processing
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                uiModeManager.setNightMode(currentMode);
            }

        } catch (Exception e) {
            // Best effort - ignore exceptions
        }
    }

    private void applyMonetChanges() {
        try {
            // Multiple approaches to ensure theme changes are applied

            // 1. Send wallpaper changed broadcast (triggers monet recalculation)
            Intent wallpaperIntent = new Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            // 2. Send theme changed broadcast
            Intent themeIntent = new Intent("android.intent.action.THEME_CHANGED");
            themeIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // 3. Send configuration changed broadcast
            Intent configIntent = new Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

            // 4. Force system UI refresh by toggling night mode briefly
            UiModeManager uiModeManager = mContext.getSystemService(UiModeManager.class);
            if (uiModeManager != null) {
                int currentNightMode = uiModeManager.getNightMode();
                // Brief toggle to force theme refresh
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                // Small delay to ensure the change is processed
                Thread.sleep(50);
                uiModeManager.setNightMode(currentNightMode);
            }
        } catch (Exception e) {
            // Ignore exceptions as this is best effort
        }
    }

    private int getPresetSeedColor(String name) {
        switch (name) {
            case "wallpaper": return 0xFF4285F4;           // Default - use wallpaper colors

            // Researched Color Collection (36 carefully selected colors)
            case "french_violet": return 0xFF8921C2;       // French Violet
            case "rose_bonbon": return 0xFFFE39A4;         // Rose Bonbon
            case "cream": return 0xFFF5F5DC;               // Cream (better cream color)
            case "turquoise": return 0xFF53E8D4;           // Turquoise
            case "vivid_sky_blue": return 0xFF25C4F8;      // Vivid Sky Blue
            case "canary": return 0xFFFFE91A;              // Canary
            case "orange_pantone": return 0xFFFF8C00;      // Orange (Pantone) - changed to dark orange
            case "off_red_rgb": return 0xFFEA1104;         // Off Red (RGB)
            case "blue_orchid": return 0xFF2F46FA;         // Blue Orchid
            case "screamin_green": return 0xFF55FC77;      // Screamin' Green
            case "russian_violet": return 0xFF2F0049;      // Russian Violet
            case "orange_crayola": return 0xFFFB7443;      // Orange (Crayola)
            case "spring_green": return 0xFF00F891;        // Spring Green
            case "lime_green": return 0xFF00D61C;          // Lime Green
            case "night": return 0xFF021307;               // Night
            case "palatinate_blue": return 0xFF0346F4;     // Palatinate Blue
            case "federal_blue": return 0xFF00005A;        // Federal Blue
            case "steel_pink": return 0xFFBF2ED5;          // Steel Pink
            case "celeste": return 0xFFB6FFFE;             // Celeste
            case "ultramarine": return 0xFFF20BF8;         // Ultramarine
            case "dark_purple": return 0xFF150390;         // Dark Purple
            case "pear": return 0xFFD9DA40;                // Pear
            case "celadon_blue": return 0xFF017DAF;        // Celadon Blue
            case "pastel_pink": return 0xFFD99EB0;         // Pastel Pink
            case "bisque": return 0xFFFFE8BD;              // Bisque
            case "brown": return 0xFF574739;               // Brown
            case "tan": return 0xFFCFBA8F;                 // Tan
            case "lilac": return 0xFFC9A5C0;               // Lilac
            case "steel_blue": return 0xFF3F99AC;          // Steel Blue
            case "deep_moss_green": return 0xFF375F47;     // Deep Moss Green
            case "dark_sea_green": return 0xFF93B285;      // Dark Sea Green
            case "alabaster": return 0xFFFAF9F6;           // Alabaster (proper off-white)
            case "quick_silver": return 0xFFA8A8A8;        // Quick Silver (proper gray)
            case "mint": return 0xFF4BAB8E;                // Mint
            case "american_blue": return 0xFF2E4370;       // American Blue

            // Additional Material Design Accent Colors
            case "red_accent": return 0xFFFF5252;           // Material Red A200
            case "purple_accent": return 0xFFE040FB;        // Material Purple A200
            case "indigo_accent": return 0xFF536DFE;        // Material Indigo A200
            case "cyan_accent": return 0xFF18FFFF;          // Material Cyan A200
            case "green_accent": return 0xFF69F0AE;         // Material Green A200
            case "yellow_accent": return 0xFFFFFF00;        // Material Yellow A200

            default: return 0xFF4285F4;                     // Default Google Blue
        }
    }
}