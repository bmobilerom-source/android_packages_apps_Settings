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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.epic.utils.CardNavigationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended Security Settings Page
 * 
 * Provides additional granular security options beyond standard security settings.
 * This page is completely independent and can be transferred to other ROMs.
 */
public class SettingsExtendedSecurity extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "SettingsExtendedSecurity";
    private SecurityInfoHeaderController mSecurityInfoHeaderController;
    private static final String KEY_AUTH_RIPPLE_ENABLED = "auth_ripple_enabled";
    private static final String KEY_FINGERPRINT_CATEGORY = "extended_security_fingerprint_category";
    private static final String KEY_FP_SUCCESS_VIBRATE = "fp_success_vibrate";
    private static final String KEY_FP_ERROR_VIBRATE = "fp_error_vibrate";
    private static final String KEY_SHOW_CLIPBOARD_OVERLAY = "show_clipboard_overlay";
    private static final String KEY_NO_STORAGE_RESTRICT = "no_storage_restrict";
    private static final String KEY_WINDOW_IGNORE_SECURE = "window_ignore_secure";
    private static final String KEY_SECURE_LOCKSCREEN_QS_DISABLED = "secure_lockscreen_qs_disabled";
    private static final String KEY_POCKET_LOCK = "pocket_lock";
    private CardNavigationHelper mCardHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.anatolia_settings_extended_security);

        // Initialize security info header controller
        mSecurityInfoHeaderController = new SecurityInfoHeaderController(getContext());
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            mSecurityInfoHeaderController.displayPreference(screen);
        }
        
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        final Context context = getActivity();

        // Check fingerprint hardware and hide category if not available
        PreferenceCategory fingerprintCategory = (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);
        if (fingerprintCategory != null) {
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
            if (KEY_AUTH_RIPPLE_ENABLED.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.System.putInt(resolver, KEY_AUTH_RIPPLE_ENABLED, enabled ? 1 : 0);
                Log.d(TAG, "auth_ripple_enabled set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.AUTH_RIPPLE_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_FP_SUCCESS_VIBRATE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.System.putInt(resolver, KEY_FP_SUCCESS_VIBRATE, enabled ? 1 : 0);
                Log.d(TAG, "fp_success_vibrate set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.FP_VIBRATION_CHANGED");
                intent.putExtra("success_vibrate", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_FP_ERROR_VIBRATE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.System.putInt(resolver, KEY_FP_ERROR_VIBRATE, enabled ? 1 : 0);
                Log.d(TAG, "fp_error_vibrate set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.FP_VIBRATION_CHANGED");
                intent.putExtra("error_vibrate", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_SHOW_CLIPBOARD_OVERLAY.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.Secure.putInt(resolver, KEY_SHOW_CLIPBOARD_OVERLAY, enabled ? 1 : 0);
                Log.d(TAG, "show_clipboard_overlay set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.CLIPBOARD_OVERLAY_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_NO_STORAGE_RESTRICT.equals(key)) {
                boolean enabled = (Boolean) newValue;
                // Framework (ExternalStorageProvider) reads this from Settings.Global
                boolean saved = Settings.Global.putInt(
                        resolver, Settings.Global.NO_STORAGE_RESTRICT, enabled ? 1 : 0);
                Log.d(TAG, "no_storage_restrict (Global) set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework / SystemUI to pick up
                android.content.Intent intent =
                        new android.content.Intent("com.android.settings.STORAGE_RESTRICT_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_WINDOW_IGNORE_SECURE.equals(key)) {
                boolean enabled = (Boolean) newValue;
                // Framework (Window.java) reads this from Settings.Global
                boolean saved = Settings.Global.putInt(
                        resolver, Settings.Global.WINDOW_IGNORE_SECURE, enabled ? 1 : 0);
                Log.d(TAG, "window_ignore_secure (Global) set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework / SystemUI to pick up
                android.content.Intent intent =
                        new android.content.Intent("com.android.settings.WINDOW_IGNORE_SECURE_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_SECURE_LOCKSCREEN_QS_DISABLED.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.Secure.putInt(resolver, KEY_SECURE_LOCKSCREEN_QS_DISABLED, enabled ? 1 : 0);
                Log.d(TAG, "secure_lockscreen_qs_disabled set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.SECURE_LOCKSCREEN_QS_CHANGED");
                intent.putExtra("disabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_POCKET_LOCK.equals(key)) {
                boolean enabled = (Boolean) newValue;
                boolean saved = Settings.Secure.putInt(resolver, KEY_POCKET_LOCK, enabled ? 1 : 0);
                Log.d(TAG, "pocket_lock set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.POCKET_LOCK_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference: " + key, e);
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh preference states when returning to this page
        updatePreferenceStates(getActivity().getContentResolver());
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
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) authRipplePref;
                    twoStatePref.setChecked(rippleEnabled != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (authRipplePref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) authRipplePref)
                            .setDisabledByAdmin(null);
                }
                authRipplePref.setOnPreferenceChangeListener(this);
            }

            // Update fingerprint success vibration preference
            Preference fpSuccessVibratePref = findPreference(KEY_FP_SUCCESS_VIBRATE);
            if (fpSuccessVibratePref != null) {
                int fpSuccessVibrate = Settings.System.getInt(resolver, KEY_FP_SUCCESS_VIBRATE, 1);
                if (fpSuccessVibratePref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) fpSuccessVibratePref;
                    twoStatePref.setChecked(fpSuccessVibrate != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (fpSuccessVibratePref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) fpSuccessVibratePref)
                            .setDisabledByAdmin(null);
                }
                fpSuccessVibratePref.setOnPreferenceChangeListener(this);
            }

            // Update fingerprint error vibration preference
            Preference fpErrorVibratePref = findPreference(KEY_FP_ERROR_VIBRATE);
            if (fpErrorVibratePref != null) {
                int fpErrorVibrate = Settings.System.getInt(resolver, KEY_FP_ERROR_VIBRATE, 1);
                if (fpErrorVibratePref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) fpErrorVibratePref;
                    twoStatePref.setChecked(fpErrorVibrate != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (fpErrorVibratePref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) fpErrorVibratePref)
                            .setDisabledByAdmin(null);
                }
                fpErrorVibratePref.setOnPreferenceChangeListener(this);
            }

            // Update show clipboard overlay preference
            Preference clipboardOverlayPref = findPreference(KEY_SHOW_CLIPBOARD_OVERLAY);
            if (clipboardOverlayPref != null) {
                int overlayEnabled = Settings.Secure.getInt(resolver, KEY_SHOW_CLIPBOARD_OVERLAY, 1);
                if (clipboardOverlayPref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) clipboardOverlayPref;
                    twoStatePref.setChecked(overlayEnabled != 0);
                    twoStatePref.setEnabled(true);
                }
                clipboardOverlayPref.setOnPreferenceChangeListener(this);
            }

            // Update no storage restrict preference (framework uses Settings.Global)
            Preference noStorageRestrictPref = findPreference(KEY_NO_STORAGE_RESTRICT);
            if (noStorageRestrictPref != null) {
                int storageRestrictEnabled = Settings.Global.getInt(
                        resolver, Settings.Global.NO_STORAGE_RESTRICT, 0);
                if (noStorageRestrictPref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) noStorageRestrictPref;
                    twoStatePref.setChecked(storageRestrictEnabled != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (noStorageRestrictPref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) noStorageRestrictPref)
                            .setDisabledByAdmin(null);
                }
                noStorageRestrictPref.setOnPreferenceChangeListener(this);
            }

            // Update window ignore secure preference (framework uses Settings.Global)
            Preference windowIgnoreSecurePref = findPreference(KEY_WINDOW_IGNORE_SECURE);
            if (windowIgnoreSecurePref != null) {
                int windowIgnoreSecureEnabled = Settings.Global.getInt(
                        resolver, Settings.Global.WINDOW_IGNORE_SECURE, 0);
                if (windowIgnoreSecurePref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) windowIgnoreSecurePref;
                    twoStatePref.setChecked(windowIgnoreSecureEnabled != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (windowIgnoreSecurePref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) windowIgnoreSecurePref)
                            .setDisabledByAdmin(null);
                }
                windowIgnoreSecurePref.setOnPreferenceChangeListener(this);
            }

            // Update secure lockscreen qs disabled preference
            Preference secureLockscreenQsPref = findPreference(KEY_SECURE_LOCKSCREEN_QS_DISABLED);
            if (secureLockscreenQsPref != null) {
                int qsDisabled = Settings.Secure.getInt(resolver, KEY_SECURE_LOCKSCREEN_QS_DISABLED, 0);
                if (secureLockscreenQsPref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) secureLockscreenQsPref;
                    twoStatePref.setChecked(qsDisabled != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (secureLockscreenQsPref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) secureLockscreenQsPref)
                            .setDisabledByAdmin(null);
                }
                secureLockscreenQsPref.setOnPreferenceChangeListener(this);
            }

            // Update pocket lock preference
            Preference pocketLockPref = findPreference(KEY_POCKET_LOCK);
            if (pocketLockPref != null) {
                int pocketLockEnabled = Settings.Secure.getInt(resolver, KEY_POCKET_LOCK, 0);
                if (pocketLockPref instanceof androidx.preference.TwoStatePreference) {
                    androidx.preference.TwoStatePreference twoStatePref = 
                            (androidx.preference.TwoStatePreference) pocketLockPref;
                    twoStatePref.setChecked(pocketLockEnabled != 0);
                    twoStatePref.setEnabled(true);
                }
                // For RestrictedSwitchPreference, also ensure it's not restricted
                if (pocketLockPref instanceof com.android.settingslib.RestrictedSwitchPreference) {
                    ((com.android.settingslib.RestrictedSwitchPreference) pocketLockPref)
                            .setDisabledByAdmin(null);
                }
                pocketLockPref.setOnPreferenceChangeListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }

        // Initialize card navigation helper
        mCardHelper = new CardNavigationHelper(this);
    }

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup click handling for navigation cards
        mCardHelper.setupCardClickHandling(view);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

