/*
 * Copyright (C) 2024 The Android Open Source Project
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
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for allowed time periods preference.
 * Allows selecting time periods when location access is permitted.
 */
public class AllowedTimesPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String SETTING_KEY = "location_allowed_times";

    public AllowedTimesPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            
            // Set entries and values
            listPreference.setEntries(R.array.location_time_period_entries);
            listPreference.setEntryValues(R.array.location_time_period_values);
            
            // Get current value
            String currentValue = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    SETTING_KEY);
            if (currentValue == null) {
                currentValue = "always";
            }
            
            listPreference.setValue(currentValue);
            listPreference.setOnPreferenceChangeListener(this);
            updateSummary(listPreference, currentValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            String value = (String) newValue;
            boolean success = Settings.Secure.putString(
                    mContext.getContentResolver(),
                    SETTING_KEY,
                    value);
            
            if (success) {
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
}

