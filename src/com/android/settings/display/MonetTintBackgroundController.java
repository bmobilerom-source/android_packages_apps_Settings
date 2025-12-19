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

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

public class MonetTintBackgroundController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetTintBackgroundController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isChecked = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_tint_background", 0) == 1;
            switchPreference.setChecked(isChecked);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;

        // Save the setting
        boolean settingSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_tint_background", isChecked ? 1 : 0);

        // Apply the tinting effect by modifying accent colors
        if (settingSaved) {
            applyBackgroundTint(isChecked);
        }

        return settingSaved;
    }

    private void applyBackgroundTint(boolean enabled) {
        try {
            // Get current preset to get the base color
            String currentPreset = android.provider.Settings.System.getString(
                    mContext.getContentResolver(), "monet_color_preset");

            if (currentPreset == null || currentPreset.isEmpty()) {
                currentPreset = "french_violet"; // Default
            }

            // If preset is "wallpaper", tint doesn't apply (wallpaper colors are used)
            if ("wallpaper".equals(currentPreset)) {
                // Just trigger refresh - wallpaper colors will be used
                sendThemeRefreshBroadcasts();
                return;
            }

            // Get the base color for the current preset (without any tinting)
            int baseColor = getBaseColorForPreset(currentPreset);

            if (enabled) {
                // Apply tint to the base color
                int tintedColor = applyTintToBaseColor(baseColor);

                // Apply the tinted color as the new seed color
                boolean colorSaved = Settings.System.putInt(mContext.getContentResolver(),
                        "monet_seed_color", tintedColor);

                // Ensure preset mode is enabled
                Settings.System.putInt(mContext.getContentResolver(),
                        "monet_preset_enabled", 1);

                // Force theme refresh
                if (colorSaved) {
                    sendThemeRefreshBroadcasts();
                }
            } else {
                // Tint disabled - use base color without tinting
                boolean colorSaved = Settings.System.putInt(mContext.getContentResolver(),
                        "monet_seed_color", baseColor);

                // Ensure preset mode is enabled
                Settings.System.putInt(mContext.getContentResolver(),
                        "monet_preset_enabled", 1);

                // Force theme refresh
                if (colorSaved) {
                    sendThemeRefreshBroadcasts();
                }
            }

        } catch (Exception e) {
            // Ignore exceptions in best-effort implementation
        }
    }

    /**
     * Gets the base color for a preset (without any tinting applied)
     */
    private int getBaseColorForPreset(String preset) {
        // Use the same mapping as MonetColorPresetsController but without makeRicherAndDarker
        // We'll apply tint separately if needed
        switch (preset) {
            case "wallpaper": return 0xFF4285F4;
            case "french_violet": return 0xFF8921C2;
            case "rose_bonbon": return 0xFFFE39A4;
            case "vivid_sky_blue": return 0xFF25C4F8;
            case "orange_pantone": return 0xFF8B4513;
            case "off_red_rgb": return 0xFFFF0000;
            case "blue_orchid": return 0xFF2F46FA;
            case "screamin_green": return 0xFF55FC77;
            case "orange_crayola": return 0xFFFB7443;
            case "spring_green": return 0xFF00FF7F;
            case "lime_green": return 0xFF00D61C;
            case "night": return 0xFF021307;
            case "palatinate_blue": return 0xFF0346F4;
            case "federal_blue": return 0xFF00005A;
            case "steel_pink": return 0xFFBF2ED5;
            case "celeste": return 0xFFB6FFFE;
            case "ultramarine": return 0xFFF20BF8;
            case "dark_purple": return 0xFF150390;
            case "pear": return 0xFFD9DA40;
            case "pastel_pink": return 0xFFD99EB0;
            case "tan": return 0xFFCFBA8F;
            case "lilac": return 0xFFC9A5C0;
            case "steel_blue": return 0xFF3F99AC;
            case "deep_moss_green": return 0xFF375F47;
            case "dark_sea_green": return 0xFF93B285;
            case "alabaster": return 0xFFFFFFFF;
            case "american_blue": return 0xFF2E4370;
            case "red_accent": return 0xFFFF5252;
            case "purple_accent": return 0xFFE040FB;
            case "indigo_accent": return 0xFF536DFE;
            case "cyan_accent": return 0xFF18FFFF;
            case "green_accent": return 0xFF69F0AE;
            case "yellow_accent": return 0xFFFFFF00;
            default: return 0xFF4285F4;
        }
    }

    /**
     * Applies tint to a base color (same logic as in ThemeOverlayController)
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

    private void sendThemeRefreshBroadcasts() {
        // Send multiple broadcasts to ensure theme refresh
        Intent[] intents = {
            new Intent("android.intent.action.WALLPAPER_CHANGED"),
            new Intent("android.intent.action.CONFIGURATION_CHANGED"),
            new Intent("android.intent.action.THEME_CHANGED"),
            new Intent("android.settings.display.action.THEME_CHANGED")
        };

        for (Intent intent : intents) {
            intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            try {
                mContext.sendBroadcast(intent);
            } catch (Exception e) {
                // Continue with other broadcasts if one fails
            }
        }
    }

    private int getAutoDarkerTintForSettings(String preset) {
        // Apply ULTRA STRONG darker tinting AUTOMATICALLY for better visibility in dark mode
        // 5x stronger tinting for maximum contrast in dark mode settings
        switch (preset) {
            // Original 4 colors - ULTRA BOLD (88% darker for 5x stronger impact)
            case "lilac": return darkenColor(0xFFC9A5C0, 0.88f);            // Lilac - 88% darker (ultra bold)
            case "tan": return darkenColor(0xFFCFBA8F, 0.88f);             // Tan - 88% darker (ultra bold)
            case "pear": return darkenColor(0xFFD9DA40, 0.88f);            // Pear - 88% darker (ultra bold)
            case "ultramarine": return darkenColor(0xFFF20BF8, 0.88f);     // Ultramarine - 88% darker (ultra bold)

            // Additional 8 colors - MEGA BOLD (93% darker + extra vibrant for 5x stronger effect)
            case "orange_pantone": return darkenColor(makeMoreVibrant(0xFF8B4513), 0.93f);  // Orange (Pantone) - ultra vibrant + 93% darker
            case "screamin_green": return darkenColor(makeMoreVibrant(0xFF55FC77), 0.93f);  // Screamin Green - ultra vibrant + 93% darker
            case "spring_green": return darkenColor(makeMoreVibrant(0xFF00FF7F), 0.93f);    // Spring Green - ultra vibrant + 93% darker
            case "lime_green": return darkenColor(makeMoreVibrant(0xFF00D61C), 0.93f);      // Lime Green - ultra vibrant + 93% darker
            case "celeste": return darkenColor(makeMoreVibrant(0xFFB6FFFE), 0.93f);         // Celeste - ultra vibrant + 93% darker
            case "pastel_pink": return darkenColor(makeMoreVibrant(0xFFD99EB0), 0.93f);     // Pastel Pink - ultra vibrant + 93% darker

            // Last 10 colors - ULTRA VIBRANT SOLID COLORS (no darkening, maximum vibrancy for dark mode)
            case "deep_moss_green": return makeUltraVibrant(0xFF375F47);                     // Deep Moss Green - ultra vibrant solid
            case "dark_sea_green": return makeUltraVibrant(0xFF93B285);                      // Dark Sea Green - ultra vibrant solid
            case "alabaster": return makeUltraVibrant(0xFFFFFFFF);                          // Alabaster - ultra vibrant white
            case "american_blue": return makeUltraVibrant(0xFF2E4370);                       // American Blue - ultra vibrant solid
            case "red_accent": return makeUltraVibrant(0xFFFF5252);                          // Material Red A200 - ultra vibrant solid
            case "purple_accent": return makeUltraVibrant(0xFFE040FB);                       // Material Purple A200 - ultra vibrant solid
            case "indigo_accent": return makeUltraVibrant(0xFF536DFE);                        // Material Indigo A200 - ultra vibrant solid
            case "cyan_accent": return makeUltraVibrant(0xFF18FFFF);                         // Material Cyan A200 - ultra vibrant solid
            case "green_accent": return makeUltraVibrant(0xFF69F0AE);                        // Material Green A200 - ultra vibrant solid
            case "yellow_accent": return makeUltraVibrant(0xFFFFFF00);                       // Material Yellow A200 - ultra vibrant solid

            default: return 0; // No auto tinting for other colors
        }
    }

    private int getDarkerTintForSettings(String preset) {
        // Apply 40% STRONGER tinting for light mode when toggle is enabled
        // Makes colors much darker and stronger in light mode settings
        switch (preset) {
            // All colors get 40% stronger tinting in light mode for better visibility
            case "cream": return darkenColor(makeMoreVibrant(0xFFF5F5DC), 0.6f);           // Cream - 40% stronger tinting
            case "turquoise": return darkenColor(makeMoreVibrant(0xFF53E8D4), 0.6f);      // Turquoise - 40% stronger tinting
            case "vivid_sky_blue": return darkenColor(makeMoreVibrant(0xFF25C4F8), 0.6f); // Vivid Sky Blue - 40% stronger tinting
            case "rose_bonbon": return darkenColor(makeMoreVibrant(0xFFFE39A4), 0.6f);    // Rose Bonbon - 40% stronger tinting
            case "blue_orchid": return darkenColor(makeMoreVibrant(0xFF2F46FA), 0.6f);    // Blue Orchid - 40% stronger tinting
            case "russian_violet": return darkenColor(makeMoreVibrant(0xFF2F0049), 0.6f); // Russian Violet - 40% stronger tinting
            case "orange_crayola": return darkenColor(makeMoreVibrant(0xFFFB7443), 0.6f); // Orange (Crayola) - 40% stronger tinting
            case "off_red_rgb": return darkenColor(makeMoreVibrant(0xFFFF0000), 0.6f);    // Off Red (RGB) - 40% stronger tinting
            case "palatinate_blue": return darkenColor(makeMoreVibrant(0xFF0346F4), 0.6f); // Palatinate Blue - 40% stronger tinting
            case "federal_blue": return darkenColor(makeMoreVibrant(0xFF00005A), 0.6f);   // Federal Blue - 40% stronger tinting
            case "steel_pink": return darkenColor(makeMoreVibrant(0xFFBF2ED5), 0.6f);     // Steel Pink - 40% stronger tinting
            case "night": return darkenColor(makeMoreVibrant(0xFF021307), 0.6f);          // Night - 40% stronger tinting
            case "dark_purple": return darkenColor(makeMoreVibrant(0xFF150390), 0.6f);    // Dark Purple - 40% stronger tinting
            case "celadon_blue": return darkenColor(makeMoreVibrant(0xFF017DAF), 0.6f);   // Celadon Blue - 40% stronger tinting
            case "brown": return darkenColor(makeMoreVibrant(0xFF574739), 0.6f);          // Brown - 40% stronger tinting
            case "mint": return darkenColor(makeMoreVibrant(0xFF4BAB8E), 0.6f);           // Mint - 40% stronger tinting
            case "quick_silver": return darkenColor(makeMoreVibrant(0xFFA8A8A8), 0.6f);   // Quick Silver - 40% stronger tinting
            default: return darkenColor(makeMoreVibrant(getSeedColorForPreset(preset)), 0.6f); // 40% stronger for any other color
        }
    }

    private int makeMoreVibrant(int color) {
        // Extract RGB components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Increase saturation and brightness for pastel colors to make them more vibrant
        // Boost each channel by 30-40% to make them stronger and more noticeable
        red = Math.min(255, (int)(red * 1.35f));
        green = Math.min(255, (int)(green * 1.35f));
        blue = Math.min(255, (int)(blue * 1.35f));

        // Ensure full opacity for maximum visibility
        alpha = 0xFF;

        // Reconstruct color
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private int makeUltraVibrant(int color) {
        // Extract RGB components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Ultra vibrancy for dark mode - boost each channel by 40-50% for maximum impact
        red = Math.min(255, (int)(red * 1.45f));
        green = Math.min(255, (int)(green * 1.45f));
        blue = Math.min(255, (int)(blue * 1.45f));

        // Ensure full opacity for maximum visibility
        alpha = 0xFF;

        // Reconstruct color
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private int getSeedColorForPreset(String preset) {
        // Get the seed color for any preset - matches MonetColorPresetsController mapping
        switch (preset) {
            case "wallpaper": return 0xFF4285F4;           // Default - use wallpaper colors
            case "french_violet": return 0xFF8921C2;       // French Violet
            case "rose_bonbon": return 0xFFFE39A4;         // Rose Bonbon
            case "vivid_sky_blue": return 0xFF25C4F8;      // Vivid Sky Blue
            case "orange_pantone": return 0xFF8B4513;      // Orange (Pantone) - changed to proper brown
            case "off_red_rgb": return 0xFFFF0000;         // Off Red (RGB) - changed to pure red
            case "blue_orchid": return 0xFF2F46FA;         // Blue Orchid
            case "screamin_green": return 0xFF55FC77;      // Screamin' Green
            case "orange_crayola": return 0xFFFB7443;      // Orange (Crayola)
            case "spring_green": return 0xFF00FF7F;        // Spring Green - brighter/more vibrant
            case "lime_green": return 0xFF00D61C;          // Lime Green
            case "night": return 0xFF021307;               // Night
            case "palatinate_blue": return 0xFF0346F4;     // Palatinate Blue
            case "federal_blue": return 0xFF00005A;        // Federal Blue
            case "steel_pink": return 0xFFBF2ED5;          // Steel Pink
            case "celeste": return 0xFFB6FFFE;             // Celeste
            case "ultramarine": return 0xFFF20BF8;         // Ultramarine
            case "dark_purple": return 0xFF150390;         // Dark Purple
            case "pear": return 0xFFD9DA40;                // Pear
            case "pastel_pink": return 0xFFD99EB0;         // Pastel Pink
            case "tan": return 0xFFCFBA8F;                 // Tan
            case "lilac": return 0xFFC9A5C0;               // Lilac
            case "steel_blue": return 0xFF3F99AC;          // Steel Blue
            case "deep_moss_green": return 0xFF375F47;     // Deep Moss Green
            case "dark_sea_green": return 0xFF93B285;      // Dark Sea Green
            case "alabaster": return 0xFFFFFFFF;           // Alabaster - pure white
            case "american_blue": return 0xFF2E4370;       // American Blue
            case "red_accent": return 0xFFFF5252;          // Material Red A200
            case "purple_accent": return 0xFFE040FB;       // Material Purple A200
            case "indigo_accent": return 0xFF536DFE;       // Material Indigo A200
            case "cyan_accent": return 0xFF18FFFF;         // Material Cyan A200
            case "green_accent": return 0xFF69F0AE;        // Material Green A200
            case "yellow_accent": return 0xFFFFFF00;       // Material Yellow A200
            default: return 0xFF4285F4;                    // Default Google Blue
        }
    }

    private int darkenColor(int color, float factor) {
        // Extract RGB components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Apply darkening factor - now much stronger for bold effect
        red = (int) (red * (1.0f - factor));
        green = (int) (green * (1.0f - factor));
        blue = (int) (blue * (1.0f - factor));

        // Ensure minimum brightness for visibility (lower minimum for richer, darker colors)
        red = Math.max(red, 15);
        green = Math.max(green, 15);
        blue = Math.max(blue, 15);

        // Ensure full opacity (0xFF = 255 = fully opaque) for maximum visibility
        alpha = 0xFF;

        // Reconstruct color
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

