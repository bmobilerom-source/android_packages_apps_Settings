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
import android.text.format.Formatter;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;

public class OptimizationStatusController extends BasePreferenceController {

    public OptimizationStatusController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference == null) {
            return;
        }
        
        // Get optimization status
        String status = SystemOptimizationHelper.getOptimizationStatus(mContext);
        
        // Get available memory for display
        long availableMem = SystemOptimizationHelper.getAvailableMemory(mContext);
        String memInfo = "";
        if (availableMem > 0) {
            memInfo = "\nAvailable Memory: " + Formatter.formatShortFileSize(mContext, availableMem);
        }
        
        // Get thermal status
        int thermalStatus = SystemOptimizationHelper.getCurrentThermalStatus(mContext);
        String thermalInfo = getThermalStatusString(thermalStatus);
        
        preference.setSummary(status + memInfo + thermalInfo);
    }


    private String getThermalStatusString(int status) {
        switch (status) {
            case android.os.PowerManager.THERMAL_STATUS_NONE:
                return "\nThermal Status: Normal";
            case android.os.PowerManager.THERMAL_STATUS_LIGHT:
                return "\nThermal Status: Light";
            case android.os.PowerManager.THERMAL_STATUS_MODERATE:
                return "\nThermal Status: Moderate";
            case android.os.PowerManager.THERMAL_STATUS_SEVERE:
                return "\nThermal Status: Severe";
            case android.os.PowerManager.THERMAL_STATUS_CRITICAL:
                return "\nThermal Status: Critical";
            case android.os.PowerManager.THERMAL_STATUS_EMERGENCY:
                return "\nThermal Status: Emergency";
            case android.os.PowerManager.THERMAL_STATUS_SHUTDOWN:
                return "\nThermal Status: Shutdown";
            default:
                return "";
        }
    }
}

