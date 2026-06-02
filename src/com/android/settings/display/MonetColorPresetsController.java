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
    public void displayPreference(androidx.preference.PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.ListPreference) {
            androidx.preference.ListPreference listPreference = (androidx.preference.ListPreference) preference;
            String currentPreset = Settings.Secure.getString(
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

        // Handle "wallpaper" preset - disable preset mode to use wallpaper colors
        if ("wallpaper".equals(presetName)) {
            // Disable preset mode to use wallpaper colors
            boolean modeSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                    "monet_preset_enabled", 0);
            
            // Clear preset name
            boolean presetSaved = Settings.Secure.putString(mContext.getContentResolver(),
                    "monet_color_preset", "wallpaper");

            if (modeSaved && presetSaved) {
                // Trigger theme refresh to use wallpaper colors
                applyBackgroundColorChange(0);
            }

            return modeSaved && presetSaved;
        }

        // Get preset seed color (base color, not tinted)
        int baseSeedColor = getPresetSeedColor(presetName);

        if (baseSeedColor != 0) {
            // Check if tint is currently enabled - we need to preserve that state
            boolean tintWasEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    "monet_tint_background", 0) == 1;

            // Save the selected preset name FIRST (before applying any color changes)
            boolean presetSaved = Settings.Secure.putString(mContext.getContentResolver(),
                    "monet_color_preset", presetName);

            // If tint is enabled, we need to apply tint to the new base color
            // Otherwise, use the base color directly
            int finalSeedColor = baseSeedColor;
            if (tintWasEnabled) {
                // Apply tint to the new base color
                finalSeedColor = applyTintToBaseColor(baseSeedColor);
            }

            // Set the system background color
            boolean colorSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                    "monet_seed_color", finalSeedColor);

            // Enable custom color mode
            boolean modeSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                    "monet_preset_enabled", 1);

            boolean result = colorSaved && presetSaved && modeSaved;

            if (result) {
                // Apply the background color change immediately
                applyBackgroundColorChange(finalSeedColor);
            }

            return result;
        }

        return false;
    }

    /**
     * Applies tint to a base color (used when preset changes but tint is enabled)
     */
    private int applyTintToBaseColor(int baseColor) {
        // Extract RGB components
        int alpha = (baseColor >> 24) & 0xFF;
        int red = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8) & 0xFF;
        int blue = baseColor & 0xFF;

        // Make richer: boost saturation by 25%
        red = Math.min(255, (int)(red * 1.25f));
        green = Math.min(255, (int)(green * 1.25f));
        blue = Math.min(255, (int)(blue * 1.25f));

        // Make darker: reduce brightness by 40%
        red = (int)(red * 0.60f);
        green = (int)(green * 0.60f);
        blue = (int)(blue * 0.60f);

        // Ensure minimum brightness
        red = Math.max(red, 15);
        green = Math.max(green, 15);
        blue = Math.max(blue, 15);

        // Ensure full opacity
        alpha = 0xFF;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
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
        int baseColor;
        switch (name) {
            case "wallpaper": baseColor = 0xFF4285F4; break;           // Default - use wallpaper colors

            // Researched Color Collection (36 carefully selected colors)
            case "french_violet": baseColor = 0xFF8921C2; break;       // French Violet
            case "rose_bonbon": baseColor = 0xFFFE39A4; break;         // Rose Bonbon
            case "vivid_sky_blue": baseColor = 0xFF25C4F8; break;      // Vivid Sky Blue
            case "orange_pantone": baseColor = 0xFF8B4513; break;      // Orange (Pantone) - changed to proper brown
            case "off_red_rgb": baseColor = 0xFFFF0000; break;         // Off Red (RGB) - changed to pure red
            case "blue_orchid": baseColor = 0xFF2F46FA; break;         // Blue Orchid
            case "screamin_green": baseColor = 0xFF55FC77; break;      // Screamin' Green
            case "orange_crayola": baseColor = 0xFFFB7443; break;      // Orange (Crayola)
            case "spring_green": baseColor = 0xFF00FF7F; break;        // Spring Green - brighter/more vibrant
            case "lime_green": baseColor = 0xFF00D61C; break;          // Lime Green
            case "night": baseColor = 0xFF021307; break;               // Night
            case "palatinate_blue": baseColor = 0xFF0346F4; break;     // Palatinate Blue
            case "federal_blue": baseColor = 0xFF00005A; break;        // Federal Blue
            case "steel_pink": baseColor = 0xFFBF2ED5; break;          // Steel Pink
            case "celeste": baseColor = 0xFFB6FFFE; break;             // Celeste
            case "ultramarine": baseColor = 0xFFF20BF8; break;         // Ultramarine
            case "dark_purple": baseColor = 0xFF150390; break;         // Dark Purple
            case "pear": baseColor = 0xFFD9DA40; break;                // Pear
            case "pastel_pink": baseColor = 0xFFD99EB0; break;         // Pastel Pink
            case "tan": baseColor = 0xFFCFBA8F; break;                 // Tan
            case "lilac": baseColor = 0xFFC9A5C0; break;               // Lilac
            case "steel_blue": baseColor = 0xFF3F99AC; break;          // Steel Blue
            case "deep_moss_green": baseColor = 0xFF375F47; break;     // Deep Moss Green
            case "dark_sea_green": baseColor = 0xFF93B285; break;      // Dark Sea Green
            case "alabaster": baseColor = 0xFFFFFFFF; break;           // Alabaster - pure white
            case "american_blue": baseColor = 0xFF2E4370; break;       // American Blue

            // Additional Material Design Accent Colors
            case "red_accent": baseColor = 0xFFFF5252; break;           // Material Red A200
            case "purple_accent": baseColor = 0xFFE040FB; break;        // Material Purple A200
            case "indigo_accent": baseColor = 0xFF536DFE; break;        // Material Indigo A200
            case "cyan_accent": baseColor = 0xFF18FFFF; break;          // Material Cyan A200
            case "green_accent": baseColor = 0xFF69F0AE; break;         // Material Green A200
            case "yellow_accent": baseColor = 0xFFFFFF00; break;        // Material Yellow A200

            default: baseColor = 0xFF4285F4; break;                     // Default Google Blue
        }
        
        // Make all colors 10% richer and darker
        return makeRicherAndDarker(baseColor);
    }

    /**
     * Makes a color 20% richer (more saturated) and 20% darker for better visibility
     * Stronger than before to match pitch black theme intensity
     */
    private int makeRicherAndDarker(int color) {
        // Extract RGB components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Make richer: increase saturation by boosting channels
        // Boost each channel by 20% for stronger richness (like pitch black theme)
        red = Math.min(255, (int)(red * 1.20f));
        green = Math.min(255, (int)(green * 1.20f));
        blue = Math.min(255, (int)(blue * 1.20f));

        // Make darker: reduce brightness by 20% for stronger contrast
        red = (int)(red * 0.80f);
        green = (int)(green * 0.80f);
        blue = (int)(blue * 0.80f);

        // Ensure minimum brightness for visibility
        red = Math.max(red, 12);
        green = Math.max(green, 12);
        blue = Math.max(blue, 12);

        // Ensure full opacity for maximum impact
        alpha = 0xFF;

        // Reconstruct color - now stronger and darker, similar to pitch black theme
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}