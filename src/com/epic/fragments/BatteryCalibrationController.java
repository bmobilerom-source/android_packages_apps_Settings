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
import com.android.settings.core.TogglePreferenceController;

public class BatteryCalibrationController extends TogglePreferenceController {

    public BatteryCalibrationController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return PowerTweaksHelper.isBatteryCalibrationEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return PowerTweaksHelper.setBatteryCalibrationEnabled(mContext, isChecked);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

