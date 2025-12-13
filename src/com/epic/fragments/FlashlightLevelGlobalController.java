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
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;

public class FlashlightLevelGlobalController extends BasePreferenceController implements Preference.OnPreferenceChangeListener {

    private static final String FLASHLIGHT_LEVEL_GLOBAL = "flashlight_level_global";

    public FlashlightLevelGlobalController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        return Settings.System.putInt(mContext.getContentResolver(),
                FLASHLIGHT_LEVEL_GLOBAL, enabled ? 1 : 0);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                FLASHLIGHT_LEVEL_GLOBAL, 0) == 1;
        if (preference instanceof androidx.preference.SwitchPreference) {
            ((androidx.preference.SwitchPreference) preference).setChecked(enabled);
        }
    }
}
