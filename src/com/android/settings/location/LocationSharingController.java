/*
 * Copyright (C) 2025 bmobile
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

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for location sharing toggle.
 * Enables/disables global location sharing for all apps.
 */
public class LocationSharingController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "location_sharing_enabled";
    private static final String TAG = "LocationSharing";

    public LocationSharingController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        // Default to enabled (1) - allow sharing by default
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, isChecked ? 1 : 0);
        if (result) {
            android.util.Log.d(TAG, "Location sharing " + (isChecked ? "enabled" : "disabled"));
        }
        return result;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

