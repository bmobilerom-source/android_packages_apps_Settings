/*
 * Copyright (C) 2024 The LineageOS Project
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

package com.android.settings.theme;

import android.content.Context;
import android.os.UserHandle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;

public class CustomThemePreferenceController extends BasePreferenceController implements Preference.OnPreferenceChangeListener {

    private ListPreference mListPreference;
    private ListPreference mPastelColorPreference;
    private ListPreference mAnimatedVideoPreference;

    public CustomThemePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            mListPreference = (ListPreference) preference;
            mListPreference.setOnPreferenceChangeListener(this);
            int currentTheme = CustomThemeHelper.getCurrentTheme(mContext);
            mListPreference.setValue(String.valueOf(currentTheme));

            // Set summary to current theme name
            CharSequence[] entries = mListPreference.getEntries();
            if (entries != null && currentTheme >= 0 && currentTheme < entries.length) {
                mListPreference.setSummary(entries[currentTheme]);
            }
        }
        updatePreferenceVisibility();
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        // Get references to other preferences for controlling visibility
        mPastelColorPreference = (ListPreference) screen.findPreference("pastel_color_theme");
        mAnimatedVideoPreference = (ListPreference) screen.findPreference("animated_theme_video");

        if (mPastelColorPreference != null) {
            mPastelColorPreference.setOnPreferenceChangeListener(this);
        }
        if (mAnimatedVideoPreference != null) {
            mAnimatedVideoPreference.setOnPreferenceChangeListener(this);
        }

        updatePreferenceVisibility();
    }

    private void updatePreferenceVisibility() {
        int currentTheme = CustomThemeHelper.getCurrentTheme(mContext);

        // Show pastel color dropdown only when pastel themes are available
        if (mPastelColorPreference != null) {
            boolean showPastelColors = CustomThemeHelper.isPastelTheme(currentTheme) ||
                    currentTheme == CustomThemeHelper.THEME_EXPRESSIVE;
            mPastelColorPreference.setVisible(showPastelColors);

            if (showPastelColors && CustomThemeHelper.isPastelTheme(currentTheme)) {
                mPastelColorPreference.setValue(String.valueOf(currentTheme));
                mPastelColorPreference.setSummary(CustomThemeHelper.getThemeName(mContext, currentTheme));
            }
        }

        // Show animated video dropdown only for animated theme
        if (mAnimatedVideoPreference != null) {
            mAnimatedVideoPreference.setVisible(currentTheme == CustomThemeHelper.THEME_ANIMATED);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (key.equals(getPreferenceKey())) {
            try {
                int themeValue = Integer.parseInt((String) newValue);
                Log.d("CustomThemePreferenceController", "Setting theme value: " + themeValue);
                CustomThemeHelper.setCurrentTheme(mContext, themeValue);
                Log.d("CustomThemePreferenceController", "Theme set, current theme: " + CustomThemeHelper.getCurrentTheme(mContext));

                // Update summary immediately
                if (mListPreference != null) {
                    CharSequence[] entries = mListPreference.getEntries();
                    if (entries != null && themeValue >= 0 && themeValue < entries.length) {
                        mListPreference.setSummary(entries[themeValue]);
                    }
                }

                // Update visibility of other preferences
                updatePreferenceVisibility();
                return true;

            } catch (NumberFormatException e) {
                Log.e("CustomThemePreferenceController", "NumberFormatException parsing theme value: " + newValue);
                return false;
            }

        } else if (key.equals("pastel_color_theme")) {
            try {
                int pastelThemeValue = Integer.parseInt((String) newValue);
                Log.d("CustomThemePreferenceController", "Setting pastel theme value: " + pastelThemeValue);

                // Set the pastel theme as the current theme
                CustomThemeHelper.setCurrentTheme(mContext, pastelThemeValue);

                // Update both main theme dropdown and pastel summary
                if (mListPreference != null) {
                    mListPreference.setValue(String.valueOf(pastelThemeValue));
                    mListPreference.setSummary(CustomThemeHelper.getThemeName(mContext, pastelThemeValue));
                }
                if (mPastelColorPreference != null) {
                    mPastelColorPreference.setSummary(CustomThemeHelper.getThemeName(mContext, pastelThemeValue));
                }

                return true;

            } catch (NumberFormatException e) {
                Log.e("CustomThemePreferenceController", "NumberFormatException parsing pastel theme value: " + newValue);
                return false;
            }

        } else if (key.equals("animated_theme_video")) {
            // Handle animated video selection
            Log.d("CustomThemePreferenceController", "Animated video selected: " + newValue);
            // Store the animated video preference
            // Implementation depends on how animated videos are handled
            return true;
        }

        return false;
    }
}
