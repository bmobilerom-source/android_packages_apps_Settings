package com.bmobile.customization;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public abstract class AbstractIntListPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    protected AbstractIntListPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    protected abstract int getDefaultValue();

    protected abstract String getSettingKey();

    protected abstract boolean isSecure();

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
        int value = readInt(getDefaultValue());
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
            writeInt(value);
            ListPreference list = (ListPreference) preference;
            list.setValue(String.valueOf(value));
            CharSequence summary = list.getEntry();
            if (summary != null) {
                list.setSummary(summary);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int readInt(int defaultValue) {
        ContentResolver resolver = mContext.getContentResolver();
        if (isSecure()) {
            return Settings.Secure.getInt(resolver, getSettingKey(), defaultValue);
        }
        return Settings.System.getInt(resolver, getSettingKey(), defaultValue);
    }

    private void writeInt(int value) {
        ContentResolver resolver = mContext.getContentResolver();
        if (isSecure()) {
            Settings.Secure.putInt(resolver, getSettingKey(), value);
        } else {
            Settings.System.putInt(resolver, getSettingKey(), value);
        }
    }
}
