package com.epic.fragments;

import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;

public class StatusBarLogoPositionController extends BasePreferenceController implements Preference.OnPreferenceChangeListener {

    public StatusBarLogoPositionController(Context context, String preferenceKey) {
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
        ListPreference listPreference = (ListPreference) preference;
        int position = StatusBarLogoHelper.getStatusBarLogoPosition(mContext);
        listPreference.setValue(String.valueOf(position));
        listPreference.setSummary(StatusBarLogoHelper.getPositionName(mContext, position));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int position = Integer.parseInt((String) newValue);
        StatusBarLogoHelper.setStatusBarLogoPosition(mContext, position);
        updateState(preference);
        return true;
    }
}
