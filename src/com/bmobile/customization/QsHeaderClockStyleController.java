package com.bmobile.customization;

import android.content.Context;

public class QsHeaderClockStyleController extends AbstractIntListPreferenceController {
    private static final String KEY = "qs_header_clock_style";

    public QsHeaderClockStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    protected int getDefaultValue() {
        return 2;
    }

    @Override
    protected String getSettingKey() {
        return KEY;
    }

    @Override
    protected boolean isSecure() {
        return false;
    }
}
