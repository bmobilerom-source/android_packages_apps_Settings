/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
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
        ContentResolver resolver = mContext.getContentResolver();
        boolean success = Settings.System.putInt(resolver, KEY_SYSTEM_ANIMATION_STYLE, value);
        if (success) {
            // Notify SystemUI and other observers of the change
            notifySystemAnimationChange(resolver);
        }
        updateState(preference);
        return success;
    }

    /**
     * Notify SystemUI and other observers of system animation style changes
     */
    private void notifySystemAnimationChange(ContentResolver resolver) {
        try {
            resolver.notifyChange(
                    Settings.System.getUriFor(KEY_SYSTEM_ANIMATION_STYLE),
                    null, true);
            Log.d("SystemAnimationStyleController", "Notified SystemUI of system animation style change");
        } catch (Exception e) {
            Log.e("SystemAnimationStyleController", "Failed to notify system animation style change", e);
        }
    }
}


