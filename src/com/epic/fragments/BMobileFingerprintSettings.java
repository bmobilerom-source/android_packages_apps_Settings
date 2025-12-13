/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This is a completely independent fingerprint management page.
 * To transfer to other ROMs, copy:
 * - BMobileFingerprintSettings.java (this file)
 * - res/xml/bmobile_fingerprint_settings.xml
 * - res/values/bmobile_fingerprint_strings.xml
 * - res/drawable/ic_bmobile_fingerprint*.xml (all fingerprint drawables)
 * 
 * Update package names if needed (currently com.epic.fragments)
 * 
 * Ensure these dependencies exist:
 * - androidx.preference.PreferenceFragmentCompat
 * - com.android.settings.SettingsPreferenceFragment
 * - com.android.settings.preferences.ui.AdaptiveSwitchPreference
 * - android.hardware.fingerprint.FingerprintManager
 * - android.provider.Settings.System
 */

package com.epic.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.biometrics.fingerprint.FingerprintEnrollIntroduction;
import com.android.settings.biometrics.fingerprint.FingerprintSettings;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

/**
 * BMobile Fingerprint Settings Page
 * 
 * Independent fingerprint management page that allows:
 * - Enrolling new fingerprints
 * - Viewing enrolled fingerprints
 * - Managing fingerprint settings
 * - Accessing fingerprint unlock options
 */
