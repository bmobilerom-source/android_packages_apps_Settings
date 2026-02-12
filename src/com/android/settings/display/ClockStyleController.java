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

package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ClockStyleController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    // Clock style constants (matching SystemUI ClockStyle)
    private static final String CLOCK_STYLE_KEY = "clock_style";
    private static final int DEFAULT_STYLE = 0;

    public ClockStyleController(Context context, String key) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return CLOCK_STYLE_KEY;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;

            // Ensure entries and values are set
            if (listPreference.getEntries() == null) {
                listPreference.setEntries(com.android.settings.R.array.clock_style_entries);
            }
            if (listPreference.getEntryValues() == null) {
                listPreference.setEntryValues(com.android.settings.R.array.clock_style_values);
            }

            final int currentValue = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    CLOCK_STYLE_KEY,
                    DEFAULT_STYLE,
                    UserHandle.USER_CURRENT);
            listPreference.setValue(String.valueOf(currentValue));
            // Set summary to show selected clock style
            int index = listPreference.findIndexOfValue(String.valueOf(currentValue));
            if (index >= 0 && index < listPreference.getEntries().length) {
                listPreference.setSummary(listPreference.getEntries()[index]);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int value = Integer.parseInt((String) newValue);
        Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                CLOCK_STYLE_KEY,
                value,
                UserHandle.USER_CURRENT);
        updateState(preference);
        return true;
    }
}