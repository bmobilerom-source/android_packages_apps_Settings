package com.bmobile.customization;

import android.content.Context;

public class LockscreenClockStyleController extends AbstractIntListPreferenceController {
    private static final String KEY = "clock_style";

    public LockscreenClockStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    protected int getDefaultValue() {
        return 0;
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
