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

package com.android.settings.display.darkmode;

import android.content.ContentResolver;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

import lineageos.providers.LineageSettings;

/**
 * Controller for Espresso Theme switch preference
 * Maps to SYSTEM_CUSTOM_THEME = 4 (Espresso)
 */
public class SystemThemeEspressoPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SystemThemeEspressoController";
    private static final String KEY_SYSTEM_THEME_ESPRESSO = "system_theme_espresso";
    private static final int THEME_ESPRESSO = 4; // Maps to CustomThemeHelper.THEME_ESPRESSO

    public SystemThemeEspressoPreferenceController(Context context, String preferenceKey) {
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
            // Check if this theme is currently active
            int currentTheme = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.SYSTEM_CUSTOM_THEME,
                    0,
                    UserHandle.USER_CURRENT);
            boolean isActive = (currentTheme == THEME_ESPRESSO);
            switchPreference.setChecked(isActive);
            // Set the preference change listener
            switchPreference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        ContentResolver resolver = mContext.getContentResolver();

        if (enabled) {
            // Disable other theme switches
            LineageSettings.Secure.putIntForUser(resolver, "system_theme_vivid", 0, UserHandle.USER_CURRENT);
            LineageSettings.Secure.putIntForUser(resolver, "system_theme_snowpaint", 0, UserHandle.USER_CURRENT);

            // Set SYSTEM_CUSTOM_THEME to Espresso (4)
            Settings.Secure.putIntForUser(resolver,
                    Settings.Secure.SYSTEM_CUSTOM_THEME,
                    THEME_ESPRESSO,
                    UserHandle.USER_CURRENT);

            // Notify ContentResolver to trigger ThemeOverlayController
            resolver.notifyChange(
                    Settings.Secure.getUriFor(Settings.Secure.SYSTEM_CUSTOM_THEME),
                    null);

            Log.d(TAG, "Espresso theme enabled, SYSTEM_CUSTOM_THEME set to " + THEME_ESPRESSO);
        } else {
            // Disable theme - set to default (0)
            Settings.Secure.putIntForUser(resolver,
                    Settings.Secure.SYSTEM_CUSTOM_THEME,
                    0,
                    UserHandle.USER_CURRENT);

            // Notify ContentResolver
            resolver.notifyChange(
                    Settings.Secure.getUriFor(Settings.Secure.SYSTEM_CUSTOM_THEME),
                    null);

            Log.d(TAG, "Espresso theme disabled, SYSTEM_CUSTOM_THEME set to 0");
        }

        // Update preference state to reflect change
        updateState(preference);
        
        // Refresh other theme preferences to show correct state
        refreshOtherThemePreferences(preference);
        
        return true;
    }
    
    private void refreshOtherThemePreferences(Preference currentPreference) {
        if (currentPreference == null) return;
        PreferenceScreen screen = currentPreference.getPreferenceManager().getPreferenceScreen();
        if (screen == null) return;
        
        Preference vividPref = screen.findPreference("system_theme_vivid");
        Preference snowpaintPref = screen.findPreference("system_theme_snowpaint");
        
        if (vividPref instanceof SwitchPreference) {
            int currentTheme = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.SYSTEM_CUSTOM_THEME,
                    0,
                    UserHandle.USER_CURRENT);
            ((SwitchPreference) vividPref).setChecked(currentTheme == 2);
        }
        if (snowpaintPref instanceof SwitchPreference) {
            int currentTheme = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.SYSTEM_CUSTOM_THEME,
                    0,
                    UserHandle.USER_CURRENT);
            ((SwitchPreference) snowpaintPref).setChecked(currentTheme == 3);
        }
    }
}

