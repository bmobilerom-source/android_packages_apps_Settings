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
 * Controller for AOD Clock Style preference
 */
public class AODClockStyleController extends BasePreferenceController {

    private static final String TAG = "AODClockStyleController";
    private static final String KEY_AOD_CLOCK_STYLE = "aod_clock_style";

    // Valid range for clock styles (0-4: Classic, Morphing, Particle, Liquid, Geometric)
    private static final int MIN_CLOCK_STYLE = 0;
    private static final int MAX_CLOCK_STYLE = 4;
    private static final int DEFAULT_CLOCK_STYLE = 0;

    public AODClockStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    /**
     * Validates and clamps clock style value to safe range
     */
    private int validateClockStyle(int value) {
        return Math.max(MIN_CLOCK_STYLE, Math.min(value, MAX_CLOCK_STYLE));
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            // Use secure getter method
            int currentValue = getSecureIntSetting(KEY_AOD_CLOCK_STYLE, DEFAULT_CLOCK_STYLE,
                                                  MIN_CLOCK_STYLE, MAX_CLOCK_STYLE);

            listPreference.setValue(String.valueOf(currentValue));
        }
    }

    /**
     * Securely gets an integer value from Settings with validation
     */
    private int getSecureIntSetting(String key, int defaultValue, int minValue, int maxValue) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            int value = Settings.System.getInt(resolver, key, defaultValue);

            // Validate the retrieved value
            if (value < minValue || value > maxValue) {
                Log.w(TAG, "Retrieved invalid value for " + key + ": " + value + ", using default: " + defaultValue);
                // Attempt to correct the stored value
                Settings.System.putInt(resolver, key, defaultValue);
                return defaultValue;
            }

            return value;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving setting " + key, e);
            return defaultValue;
        }
    }

    /**
     * Securely sets an integer value to Settings with validation
     */
    private boolean setSecureIntSetting(String key, int value, int minValue, int maxValue) {
        try {
            // Validate the value before storing
            int validatedValue = Math.max(minValue, Math.min(value, maxValue));

            if (value != validatedValue) {
                Log.w(TAG, "Attempted to set invalid value for " + key + ": " + value + ", corrected to: " + validatedValue);
            }

            ContentResolver resolver = mContext.getContentResolver();
            return Settings.System.putInt(resolver, key, validatedValue);
        } catch (Exception e) {
            Log.e(TAG, "Error setting " + key + " to " + value, e);
            return false;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof String) {
            try {
                int styleValue = Integer.parseInt((String) newValue);

                // Use secure setting method
                return setSecureIntSetting(KEY_AOD_CLOCK_STYLE, styleValue, MIN_CLOCK_STYLE, MAX_CLOCK_STYLE);

            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid clock style value format: " + newValue, e);
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
        return KEY_AOD_CLOCK_STYLE;
    }
}
