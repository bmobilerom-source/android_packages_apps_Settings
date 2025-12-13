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
import android.widget.Toast;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class MockLocationDetectionController extends TogglePreferenceController {

    public MockLocationDetectionController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // This feature cannot be changed on user builds
        return DISABLED_FOR_USER;
    }

    @Override
    public boolean isChecked() {
        return SecurityFeaturesHelper.isMockLocationDetectionEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = SecurityFeaturesHelper.setMockLocationDetectionEnabled(mContext, isChecked);
        if (!result) {
            Toast.makeText(mContext, R.string.mock_location_detection_warning, Toast.LENGTH_LONG).show();
        }
        return result;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(R.string.mock_location_detection_warning);
            preference.setEnabled(false);
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

