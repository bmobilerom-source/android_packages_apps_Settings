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
import android.os.SystemProperties;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for ripple animation style selection
 */
public class RippleAnimationController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_RIPPLE_ANIMATION_STYLE = "ripple_animation_style";
    private static final String SYSTEM_PROP_RIPPLE_STYLE = "persist.sys.ripple_style";

    public RippleAnimationController(Context context, String key) {
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
            String currentValue = SystemProperties.get(SYSTEM_PROP_RIPPLE_STYLE, "default");
            listPreference.setValue(currentValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String rippleStyle = (String) newValue;
        SystemProperties.set(SYSTEM_PROP_RIPPLE_STYLE, rippleStyle);

        // Apply the change immediately if possible
        // This would require framework modifications to read the system property
        // For now, we'll just save the preference and note that a reboot may be required

        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
