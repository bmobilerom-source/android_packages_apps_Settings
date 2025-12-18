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

            if (currentPreset != null && enabled) {
                // Apply darker tinting for specific colors that need better contrast
                int tintedColor = getDarkerTintForSettings(currentPreset);
                if (tintedColor != 0) {
                    // Apply the darker tint for settings background
                    android.provider.Settings.System.putInt(mContext.getContentResolver(),
                            "settings_background_tint_color", tintedColor);
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

    private int getDarkerTintForSettings(String preset) {
        // Apply 40% darker tinting for specific colors that need better visibility in settings
        switch (preset) {
            case "deep_moss_green": return darkenColor(0xFF375F47, 0.4f); // Deep Moss Green - 40% darker
            case "lilac": return darkenColor(0xFFC9A5C0, 0.4f);            // Lilac - 40% darker
            case "tan": return darkenColor(0xFFCFBA8F, 0.4f);             // Tan - 40% darker
            case "pear": return darkenColor(0xFFD9DA40, 0.4f);            // Pear - 40% darker
            case "ultramarine": return darkenColor(0xFFF20BF8, 0.4f);     // Ultramarine - 40% darker
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

