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
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class SystemAnimationStyleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String SYSTEM_ANIMATION_STYLE_KEY = "system_animation_style";

    public SystemAnimationStyleController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return SYSTEM_ANIMATION_STYLE_KEY;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            final int currentValue = Settings.System.getInt(mContext.getContentResolver(),
                    "system_animation_style", 0);
            listPreference.setValue(String.valueOf(currentValue));
            // Set summary to show selected animation style
            int index = listPreference.findIndexOfValue(String.valueOf(currentValue));
            if (index >= 0 && index < listPreference.getEntries().length) {
                listPreference.setSummary(listPreference.getEntries()[index]);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int value = Integer.parseInt((String) newValue);
        Settings.System.putInt(mContext.getContentResolver(),
                "system_animation_style", value);
        updateState(preference);
        return true;
    }
}