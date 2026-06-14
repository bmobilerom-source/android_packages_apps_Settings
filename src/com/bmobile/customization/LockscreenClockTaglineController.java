package com.bmobile.customization;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Edits the tagline line shown on lockscreen clocks that expose {@code @id/summary}.
 */
public class LockscreenClockTaglineController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String SETTING_KEY = "lockscreen_clock_tagline";

    /** Keep in sync with {@code ClockStyle.usesTagline} in SystemUI. */
    private static boolean usesTagline(int style) {
        return style == 4   // Orchid
                || style == 7   // Lily
                || style == 8   // Fern
                || style == 14; // Azalea
    }

    public LockscreenClockTaglineController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        int style = LockscreenClockStyleController.readClockStyle(mContext);
        return usesTagline(style) ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof EditTextPreference)) {
            return;
        }
        EditTextPreference edit = (EditTextPreference) preference;
        String value = Settings.Secure.getString(mContext.getContentResolver(), SETTING_KEY);
        if (TextUtils.isEmpty(value)) {
            edit.setText("");
            edit.setSummary(mContext.getString(
                    com.android.settings.R.string.lockscreen_clock_tagline_empty_summary));
        } else {
            edit.setText(value);
            edit.setSummary(value);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof EditTextPreference)) {
            return false;
        }
        String text = String.valueOf(newValue).trim();
        Settings.Secure.putString(mContext.getContentResolver(), SETTING_KEY, text);
        EditTextPreference edit = (EditTextPreference) preference;
        edit.setText(text);
        edit.setSummary(text);
        return true;
    }
}
