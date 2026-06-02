package com.bmobile.customization;

import android.content.Context;
import android.os.UserHandle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import lineageos.providers.LineageSettings;

public class BatteryStylePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public BatteryStylePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof ListPreference)) {
            return;
        }
        ListPreference list = (ListPreference) preference;
        int value = LineageSettings.System.getIntForUser(mContext.getContentResolver(),
                LineageSettings.System.STATUS_BAR_BATTERY_STYLE, 0, UserHandle.USER_CURRENT);
        list.setValue(String.valueOf(value));
        CharSequence summary = list.getEntry();
        if (summary != null) {
            list.setSummary(summary);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof ListPreference)) {
            return false;
        }
        try {
            int value = Integer.parseInt(String.valueOf(newValue));
            boolean ok = LineageSettings.System.putIntForUser(mContext.getContentResolver(),
                    LineageSettings.System.STATUS_BAR_BATTERY_STYLE, value, UserHandle.USER_CURRENT);
            if (ok) {
                ListPreference list = (ListPreference) preference;
                list.setValue(String.valueOf(value));
                CharSequence summary = list.getEntry();
                if (summary != null) {
                    list.setSummary(summary);
                }
            }
            return ok;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
