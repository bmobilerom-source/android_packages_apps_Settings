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
    public void displayPreference(androidx.preference.PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            // Ensure SeekBarPreference has a valid layout with required views
            if (preference instanceof SeekBarPreference) {
                SeekBarPreference seekBarPreference = (SeekBarPreference) preference;
                // Use adaptive seekbar card layout which has all required views
                seekBarPreference.setLayoutResource(com.android.settings.R.layout.adaptive_preference_card_seekbar);
            }
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SeekBarPreference) {
            SeekBarPreference seekBarPreference = (SeekBarPreference) preference;
            // Get stored value (50-400 range, default 325%)
            int storedValue = Settings.Secure.getInt(mContext.getContentResolver(),
                    "monet_chroma_multiplier", 325);
            seekBarPreference.setProgress(storedValue);

            // Update summary to show current multiplier
            float multiplier = storedValue / 100.0f;
            preference.setSummary(String.format("%.1fx vibrancy", multiplier));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int progressValue = (Integer) newValue;

        // Save to secure settings for theme-related preferences
        boolean settingSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                "monet_chroma_multiplier", progressValue);

        if (settingSaved) {
            // Notify content resolver of the change to trigger theme update
            mContext.getContentResolver().notifyChange(
                    Settings.Secure.getUriFor("monet_chroma_multiplier"), null);

            // Update summary immediately
            float multiplier = progressValue / 100.0f;
            preference.setSummary(String.format("%.1fx vibrancy", multiplier));
        }

        return settingSaved;
    }

    private void applyChromaMultiplier(float multiplier) {
        try {
            // Apply chroma multiplier by updating system theme properties
            // This simulates MonetCompat behavior by triggering theme refresh

            // Send theme change broadcast to trigger UI refresh
            android.content.Intent themeIntent = new android.content.Intent("android.intent.action.THEME_CHANGED");
            themeIntent.putExtra("monet_chroma_multiplier", multiplier);
            themeIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // Also send configuration change to refresh system UI
            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

            // Force activity recreation for settings app
            if (mContext instanceof android.app.Activity) {
                ((android.app.Activity) mContext).recreate();
            }

        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("MonetChromaMultiplier", "Failed to apply chroma multiplier", e);
        }
    }
}
