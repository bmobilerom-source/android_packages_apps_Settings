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
 * This is a completely independent kidsafe security settings page.
 * To transfer to other ROMs:
 * 
 * 1. Copy these files:
 *    - KidsafeSecurity.java (this file)
 *    - res/xml/kidsafe_security_settings.xml
 *    - res/values/kidsafe_security_strings.xml
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
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Kidsafe Security Settings Page
 * 
 * Provides security options for kid-safe environments.
 * This page is completely independent and can be transferred to other ROMs.
 */
public class KidsafeSecurity extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "KidsafeSecurity";
    private static final String KEY_FINGERPRINT_CATEGORY = "kidsafe_security_fingerprint_category";
    private static final String KEY_SHOW_CLIPBOARD_OVERLAY = "show_clipboard_overlay";
    private static final String KEY_WINDOW_IGNORE_SECURE = "window_ignore_secure";
    private static final String KEY_POCKET_LOCK = "pocket_lock";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.kidsafe_security_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        final Context context = getActivity();

        // Check fingerprint hardware and hide category if not available
        PreferenceCategory fingerprintCategory = (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);
        if (fingerprintCategory != null && context != null) {
            FingerprintManager fingerprintManager = (FingerprintManager)
                    context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                prefScreen.removePreference(fingerprintCategory);
            }
        }

        // Initialize preference states
        updatePreferenceStates(resolver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String key = preference.getKey();

        try {
            if (KEY_SHOW_CLIPBOARD_OVERLAY.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putInt(resolver, KEY_SHOW_CLIPBOARD_OVERLAY, enabled ? 1 : 0);
                Log.d(TAG, "show_clipboard_overlay set to: " + enabled);
                return true;
            } else if (KEY_WINDOW_IGNORE_SECURE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Global.putInt(resolver, KEY_WINDOW_IGNORE_SECURE, enabled ? 1 : 0);
                Log.d(TAG, "window_ignore_secure set to: " + enabled);
                return true;
            } else if (KEY_POCKET_LOCK.equals(key)) {
                boolean enabled = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, 
                        "pocket_mode_enabled", 
                        enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "pocket_lock set to: " + enabled);
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

            // Update window ignore secure preference
            Preference windowIgnoreSecurePref = findPreference(KEY_WINDOW_IGNORE_SECURE);
            if (windowIgnoreSecurePref != null) {
                int windowIgnoreSecureEnabled = Settings.Global.getInt(resolver, KEY_WINDOW_IGNORE_SECURE, 0);
                if (windowIgnoreSecurePref instanceof androidx.preference.TwoStatePreference) {
                    ((androidx.preference.TwoStatePreference) windowIgnoreSecurePref)
                            .setChecked(windowIgnoreSecureEnabled != 0);
                }
                windowIgnoreSecurePref.setOnPreferenceChangeListener(this);
            }
            
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
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
    
    /**
     * Search index provider - completely blocks this page from search
     */
    @SearchIndexable
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.kidsafe_security_settings) {
                @Override
                public java.util.List<android.provider.SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    // Completely block kidsafe security from search
                    return null;
                }
                
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Always disable kidsafe security from search
                    return false;
                }
                
                @Override
                public java.util.List<String> getNonIndexableKeys(Context context) {
                    // Hide all preferences from search
                    java.util.List<String> keys = super.getNonIndexableKeys(context);
                    if (keys == null) {
                        keys = new java.util.ArrayList<>();
                    }
                    // Add all preference keys to block list
                    keys.add("kidsafe_security_illustration");
                    keys.add("kidsafe_security_top_intro");
                    keys.add("kidsafe_security_fingerprint_category");
                    keys.add("kidsafe_fingerprint_settings");
                    keys.add("pocket_lock");
                    keys.add("kidsafe_security_privacy_category");
                    keys.add("show_clipboard_overlay");
                    keys.add("kidsafe_security_system_category");
                    keys.add("window_ignore_secure");
                    return keys;
                }
            };
}

