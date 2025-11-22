/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;

public class SystemAnimationStyleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_SYSTEM_ANIMATION_STYLE = "system_animation_style";

    public SystemAnimationStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        ListPreference listPreference = (ListPreference) preference;
        int value = Settings.System.getInt(mContext.getContentResolver(),
                KEY_SYSTEM_ANIMATION_STYLE, 0);
        listPreference.setValue(String.valueOf(value));
        int index = listPreference.findIndexOfValue(String.valueOf(value));
        if (index >= 0) {
            listPreference.setSummary(listPreference.getEntries()[index]);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.System.putInt(mContext.getContentResolver(),
                KEY_SYSTEM_ANIMATION_STYLE, value);
        updateState(preference);
        return true;
    }
}

