package com.epic.fragments;

import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;

public class StatusBarLogoStyleController extends BasePreferenceController implements Preference.OnPreferenceChangeListener {

    public StatusBarLogoStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof ListPreference) {
            pref.setOnPreferenceChangeListener(this);
            updateState(pref);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (!(preference instanceof ListPreference)) {
            return;
        }
        ListPreference listPreference = (ListPreference) preference;
        int style = StatusBarLogoHelper.getStatusBarLogoStyle(mContext);
        listPreference.setValue(String.valueOf(style));
        listPreference.setSummary(StatusBarLogoHelper.getStyleName(mContext, style));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int style = Integer.parseInt((String) newValue);
        StatusBarLogoHelper.setStatusBarLogoStyle(mContext, style);
        updateState(preference);
        return true;
    }
}