public class BMobileFingerprintSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "BMobileFingerprint";
    
    private static final String KEY_FINGERPRINT_ENROLL = "fingerprint_enroll";
    private static final String KEY_FINGERPRINT_MANAGE = "fingerprint_manage";
    private static final String KEY_FINGERPRINT_UNLOCK = "fingerprint_unlock";
    private static final String KEY_FINGERPRINT_TOOLS = "fingerprint_tools";
    private static final String KEY_FINGERPRINT_CATEGORY = "fingerprint_category";
    
    private FingerprintManager mFingerprintManager;
    private Preference mEnrollPreference;
    private Preference mManagePreference;
    private Preference mUnlockPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ensure theme backgrounds are applied
        ensureThemeBackgrounds();
        
        addPreferencesFromResource(R.xml.bmobile_fingerprint_settings);
        
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        
        // Initialize FingerprintManager
        Context context = getActivity();
        if (context != null) {
            mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
        }
        
        // Check if fingerprint hardware is available
        PreferenceCategory fingerprintCategory = 
                (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);
        if (fingerprintCategory != null) {
            if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
                prefScreen.removePreference(fingerprintCategory);
                return;
            }
        }
        
        // Initialize preferences
        mEnrollPreference = findPreference(KEY_FINGERPRINT_ENROLL);
        mManagePreference = findPreference(KEY_FINGERPRINT_MANAGE);
        mUnlockPreference = findPreference(KEY_FINGERPRINT_UNLOCK);
        
        // Update preference states
        updatePreferenceStates();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Ensure theme backgrounds are applied after view creation
        ensureThemeBackgrounds();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceStates();
    }
    
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        
        try {
            if (KEY_FINGERPRINT_ENROLL.equals(key)) {
                // Launch fingerprint enrollment
                launchFingerprintEnrollment();
                return true;
            } else if (KEY_FINGERPRINT_MANAGE.equals(key)) {
                // Launch fingerprint management (FingerprintSettings)
                launchFingerprintManagement();
                return true;
            } else if (KEY_FINGERPRINT_UNLOCK.equals(key)) {
                // Launch fingerprint unlock settings
                launchFingerprintUnlock();
                return true;
            } else if (KEY_FINGERPRINT_TOOLS.equals(key)) {
                // Show fingerprint tools bottom sheet
                // TODO: Implement FingerprintToolsBottomSheet
                // FingerprintToolsBottomSheet bottomSheet = FingerprintToolsBottomSheet.newInstance();
                // if (getFragmentManager() != null) {
                //     bottomSheet.show(getFragmentManager(), "FingerprintToolsBottomSheet");
                // }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling preference click: " + key, e);
        }
        
        return super.onPreferenceTreeClick(preference);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Handle preference changes if needed
        return false;
    }
    
    /**
     * Update preference states based on current fingerprint status
     */
    private void updatePreferenceStates() {
        if (mFingerprintManager == null) {
            return;
        }
        
        try {
            int userId = UserHandle.myUserId();
            boolean hasEnrolled = mFingerprintManager.hasEnrolledFingerprints(userId);
            List<Fingerprint> fingerprints = mFingerprintManager.getEnrolledFingerprints(userId);
            int enrolledCount = fingerprints != null ? fingerprints.size() : 0;
            
            // Update enroll preference summary
            if (mEnrollPreference != null) {
                if (hasEnrolled) {
                    mEnrollPreference.setSummary(getString(R.string.bmobile_fingerprint_enroll_summary_existing));
                } else {
                    mEnrollPreference.setSummary(getString(R.string.bmobile_fingerprint_enroll_summary_new));
                }
            }
            
            // Update manage preference summary and visibility
            if (mManagePreference != null) {
                if (hasEnrolled && enrolledCount > 0) {
                    mManagePreference.setSummary(getString(R.string.bmobile_fingerprint_manage_summary, enrolledCount));
                    mManagePreference.setVisible(true);
                } else {
                    mManagePreference.setVisible(false);
                }
            }
            
            // Update unlock preference visibility
            if (mUnlockPreference != null) {
                mUnlockPreference.setVisible(hasEnrolled);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }
    }
    
    /**
     * Launch fingerprint enrollment
     */
    private void launchFingerprintEnrollment() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.biometrics.fingerprint.FingerprintEnrollIntroduction");
            intent.putExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
            // Add flag to prevent re-entry loops
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch fingerprint enrollment", e);
            // Fallback to suggestion activity
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.biometrics.fingerprint.FingerprintEnrollSuggestionActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to launch fingerprint suggestion activity", ex);
            }
        }
    }
    
    /**
     * Launch fingerprint management (view/edit enrolled fingerprints)
     * Uses SubSettingLauncher to properly handle navigation and prevent PIN loops
     */
    private void launchFingerprintManagement() {
        try {
            // Use SubSettingLauncher to launch the new fingerprint settings fragment
            // This avoids the PIN loop issue by properly handling the navigation flow
            new SubSettingLauncher(getContext())
                    .setDestination("com.android.settings.biometrics.fingerprint2.ui.settings.fragment.FingerprintSettingsV2Fragment")
                    .setTitleRes(R.string.security_settings_fingerprint_preference_title)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .setArguments(createFingerprintManagementArgs())
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch fingerprint management via SubSettingLauncher", e);
            // Fallback to old method
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.biometrics.fingerprint.FingerprintSettings");
                intent.putExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
                // Add flags to prevent re-entry loops
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to launch fingerprint management via Intent", ex);
            }
        }
    }
    
    /**
     * Create arguments bundle for fingerprint management
     * This helps prevent PIN loops by properly initializing the fragment
     */
    private Bundle createFingerprintManagementArgs() {
        Bundle args = new Bundle();
        args.putInt(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        // Add flag to indicate we're coming from BMobile settings
        args.putBoolean("from_bmobile_settings", true);
        return args;
    }
    
    /**
     * Launch fingerprint unlock settings
     * Uses SubSettingLauncher to properly handle navigation and prevent PIN loops
     */
    private void launchFingerprintUnlock() {
        try {
            // Use SubSettingLauncher to launch the new fingerprint settings fragment
            new SubSettingLauncher(getContext())
                    .setDestination("com.android.settings.biometrics.fingerprint2.ui.settings.fragment.FingerprintSettingsV2Fragment")
                    .setTitleRes(R.string.security_settings_fingerprint_preference_title)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .setArguments(createFingerprintManagementArgs())
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch fingerprint unlock via SubSettingLauncher", e);
            // Fallback to old method
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.biometrics.fingerprint.FingerprintSettings");
                intent.putExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
                // Add flags to prevent re-entry loops
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to launch fingerprint unlock via Intent", ex);
            }
        }
    }
    
    /**
     * Ensures custom theme backgrounds are applied to this fragment.
     */
    private void ensureThemeBackgrounds() {
        try {
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            // TODO: Implement adaptive theme background
            // android.view.View rootView = activity.findViewById(android.R.id.content);
            // if (rootView instanceof android.view.ViewGroup) {
            //     android.view.ViewGroup rootGroup = (android.view.ViewGroup) rootView;
            //     if (rootGroup.findViewById(R.id.theme_background) != null) {
            //         return;
            //     }
            //
            //     com.android.settings.preferences.ui.AdaptiveThemeBackgroundView themeView =
            //             new com.android.settings.preferences.ui.AdaptiveThemeBackgroundView(activity);
            //     themeView.setId(R.id.theme_background);
            //     themeView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            //     int insertIndex = (rootGroup.findViewById(R.id.wallpaper_background) != null) ? 1 : 0;
            //     rootGroup.addView(themeView, insertIndex, new android.view.ViewGroup.LayoutParams(
            //             android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            //             android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            // }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error adding theme background", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}


