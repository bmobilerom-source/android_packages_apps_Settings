/*
 * Copyright (C) 2025 BashaMobile
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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This is a completely independent extended security settings page.
 * To transfer to other ROMs:
 * 
 * 1. Copy these files:
 *    - SettingsExtendedSecurity.java (this file)
 *    - res/xml/anatolia_settings_extended_security.xml
 *    - res/values/extended_security_strings.xml
 * 
 * 2. Update package name if needed (currently com.epic.fragments)
 * 
 * 3. Ensure these dependencies exist in target ROM:
 *    - androidx.preference.PreferenceFragmentCompat
 *    - com.android.settings.SettingsPreferenceFragment
 *    - com.android.settings.preferences.ui.AdaptiveRestrictedSwitchPreference
 *    - android.provider.Settings.System
 * 
 * 4. If AdaptiveRestrictedSwitchPreference path differs, update imports
 * 
 * 5. Update Settings.System key names if they differ in target ROM
 * 
 * That's it! The page is completely self-contained and ROM-independent.
 */

package com.epic.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Extended Security Settings Page
 * 
 * Provides additional granular security options beyond standard security settings.
 * This page is completely independent and can be transferred to other ROMs.
 */
public class SettingsExtendedSecurity extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "SettingsExtendedSecurity";
    private static final String KEY_AUTH_RIPPLE_ENABLED = "auth_ripple_enabled";
    private static final String KEY_SHOW_CLIPBOARD_OVERLAY = "show_clipboard_overlay";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.anatolia_settings_extended_security);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        // Initialize preference states
        updatePreferenceStates(resolver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String key = preference.getKey();

        try {
            if (KEY_AUTH_RIPPLE_ENABLED.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.System.putInt(resolver, KEY_AUTH_RIPPLE_ENABLED, enabled ? 1 : 0);
                Log.d(TAG, "auth_ripple_enabled set to: " + enabled);
                return true;
            } else if (KEY_SHOW_CLIPBOARD_OVERLAY.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putInt(resolver, KEY_SHOW_CLIPBOARD_OVERLAY, enabled ? 1 : 0);
                Log.d(TAG, "show_clipboard_overlay set to: " + enabled);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference: " + key, e);
        }

        return false;
    }

    /**
     * Update all preference states based on current system settings
     */
    private void updatePreferenceStates(ContentResolver resolver) {
        try {
            // Update auth ripple enabled preference
            Preference authRipplePref = findPreference(KEY_AUTH_RIPPLE_ENABLED);
            if (authRipplePref != null) {
                int rippleEnabled = Settings.System.getInt(resolver, KEY_AUTH_RIPPLE_ENABLED, 1);
                if (authRipplePref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) authRipplePref)
                            .setChecked(rippleEnabled != 0);
                }
                authRipplePref.setOnPreferenceChangeListener(this);
            }

            // Update show clipboard overlay preference
            Preference clipboardOverlayPref = findPreference(KEY_SHOW_CLIPBOARD_OVERLAY);
            if (clipboardOverlayPref != null) {
                int overlayEnabled = Settings.Secure.getInt(resolver, KEY_SHOW_CLIPBOARD_OVERLAY, 1);
                if (clipboardOverlayPref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) clipboardOverlayPref)
                            .setChecked(overlayEnabled != 0);
                }
                clipboardOverlayPref.setOnPreferenceChangeListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

