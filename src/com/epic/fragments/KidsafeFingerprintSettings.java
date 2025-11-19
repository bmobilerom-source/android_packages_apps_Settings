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
 * This is a completely independent kidsafe fingerprint management page.
 * To transfer to other ROMs, copy:
 * - KidsafeFingerprintSettings.java (this file)
 * - res/xml/kidsafe_fingerprint_settings.xml
 * - res/values/kidsafe_fingerprint_strings.xml
 * - KidsafeFingerprintHelper.java
 * 
 * Update package names if needed (currently com.epic.fragments)
 * 
 * Ensure these dependencies exist:
 * - androidx.preference.PreferenceFragmentCompat
 * - com.android.settings.SettingsPreferenceFragment
 * - android.hardware.fingerprint.FingerprintManager
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kidsafe Fingerprint Settings Page
 * 
 * Independent fingerprint management page for kidsafe fingerprints.
 * Allows selecting which enrolled fingerprints (excluding master fingerprints)
 * should be designated as kidsafe fingerprints.
 * 
 * Hierarchy:
 * - Master fingerprints (set on BMobile Fingerprint page) - highest level
 * - Kidsafe fingerprints (set here) - lower level, can be overridden by master
 */
public class KidsafeFingerprintSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "KidsafeFingerprint";
    
    private static final String KEY_KIDSAFE_FINGERPRINT_ENROLL = "kidsafe_fingerprint_enroll";
    private static final String KEY_KIDSAFE_FINGERPRINT_MANAGE = "kidsafe_fingerprint_manage";
    private static final String KEY_KIDSAFE_FINGERPRINT_CATEGORY = "kidsafe_fingerprint_category";
    
    private FingerprintManager mFingerprintManager;
    private Preference mEnrollPreference;
    private Preference mManagePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.kidsafe_fingerprint_settings);
        
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        
        // Initialize FingerprintManager
        Context context = getActivity();
        if (context != null) {
            mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
        }
        
        // Check if fingerprint hardware is available
        PreferenceCategory fingerprintCategory = 
                (PreferenceCategory) findPreference(KEY_KIDSAFE_FINGERPRINT_CATEGORY);
        if (fingerprintCategory != null) {
            if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
                prefScreen.removePreference(fingerprintCategory);
                return;
            }
        }
        
        // Initialize preferences
        mEnrollPreference = findPreference(KEY_KIDSAFE_FINGERPRINT_ENROLL);
        mManagePreference = findPreference(KEY_KIDSAFE_FINGERPRINT_MANAGE);
        
        // Update preference states
        updatePreferenceStates();
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
            if (KEY_KIDSAFE_FINGERPRINT_ENROLL.equals(key)) {
                // Launch fingerprint enrollment
                launchFingerprintEnrollment();
                return true;
            } else if (KEY_KIDSAFE_FINGERPRINT_MANAGE.equals(key)) {
                // Launch kidsafe fingerprint management
                launchKidsafeFingerprintManagement();
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
            
            // Get available fingerprints (excluding master fingerprints)
            List<Fingerprint> availableFingerprints = 
                    KidsafeFingerprintHelper.getAvailableFingerprintsForKidsafe(getActivity());
            int availableCount = availableFingerprints != null ? availableFingerprints.size() : 0;
            
            // Get kidsafe fingerprints
            List<Fingerprint> kidsafeFingerprints = 
                    KidsafeFingerprintHelper.getKidsafeFingerprints(getActivity());
            int kidsafeCount = kidsafeFingerprints != null ? kidsafeFingerprints.size() : 0;
            
            // Update enroll preference summary
            if (mEnrollPreference != null) {
                if (hasEnrolled && availableCount > 0) {
                    mEnrollPreference.setSummary(getString(R.string.kidsafe_fingerprint_enroll_summary_existing));
                } else if (hasEnrolled) {
                    mEnrollPreference.setSummary(getString(R.string.kidsafe_fingerprint_enroll_summary_all_master));
                } else {
                    mEnrollPreference.setSummary(getString(R.string.kidsafe_fingerprint_enroll_summary_new));
                }
            }
            
            // Update manage preference summary and visibility
            if (mManagePreference != null) {
                if (kidsafeCount > 0) {
                    mManagePreference.setSummary(getString(R.string.kidsafe_fingerprint_manage_summary, kidsafeCount));
                    mManagePreference.setVisible(true);
                } else {
                    mManagePreference.setVisible(false);
                }
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch fingerprint enrollment", e);
        }
    }
    
    /**
     * Launch kidsafe fingerprint management
     * Shows a dialog/list to select which fingerprints should be kidsafe
     */
    private void launchKidsafeFingerprintManagement() {
        try {
            Context context = getActivity();
            if (context == null) {
                return;
            }
            
            // Get available fingerprints (excluding master)
            List<Fingerprint> availableFingerprints = 
                    KidsafeFingerprintHelper.getAvailableFingerprintsForKidsafe(context);
            
            if (availableFingerprints == null || availableFingerprints.isEmpty()) {
                Log.w(TAG, "No available fingerprints for kidsafe selection");
                return;
            }
            
            // Get currently selected kidsafe fingerprints
            Set<Integer> currentKidsafeIds = 
                    KidsafeFingerprintHelper.getKidsafeFingerprintIds(context);
            
            // Create a dialog or fragment to allow selecting fingerprints
            // For now, we'll use a simple approach: show a multi-select dialog
            showKidsafeFingerprintSelectionDialog(availableFingerprints, currentKidsafeIds);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch kidsafe fingerprint management", e);
        }
    }
    
    /**
     * Show dialog to select which fingerprints should be kidsafe
     */
    private void showKidsafeFingerprintSelectionDialog(
            List<Fingerprint> availableFingerprints, Set<Integer> currentKidsafeIds) {
        try {
            Context context = getActivity();
            if (context == null) {
                return;
            }
            
            // Create array of fingerprint names
            CharSequence[] fingerprintNames = new CharSequence[availableFingerprints.size()];
            boolean[] checkedItems = new boolean[availableFingerprints.size()];
            
            for (int i = 0; i < availableFingerprints.size(); i++) {
                Fingerprint fingerprint = availableFingerprints.get(i);
                fingerprintNames[i] = fingerprint.getName() != null ? 
                        fingerprint.getName().toString() : 
                        getString(R.string.fingerprint_default_name, i + 1);
                checkedItems[i] = currentKidsafeIds.contains(fingerprint.getBiometricId());
            }
            
            // Show multi-select dialog
            new android.app.AlertDialog.Builder(context)
                    .setTitle(R.string.kidsafe_fingerprint_select_title)
                    .setMultiChoiceItems(fingerprintNames, checkedItems, 
                            (dialog, which, isChecked) -> {
                                // Update checked state
                                checkedItems[which] = isChecked;
                            })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Save selected fingerprints
                        Set<Integer> selectedIds = new HashSet<>();
                        for (int i = 0; i < availableFingerprints.size(); i++) {
                            if (checkedItems[i]) {
                                selectedIds.add(availableFingerprints.get(i).getBiometricId());
                            }
                        }
                        KidsafeFingerprintHelper.setKidsafeFingerprintIds(context, selectedIds);
                        updatePreferenceStates();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
                    
        } catch (Exception e) {
            Log.e(TAG, "Error showing kidsafe fingerprint selection dialog", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

