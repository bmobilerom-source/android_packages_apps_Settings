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

package com.android.settings.applications;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for application downgrade preference.
 */
public class AppDowngradePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_PM_DOWNGRADE_ALLOWED = "pm_downgrade_allowed";

    public AppDowngradePreferenceController(@NonNull Context context, @NonNull String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
            int value = Settings.Global.getInt(mContext.getContentResolver(),
                    KEY_PM_DOWNGRADE_ALLOWED, 0);
            switchPreference.setChecked(value != 0);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.Global.putInt(mContext.getContentResolver(),
                KEY_PM_DOWNGRADE_ALLOWED, enabled ? 1 : 0);
        return true;
    }
}


