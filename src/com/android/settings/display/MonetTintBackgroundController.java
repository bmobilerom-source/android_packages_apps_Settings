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
            // Get current preset
            String currentPreset = android.provider.Settings.System.getString(
                    mContext.getContentResolver(), "monet_color_preset");

            if (currentPreset != null) {
                // Always apply darker tinting for specific colors that need better contrast in settings
                // These 5 colors get automatic darker tinting regardless of toggle setting
                int tintedColor = getAutoDarkerTintForSettings(currentPreset);
                if (tintedColor != 0) {
                    // Apply the darker tint for settings background
                    android.provider.Settings.System.putInt(mContext.getContentResolver(),
                            "settings_background_tint_color", tintedColor);
                } else if (enabled) {
                    // For other colors, only apply tinting if the toggle is enabled
                    tintedColor = getDarkerTintForSettings(currentPreset);
                    if (tintedColor != 0) {
                        android.provider.Settings.System.putInt(mContext.getContentResolver(),
                                "settings_background_tint_color", tintedColor);
                    }
                }
            }

            // Send broadcast to notify system of appearance changes
            Intent intent = new Intent("android.intent.action.WALLPAPER_CHANGED");
            intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(intent);

            // Also try configuration changed broadcast
            Intent configIntent = new Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

        } catch (Exception e) {
            // Ignore exceptions in best-effort implementation
        }
    }

    private int getAutoDarkerTintForSettings(String preset) {
        // Apply 40% darker tinting AUTOMATICALLY for these 15 colors that need better visibility in settings
        // These colors always get darker tinting in settings, regardless of toggle setting
        switch (preset) {
            // Original 5 colors
            case "deep_moss_green": return darkenColor(0xFF375F47, 0.4f); // Deep Moss Green - auto 40% darker
            case "lilac": return darkenColor(0xFFC9A5C0, 0.4f);            // Lilac - auto 40% darker
            case "tan": return darkenColor(0xFFCFBA8F, 0.4f);             // Tan - auto 40% darker
            case "pear": return darkenColor(0xFFD9DA40, 0.4f);            // Pear - auto 40% darker
            case "ultramarine": return darkenColor(0xFFF20BF8, 0.4f);     // Ultramarine - auto 40% darker

            // Additional 10 colors that need darker backgrounds for better visibility
            case "cream": return darkenColor(0xFFF5F5DC, 0.4f);           // Cream - auto 40% darker
            case "canary": return darkenColor(0xFFFFE91A, 0.4f);          // Canary - auto 40% darker
            case "orange_pantone": return darkenColor(0xFFFF8C00, 0.4f);  // Orange - auto 40% darker
            case "screamin_green": return darkenColor(0xFF55FC77, 0.4f);  // Screamin Green - auto 40% darker
            case "spring_green": return darkenColor(0xFF00F891, 0.4f);    // Spring Green - auto 40% darker
            case "lime_green": return darkenColor(0xFF00D61C, 0.4f);      // Lime Green - auto 40% darker
            case "celeste": return darkenColor(0xFFB6FFFE, 0.4f);         // Celeste - auto 40% darker
            case "pastel_pink": return darkenColor(0xFFD99EB0, 0.4f);     // Pastel Pink - auto 40% darker
            case "bisque": return darkenColor(0xFFFFE8BD, 0.4f);          // Bisque - auto 40% darker
            case "alabaster": return darkenColor(0xFFFAF9F6, 0.4f);       // Alabaster - auto 40% darker

            default: return 0; // No auto tinting for other colors
        }
    }

    private int getDarkerTintForSettings(String preset) {
        // Apply additional tinting for other colors when toggle is enabled
        // These colors only get darker when the user enables the toggle
        switch (preset) {
            // Add other colors here if needed in the future
            default: return 0; // No special tinting for other colors
        }
    }

    private int darkenColor(int color, float factor) {
        // Extract RGB components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Apply darkening factor
        red = (int) (red * (1.0f - factor));
        green = (int) (green * (1.0f - factor));
        blue = (int) (blue * (1.0f - factor));

        // Ensure minimum brightness for visibility
        red = Math.max(red, 20);
        green = Math.max(green, 20);
        blue = Math.max(blue, 20);

        // Reconstruct color
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

