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

public class BiometricTimeoutController extends TogglePreferenceController {

    public BiometricTimeoutController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return SecurityFeaturesHelper.isBiometricTimeoutEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = SecurityFeaturesHelper.setBiometricTimeoutEnabled(mContext, isChecked);
        if (result) {
            String message = isChecked ? 
                "Biometric unlock enabled (30 second timeout)" :
                "Biometric unlock disabled";
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            boolean enabled = isChecked();
            preference.setSummary((CharSequence)(enabled ?
                mContext.getString(R.string.biometric_timeout_summary_on) :
                mContext.getString(R.string.biometric_timeout_summary_off)));
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

