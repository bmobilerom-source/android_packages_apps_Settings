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
import android.graphics.Color;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import java.util.Random;

public class MonetRandomPresetController extends BasePreferenceController {

    private final Random mRandom = new Random();

    public MonetRandomPresetController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(mContext.getString(
                com.android.settings.R.string.monet_random_preset_summary));
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        // Generate a random vibrant color for Monet seed
        int randomColor = generateRandomVibrantColor();

        // Apply the random seed color
        boolean seedSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_seed_color", randomColor);
        boolean presetSaved = Settings.System.putString(mContext.getContentResolver(),
                "monet_color_preset", "random");
        boolean modeSaved = Settings.System.putInt(mContext.getContentResolver(),
                "monet_preset_enabled", 1);

        // Update summary to show the generated color
        String hexColor = String.format("#%06X", (0xFFFFFF & randomColor));
        preference.setSummary("Generated: " + hexColor);

        return seedSaved && presetSaved && modeSaved;
    }

    private int generateRandomVibrantColor() {
        // Generate vibrant colors by ensuring good saturation and brightness
        float hue = mRandom.nextFloat() * 360f; // Random hue 0-360
        float saturation = 0.7f + mRandom.nextFloat() * 0.3f; // 70-100% saturation
        float brightness = 0.5f + mRandom.nextFloat() * 0.4f; // 50-90% brightness

        // Convert HSV to RGB using Android's Color class
        float[] hsv = {hue, saturation, brightness};
        return Color.HSVToColor(hsv);
    }
}
