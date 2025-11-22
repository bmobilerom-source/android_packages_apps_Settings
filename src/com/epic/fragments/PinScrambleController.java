/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;
import android.content.Context;
import com.android.settings.core.TogglePreferenceController;
public class PinScrambleController extends TogglePreferenceController {
    public PinScrambleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    @Override
    public boolean isChecked() {
        return PrivacySecurityHelper.isPinScrambleEnabled(mContext);
    }
    @Override
    public boolean setChecked(boolean isChecked) {
        return PrivacySecurityHelper.setPinScrambleEnabled(mContext, isChecked);
    }
    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
