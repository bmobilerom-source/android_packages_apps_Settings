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
import com.android.settings.core.TogglePreferenceController;

public class StorageOptimizationController extends TogglePreferenceController {

    public StorageOptimizationController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return SystemOptimizationHelper.isStorageOptimizationEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return SystemOptimizationHelper.setStorageOptimizationEnabled(mContext, isChecked);
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
            long availableStorage = SystemOptimizationHelper.getAvailableStorage(mContext);
            String storageInfo = availableStorage > 0 
                ? " • Available: " + Formatter.formatShortFileSize(mContext, availableStorage)
                : "";
            String status = enabled 
                ? "Active • Optimizing storage access" + storageInfo
                : "Inactive" + storageInfo;
            preference.setSummary(status);
        }
    }
}

