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
import com.android.settings.core.TogglePreferenceController;

public class NetworkOptimizationController extends TogglePreferenceController {

    public NetworkOptimizationController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return SystemOptimizationHelper.isNetworkOptimizationEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return SystemOptimizationHelper.setNetworkOptimizationEnabled(mContext, isChecked);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
    
    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        updateSummary(preference);
    }
    
    private void updateSummary(Preference preference) {
        if (preference != null) {
            boolean enabled = isChecked();
            String networkStatus = SystemOptimizationHelper.getNetworkStatus(mContext);
            String status = enabled 
                ? "Active • Optimizing network usage • Connected: " + networkStatus
                : "Inactive • Network: " + networkStatus;
            preference.setSummary(status);
        }
    }
}

