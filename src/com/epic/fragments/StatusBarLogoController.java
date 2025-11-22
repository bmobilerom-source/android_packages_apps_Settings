package com.epic.fragments;

import android.content.Context;
import android.provider.Settings;
import com.android.settings.core.TogglePreferenceController;

public class StatusBarLogoController extends TogglePreferenceController {

    public StatusBarLogoController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return StatusBarLogoHelper.isStatusBarLogoEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return StatusBarLogoHelper.setStatusBarLogoEnabled(mContext, isChecked);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
