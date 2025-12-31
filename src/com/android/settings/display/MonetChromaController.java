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
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;

public class MonetChromaController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetChromaController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SeekBarPreference) {
            SeekBarPreference seekBarPreference = (SeekBarPreference) preference;
            int currentValue = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_chroma_factor", 50); // Default 50%
            seekBarPreference.setProgress(currentValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = (Integer) newValue;
        return Settings.System.putInt(mContext.getContentResolver(),
                "monet_chroma_factor", value);
    }
}






