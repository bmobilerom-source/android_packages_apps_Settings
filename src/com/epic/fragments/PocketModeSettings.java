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
 */

package com.epic.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Pocket Mode Settings Page
 * 
 * Provides comprehensive pocket mode configuration to prevent accidental touches
 * when device is in pocket or bag. Includes options to disable various wake gestures
 * and authentication methods when pocket mode is active.
 */
@SearchIndexable
public class PocketModeSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PocketModeSettings";
    private static final String KEY_POCKET_LOCK = "pocket_lock";
    private static final String KEY_ALWAYS_ON_POCKET_MODE = "always_on_pocket_mode";
    private static final String KEY_DISABLE_DT2W = "pocket_mode_disable_dt2w";
    private static final String KEY_DISABLE_FINGERPRINT = "pocket_mode_disable_fingerprint";
    private static final String KEY_DISABLE_LIFT_TO_WAKE = "pocket_mode_disable_lift_to_wake";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pocket_mode_settings);
        
        final ContentResolver resolver = getActivity().getContentResolver();
        updatePreferenceStates(resolver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String key = preference.getKey();

        try {
            if (KEY_POCKET_LOCK.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "pocket_mode_enabled", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "pocket_lock set to: " + enabled);
                return true;
            } else if (KEY_ALWAYS_ON_POCKET_MODE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "always_on_pocket_mode_enabled", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "always_on_pocket_mode set to: " + enabled);
                return true;
            } else if (KEY_DISABLE_DT2W.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "pocket_mode_disable_dt2w", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "pocket_mode_disable_dt2w set to: " + enabled);
                return true;
            } else if (KEY_DISABLE_FINGERPRINT.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "pocket_mode_disable_fingerprint", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "pocket_mode_disable_fingerprint set to: " + enabled);
                return true;
            } else if (KEY_DISABLE_LIFT_TO_WAKE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "pocket_mode_disable_lift_to_wake", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "pocket_mode_disable_lift_to_wake set to: " + enabled);
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
            // Update pocket lock preference
            Preference pocketLockPref = findPreference(KEY_POCKET_LOCK);
            if (pocketLockPref != null) {
                int pocketLockEnabled = Settings.Secure.getIntForUser(resolver, 
                        "pocket_mode_enabled", 
                        0, 
                        UserHandle.USER_CURRENT);
                if (pocketLockPref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) pocketLockPref)
                            .setChecked(pocketLockEnabled != 0);
                }
                pocketLockPref.setOnPreferenceChangeListener(this);
            }
            
            // Update always-on pocket mode preference
            Preference alwaysOnPocketModePref = findPreference(KEY_ALWAYS_ON_POCKET_MODE);
            if (alwaysOnPocketModePref != null) {
                int alwaysOnEnabled = Settings.Secure.getIntForUser(resolver, 
                        "always_on_pocket_mode_enabled", 
                        0, 
                        UserHandle.USER_CURRENT);
                if (alwaysOnPocketModePref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) alwaysOnPocketModePref)
                            .setChecked(alwaysOnEnabled != 0);
                }
                alwaysOnPocketModePref.setOnPreferenceChangeListener(this);
            }
            
            // Update disable double tap to wake preference
            Preference disableDt2wPref = findPreference(KEY_DISABLE_DT2W);
            if (disableDt2wPref != null) {
                int disableDt2wEnabled = Settings.Secure.getIntForUser(resolver, 
                        "pocket_mode_disable_dt2w", 
                        1, 
                        UserHandle.USER_CURRENT);
                if (disableDt2wPref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) disableDt2wPref)
                            .setChecked(disableDt2wEnabled != 0);
                }
                disableDt2wPref.setOnPreferenceChangeListener(this);
            }
            
            // Update disable fingerprint preference
            Preference disableFingerprintPref = findPreference(KEY_DISABLE_FINGERPRINT);
            if (disableFingerprintPref != null) {
                int disableFingerprintEnabled = Settings.Secure.getIntForUser(resolver, 
                        "pocket_mode_disable_fingerprint", 
                        1, 
                        UserHandle.USER_CURRENT);
                if (disableFingerprintPref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) disableFingerprintPref)
                            .setChecked(disableFingerprintEnabled != 0);
                }
                disableFingerprintPref.setOnPreferenceChangeListener(this);
            }
            
            // Update disable lift to wake preference
            Preference disableLiftToWakePref = findPreference(KEY_DISABLE_LIFT_TO_WAKE);
            if (disableLiftToWakePref != null) {
                int disableLiftToWakeEnabled = Settings.Secure.getIntForUser(resolver, 
                        "pocket_mode_disable_lift_to_wake", 
                        1, 
                        UserHandle.USER_CURRENT);
                if (disableLiftToWakePref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) disableLiftToWakePref)
                            .setChecked(disableLiftToWakeEnabled != 0);
                }
                disableLiftToWakePref.setOnPreferenceChangeListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
    
    /**
     * Search index provider
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.pocket_mode_settings) {
                @Override
                public java.util.List<String> getNonIndexableKeys(Context context) {
                    java.util.List<String> keys = super.getNonIndexableKeys(context);
                    if (keys == null) {
                        keys = new java.util.ArrayList<>();
                    }
                    keys.add("pocket_mode_illustration");
                    keys.add("pocket_mode_intro");
                    return keys;
                }
            };
}

