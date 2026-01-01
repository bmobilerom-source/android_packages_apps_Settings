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
import com.android.settings.core.TogglePreferenceController;

public class FastChargingController extends TogglePreferenceController {

    public FastChargingController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return PowerTweaksHelper.isFastChargingEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return PowerTweaksHelper.setFastChargingEnabled(mContext, isChecked);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

