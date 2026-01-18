package com.epic.controllers;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class PowerOffVerifyController extends TogglePreferenceController {

    private static final String KEY_POWER_OFF_VERIFY = "power_off_verify_enabled";

    public PowerOffVerifyController(Context context, String key) {
        super(context, key);
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                "power_off_verify_enabled", 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(mContext.getContentResolver(),
                "power_off_verify_enabled", isChecked ? 1 : 0);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_POWER_OFF_VERIFY;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}