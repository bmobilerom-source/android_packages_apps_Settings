/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.epic.fragments;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

/**
 * Adapter for managing Security Features preferences.
 * Updates preference states and provides status information.
 */
public class SecurityFeaturesAdapter {
    private final Context mContext;
    private PreferenceScreen mScreen;

    public SecurityFeaturesAdapter(Context context) {
        mContext = context;
    }

    public void setPreferenceScreen(PreferenceScreen screen) {
        mScreen = screen;
    }

    /**
     * Update all security feature preference states
     */
    public void updateAllPreferenceStates() {
        if (mScreen == null) {
            return;
        }

        updatePreferenceState("network_permission_control",
                SecurityFeaturesHelper.isNetworkPermissionControlEnabled(mContext));
        updatePreferenceState("app_hardening",
                SecurityFeaturesHelper.isAppHardeningEnabled(mContext));
        updatePreferenceState("sensor_access_control",
                SecurityFeaturesHelper.isSensorAccessControlEnabled(mContext));
        updatePreferenceState("location_access_control",
                SecurityFeaturesHelper.isLocationAccessControlEnabled(mContext));
        updatePreferenceState("microphone_access_control",
                SecurityFeaturesHelper.isMicrophoneAccessControlEnabled(mContext));
    }

    private void updatePreferenceState(String key, boolean enabled) {
        Preference pref = mScreen.findPreference(key);
        if (pref instanceof TwoStatePreference) {
            ((TwoStatePreference) pref).setChecked(enabled);
        }
    }

    /**
     * Get security features status summary
     */
    public String getSecurityFeaturesStatus() {
        return SecurityFeaturesHelper.getSecurityFeaturesStatus(mContext);
    }
}

