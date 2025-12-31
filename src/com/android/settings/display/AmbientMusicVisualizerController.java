/*
 * Copyright (C) 2025 BashaMobile
 * Copyright (C) 2025 LineageOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.settings.display;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Ambient Music Visualizer preference
 */
public class AmbientMusicVisualizerController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AmbientMusicVisualizerController";
    private static final String KEY_AMBIENT_MUSIC_VISUALIZER = "ambient_music_visualizer";

    // Valid range for visualizer modes (0-4: Spectrum, Waveform, Particle, Circle, Bars)
    private static final int MIN_VISUALIZER_MODE = 0;
    private static final int MAX_VISUALIZER_MODE = 4;
    private static final int DEFAULT_VISUALIZER_MODE = 0;

    public AmbientMusicVisualizerController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    /**
     * Validates and clamps visualizer mode value to safe range
     */
    private int validateVisualizerMode(int value) {
        return Math.max(MIN_VISUALIZER_MODE, Math.min(value, MAX_VISUALIZER_MODE));
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            ContentResolver resolver = mContext.getContentResolver();
            int currentValue = Settings.System.getInt(resolver, KEY_AMBIENT_MUSIC_VISUALIZER, DEFAULT_VISUALIZER_MODE);

            // Validate and clamp the value to prevent security issues
            int validatedValue = validateVisualizerMode(currentValue);

            // If the stored value was invalid, correct it
            if (currentValue != validatedValue) {
                Log.w(TAG, "Invalid visualizer mode value detected: " + currentValue + ", clamping to: " + validatedValue);
                Settings.System.putInt(resolver, KEY_AMBIENT_MUSIC_VISUALIZER, validatedValue);
            }

            listPreference.setValue(String.valueOf(validatedValue));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof String) {
            try {
                int modeValue = Integer.parseInt((String) newValue);

                // Validate the new value before accepting it
                int validatedValue = validateVisualizerMode(modeValue);

                if (modeValue != validatedValue) {
                    Log.w(TAG, "Attempted to set invalid visualizer mode: " + modeValue + ", using validated value: " + validatedValue);
                }

                ContentResolver resolver = mContext.getContentResolver();
                return Settings.System.putInt(resolver, KEY_AMBIENT_MUSIC_VISUALIZER, validatedValue);

            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid visualizer mode value format: " + newValue, e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false; // Let the ListPreference handle the click
    }

    @Override
    public String getPreferenceKey() {
        return KEY_AMBIENT_MUSIC_VISUALIZER;
    }
}
