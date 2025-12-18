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

import com.android.settings.core.BasePreferenceController;

public class MonetCustomSeedController extends BasePreferenceController {

    public MonetCustomSeedController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        int currentSeed = Settings.System.getInt(
                mContext.getContentResolver(), "monet_seed_color", 0xFF4285F4);
        String hexColor = String.format("#%06X", (0xFFFFFF & currentSeed));
        preference.setSummary(mContext.getString(
                com.android.settings.R.string.monet_custom_seed_summary) + " (Current: " + hexColor + ")");
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        // Open color picker or custom color selection
        // For now, cycle through some preset colors on tap
        int currentSeed = Settings.System.getInt(
                mContext.getContentResolver(), "monet_seed_color", 0xFF4285F4);

        int[] customColors = {
                0xFFE91E63, // Pink
                0xFF9C27B0, // Purple
                0xFF3F51B5, // Indigo
                0xFF2196F3, // Blue
                0xFF00BCD4, // Cyan
                0xFF4CAF50, // Green
                0xFF8BC34A, // Light Green
                0xFFFFEB3B, // Yellow
                0xFFFF9800, // Orange
                0xFFFF5722, // Deep Orange
                0xFFF44336  // Red
        };

        int currentIndex = -1;
        for (int i = 0; i < customColors.length; i++) {
            if (customColors[i] == currentSeed) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % customColors.length;
        int newSeed = customColors[nextIndex];

        // Apply the new seed color
        boolean seedSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_seed_color", newSeed);
        boolean presetSaved = Settings.System.putString(mContext.getContentResolver(),
                "monet_color_preset", "custom");
        boolean modeSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_preset_enabled", 1);

        // Update UI
        updateState(preference);

        return seedSaved && presetSaved && modeSaved;
    }
}
