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

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for location precision preference.
 * Allows users to choose between precise and approximate location.
 */
public class LocationPrecisionController extends BasePreferenceController {

    private static final String LOCATION_PRECISION_KEY = "location_precision";

    public LocationPrecisionController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(androidx.preference.Preference preference) {
        super.updateState(preference);

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            // Set entries and values from arrays
            listPreference.setEntries(R.array.location_precision_entries);
            listPreference.setEntryValues(R.array.location_precision_values);

            // Get current value from settings
            String currentValue = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.LOCATION_PRECISION,
                "precise" // default to precise
            );

            listPreference.setValue(currentValue);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(androidx.preference.Preference preference) {
        return false; // ListPreference handles its own clicks
    }

    @Override
    public boolean isSliceable() {
        return false;
    }
}
