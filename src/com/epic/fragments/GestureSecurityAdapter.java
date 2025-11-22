/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epic.fragments;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.preferences.ui.AdaptiveSwitchPreference;

/**
 * Adapter for managing Gesture Security preference changes.
 * Handles state persistence and preference updates.
 */
public class GestureSecurityAdapter {

    private final Context mContext;

    public GestureSecurityAdapter(Context context) {
        mContext = context;
    }

    /**
     * Setup clipboard overlay preference.
     */
    public void setupClipboardOverlayPreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = GestureSecurityHelper.isClipboardOverlayEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return GestureSecurityHelper.setClipboardOverlayEnabled(mContext, enabled);
            }
        });
    }

    /**
     * Setup no storage restrict preference.
     */
    public void setupNoStorageRestrictPreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = GestureSecurityHelper.isNoStorageRestrictEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return GestureSecurityHelper.setNoStorageRestrictEnabled(mContext, enabled);
            }
        });
    }

    /**
     * Setup window ignore secure preference.
     */
    public void setupWindowIgnoreSecurePreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = GestureSecurityHelper.isWindowIgnoreSecureEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return GestureSecurityHelper.setWindowIgnoreSecureEnabled(mContext, enabled);
            }
        });
    }

    /**
     * Setup secure lock screen QS preference.
     */
    public void setupSecureLockscreenQsPreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = GestureSecurityHelper.isSecureLockscreenQsDisabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return GestureSecurityHelper.setSecureLockscreenQsDisabled(mContext, enabled);
            }
        });
    }
}


