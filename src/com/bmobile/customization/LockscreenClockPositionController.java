package com.bmobile.customization;

import android.content.Context;

public class LockscreenClockPositionController extends AbstractIntListPreferenceController {
    private static final String KEY = "clock_position";

    public LockscreenClockPositionController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    protected int getDefaultValue() {
        return 1;
    }

    @Override
    protected String getSettingKey() {
        return KEY;
    }

    @Override
    protected boolean isSecure() {
        return true;
    }
}
