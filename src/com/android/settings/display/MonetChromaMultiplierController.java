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
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;

/**
 * Controller for Monet chroma multiplier (color vibrancy)
 * Inspired by MonetCompat's chromaMultiplier feature
 */
public class MonetChromaMultiplierController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetChromaMultiplierController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SeekBarPreference) {
            SeekBarPreference seekBarPreference = (SeekBarPreference) preference;
            // Convert from 50-200 range to 0.5-2.0 multiplier
            int storedValue = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_chroma_multiplier", 100); // Default 100%
            seekBarPreference.setProgress(storedValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int progressValue = (Integer) newValue;

        // Save the multiplier value (50-200 range)
        boolean settingSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_chroma_multiplier", progressValue);

        if (settingSaved) {
            // Convert to actual multiplier (0.5-2.0)
            float multiplier = progressValue / 100.0f;

            // Apply the chroma multiplier
            // In MonetCompat, this would adjust: chroma = baseChroma * multiplier
            applyChromaMultiplier(multiplier);
        }

        return settingSaved;
    }

    private void applyChromaMultiplier(float multiplier) {
        // Force theme refresh to apply new chroma multiplier
        // In a full MonetCompat implementation, this would:
        // 1. Recalculate all Monet colors with new chroma multiplier
        // 2. Update the color palette
        // 3. Notify all apps of color changes

        try {
            // Send broadcasts to trigger theme refresh
            android.content.Intent wallpaperIntent =
                new android.content.Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            android.content.Intent configIntent =
                new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

        } catch (Exception e) {
            // Best effort implementation
        }
    }
}
