/*
 * Copyright (C) 2025 bmobile
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

package com.android.settings.location;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for location time restrictions toggle.
 * Allows users to enable/disable time-based location access restrictions.
 */
public class LocationTimeRestrictionsController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String SETTING_TIME_RESTRICTIONS_ENABLED = "location_time_restrictions_enabled";
    private static final String SETTING_ALLOWED_TIMES = "location_allowed_times";
    private SettingObserver mSettingObserver;

    public LocationTimeRestrictionsController(Context context, String key) {
        super(context, key);
        mSettingObserver = new SettingObserver(new Handler(Looper.getMainLooper()));
        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(SETTING_TIME_RESTRICTIONS_ENABLED),
            false,
            mSettingObserver
        );
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null && preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setEntries(R.array.location_time_period_entries);
            listPreference.setEntryValues(R.array.location_time_period_values);
            listPreference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            
            // Read current value from secure settings
            String currentValue = Settings.Secure.getString(
                mContext.getContentResolver(),
                SETTING_ALLOWED_TIMES
            );
            if (currentValue == null) {
                currentValue = "always"; // default to always
            }
            
            listPreference.setValue(currentValue);
            updateSummary(listPreference, currentValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            String value = (String) newValue;
            
            // Save to secure settings
            boolean success = Settings.Secure.putString(
                mContext.getContentResolver(),
                SETTING_ALLOWED_TIMES,
                value
            );
            
            if (success) {
                ((ListPreference) preference).setValue(value);
                updateSummary((ListPreference) preference, value);
            }
            
            return success;
        }
        return false;
    }

    private void updateSummary(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index >= 0) {
            preference.setSummary(preference.getEntries()[index]);
        }
    }

    /**
     * Observer for time restrictions setting changes
     */
    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.Secure.getUriFor(SETTING_TIME_RESTRICTIONS_ENABLED))) {
                // Setting changed - could update UI if needed
            }
        }
    }
}


