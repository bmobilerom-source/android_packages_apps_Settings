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
 * Adapter for managing Fingerprint Tools preference changes.
 * Handles state persistence and preference updates.
 */
public class FingerprintToolsAdapter {

    private final Context mContext;

    public FingerprintToolsAdapter(Context context) {
        mContext = context;
    }

    /**
     * Setup authentication ripple preference.
     */
    public void setupAuthRipplePreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = FingerprintToolsHelper.isAuthRippleEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return FingerprintToolsHelper.setAuthRippleEnabled(mContext, enabled);
            }
        });
    }

    /**
     * Setup fingerprint success vibration preference.
     */
    public void setupFpSuccessVibratePreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = FingerprintToolsHelper.isFpSuccessVibrateEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return FingerprintToolsHelper.setFpSuccessVibrateEnabled(mContext, enabled);
            }
        });
    }

    /**
     * Setup fingerprint error vibration preference.
     */
    public void setupFpErrorVibratePreference(AdaptiveSwitchPreference preference) {
        if (preference == null) {
            return;
        }
        boolean enabled = FingerprintToolsHelper.isFpErrorVibrateEnabled(mContext);
        preference.setChecked(enabled);

        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                boolean enabled = (Boolean) newValue;
                return FingerprintToolsHelper.setFpErrorVibrateEnabled(mContext, enabled);
            }
        });
    }
}

