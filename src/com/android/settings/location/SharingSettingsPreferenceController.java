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

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for sharing settings preference.
 */
public class SharingSettingsPreferenceController extends BasePreferenceController {

    public SharingSettingsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        
        // Update summary based on current sharing settings
        String sharingLevel = Settings.Secure.getString(
                mContext.getContentResolver(),
                "location_sharing_level");
        
        if (sharingLevel != null) {
            preference.setSummary("Current: " + sharingLevel);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            // TODO: Open sharing settings dialog
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }
}

