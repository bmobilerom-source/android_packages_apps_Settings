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

/**
 * Controller for Monochromatic Mode toggle.
 * When enabled, this overrides all color styles and forces monochromatic theming.
 */
public class MonetMonochromaticController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetMonochromaticController(Context context, String preferenceKey) {
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
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_monochromatic_mode", 0) == 1;
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;

        // Save the setting
        boolean settingSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_monochromatic_mode", isEnabled ? 1 : 0);

        if (settingSaved) {
            // Apply the monochromatic mode
            applyMonochromaticMode(isEnabled);
        }

        return settingSaved;
    }

    private void applyMonochromaticMode(boolean enabled) {
        try {
            if (enabled) {
                // Force monochromatic style by overriding the current style
                boolean styleSaved = Settings.System.putString(mContext.getContentResolver(),
                        "monet_color_style", "monochromatic");

                if (styleSaved) {
                    // Send theme refresh broadcasts
                    sendThemeRefreshBroadcasts();
                }
            } else {
                // When disabled, reset to default tonal_spot style
                Settings.System.putString(mContext.getContentResolver(),
                        "monet_color_style", "tonal_spot");

                // Send theme refresh broadcasts
                sendThemeRefreshBroadcasts();
            }
        } catch (Exception e) {
            // Ignore exceptions in best-effort implementation
        }
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
}
