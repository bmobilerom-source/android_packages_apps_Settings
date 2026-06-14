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

package com.android.settings.display;

import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class MonetColorStyleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetColorStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
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
            String style = MonetThemeApplier.getCurrentStyle(mContext);
            if ("monochrome".equals(style)) {
                style = "tonal_spot";
            }
            listPreference.setValue(style);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String styleString = (String) newValue;
        int seedColor = MonetThemeApplier.getCurrentSeedColor(mContext);
        if (seedColor != 0) {
            return MonetThemeApplier.applyPreset(mContext, seedColor, styleString, 0);
        }
        return MonetThemeApplier.applyStyle(mContext, styleString);
    }
}
