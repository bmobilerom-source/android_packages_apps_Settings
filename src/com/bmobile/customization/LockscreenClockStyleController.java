package com.bmobile.customization;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class LockscreenClockStyleController extends BasePreferenceController {
    public static final String SETTING_KEY = "clock_style";
    private static final String KEY = "lockscreen_clock_style";

    public LockscreenClockStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public static int getDefaultValue() {
        return 1;
    }

    public static int readClockStyle(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), SETTING_KEY,
                getDefaultValue());
    }

    public static String getLabelForStyle(Context context, int style) {
        String[] entries = context.getResources().getStringArray(
                R.array.lockscreen_clock_style_entries);
        String[] values = context.getResources().getStringArray(
                R.array.lockscreen_clock_style_values);
        String target = String.valueOf(style);
        for (int i = 0; i < values.length && i < entries.length; i++) {
            if (target.equals(values[i])) {
                return entries[i];
            }
        }
        return entries.length > getDefaultValue() ? entries[getDefaultValue()] : "";
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(getLabelForStyle(mContext, readClockStyle(mContext)));
    }
}
