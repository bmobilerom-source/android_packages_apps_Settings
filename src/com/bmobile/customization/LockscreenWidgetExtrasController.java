package com.bmobile.customization;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import java.util.HashSet;
import java.util.Set;

public class LockscreenWidgetExtrasController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY = "lockscreen_widgets_extras";
    private static final String DEFAULT = "RINGER,TORCH,MEDIA,TIMER,QRSCANNER";

    public LockscreenWidgetExtrasController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof MultiSelectListPreference)) {
            return;
        }
        MultiSelectListPreference list = (MultiSelectListPreference) preference;
        String stored = Settings.System.getString(mContext.getContentResolver(), KEY);
        if (stored == null || stored.trim().isEmpty()) {
            stored = DEFAULT;
            Settings.System.putString(mContext.getContentResolver(), KEY, stored);
        }
        list.setValues(readValues());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof MultiSelectListPreference) || !(newValue instanceof Set)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Set<String> values = (Set<String>) newValue;
        String csv = TextUtils.join(",", values);
        return Settings.System.putString(mContext.getContentResolver(), KEY, csv);
    }

    private Set<String> readValues() {
        String stored = Settings.System.getString(mContext.getContentResolver(), KEY);
        if (TextUtils.isEmpty(stored)) {
            stored = DEFAULT;
        }
        Set<String> result = new HashSet<>();
        for (String part : stored.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
