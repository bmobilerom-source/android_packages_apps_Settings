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
import android.content.Intent;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for manage app permissions preference.
 * Opens the app permissions page filtered to location permissions.
 */
public class ManageAppPermissionsPreferenceController extends BasePreferenceController {

    public ManageAppPermissionsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            // Open app permissions page filtered to location permissions
            try {
                // Try to open the app permissions page directly
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS);
                mContext.startActivity(intent);
            } catch (Exception e) {
                // If that fails, open general settings
                Intent lastResort = new Intent(android.provider.Settings.ACTION_SETTINGS);
                mContext.startActivity(lastResort);
            }
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }
}

