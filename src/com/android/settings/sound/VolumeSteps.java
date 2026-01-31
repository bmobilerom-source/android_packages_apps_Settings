/*
 * Copyright (C) 2024 Yet Another AOSP Project
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

package com.android.settings.sound;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * volume steps settings under sound
 */
@SearchIndexable
public class VolumeSteps extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "VolumeSteps";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.volume_steps_settings);

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen screen = getPreferenceScreen();
        final int count = screen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = screen.getPreference(i);
            if (!(pref instanceof ListPreference))
                continue;
            String key = pref.getKey();
            final int def = Settings.System.getIntForUser(resolver, "default_" + key, getDefaultValue(key), UserHandle.USER_CURRENT);
            final int value = Settings.System.getIntForUser(resolver, key, def, UserHandle.USER_CURRENT);
            ListPreference listPref = (ListPreference) pref;
            listPref.setValue(String.valueOf(value));
            listPref.setSummary(String.valueOf(value));
            listPref.setOnPreferenceChangeListener(this);
        }
    }

    private int getDefaultValue(String key) {
        switch (key) {
            case "max_music_volume":
                return 15;
            case "max_call_volume":
                return 7;
            case "max_alarm_volume":
                return 7;
            default:
                return 15;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof ListPreference))
            return false;

        String valueStr = (String) newValue;
        int value = Integer.parseInt(valueStr);

        Settings.System.putIntForUser(getActivity().getContentResolver(),
                preference.getKey(), value, UserHandle.USER_CURRENT);

        // Update summary
        preference.setSummary(valueStr);

        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.volume_steps_settings);
}
