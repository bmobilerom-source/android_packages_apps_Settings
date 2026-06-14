/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.bmobile.customization;

import android.content.Context;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.bmobile.fragments.StatusBarLogoHelper;

public class StatusBarLogoStyleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public StatusBarLogoStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return StatusBarLogoHelper.isLogoFeatureAvailable(mContext) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof ListPreference) {
            pref.setOnPreferenceChangeListener(this);
            updateState(pref);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (!(preference instanceof ListPreference)) {
            return;
        }
        ListPreference listPreference = (ListPreference) preference;
        int style = StatusBarLogoHelper.getStatusBarLogoStyle(mContext);
        listPreference.setValue(String.valueOf(style));
        listPreference.setSummary(StatusBarLogoHelper.getStyleName(mContext, style));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int style = Integer.parseInt((String) newValue);
        StatusBarLogoHelper.setStatusBarLogoStyle(mContext, style);
        updateState(preference);
        return true;
    }
}
