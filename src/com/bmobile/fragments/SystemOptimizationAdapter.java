/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.bmobile.fragments;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

/**
 * Adapter for managing System Optimization preferences.
 * Updates preference states and provides status information.
 */
public class SystemOptimizationAdapter {
    private final Context mContext;
    private PreferenceScreen mScreen;

    public SystemOptimizationAdapter(Context context) {
        mContext = context;
    }

    public void setPreferenceScreen(PreferenceScreen screen) {
        mScreen = screen;
    }

    /**
     * Update all optimization preference states
     * Only features that work out of the box without kernel/framework changes
     */
    public void updateAllPreferenceStates() {
        if (mScreen == null) {
            return;
        }

        updatePreferenceState("background_app_limits",
                SystemOptimizationHelper.isBackgroundAppLimitsEnabled(mContext));
        updatePreferenceState("network_optimization",
                SystemOptimizationHelper.isNetworkOptimizationEnabled(mContext));
        updatePreferenceState("battery_optimization",
                SystemOptimizationHelper.isBatteryOptimizationEnabled(mContext));
        updatePreferenceState("storage_optimization",
                SystemOptimizationHelper.isStorageOptimizationEnabled(mContext));
        updatePreferenceState("thermal_throttling",
                SystemOptimizationHelper.isThermalThrottlingEnabled(mContext));
        // Removed features (require kernel/framework):
        // - memory_optimization
        // - cpu_governor_optimization
        // - io_scheduler_optimization
        // - zram_optimization
        // - performance_mode
    }

    private void updatePreferenceState(String key, boolean enabled) {
        Preference pref = mScreen.findPreference(key);
        if (pref instanceof TwoStatePreference) {
            ((TwoStatePreference) pref).setChecked(enabled);
        }
    }

    /**
     * Get optimization status summary
     */
    public String getOptimizationStatus() {
        return SystemOptimizationHelper.getOptimizationStatus(mContext);
    }
}

