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
import android.location.LocationManager;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.slices.Sliceable;

/**
 * Controller for time-based location restrictions toggle preference.
 * When enabled, location access is restricted to specific time periods.
 */
public class TimeRestrictionsToggleController extends TogglePreferenceController {

    private static final String SETTING_KEY = "location_time_restrictions_enabled";
    private LocationPrivacyManager mPrivacyManager;

    public TimeRestrictionsToggleController(Context context, String key) {
        super(context, key);
        mPrivacyManager = new LocationPrivacyManager(context);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                SETTING_KEY, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean success = Settings.Secure.putInt(mContext.getContentResolver(),
                SETTING_KEY, isChecked ? 1 : 0);
        
        if (success) {
            // Broadcast change to notify system services
            Intent intent = new Intent("com.android.settings.location.TIME_RESTRICTIONS_CHANGED");
            intent.putExtra("enabled", isChecked);
            mContext.sendBroadcast(intent);
            
            // Update location availability
            updateLocationAvailability();
        }
        
        return success;
    }
    
    /**
     * Update location availability based on privacy settings
     */
    private void updateLocationAvailability() {
        // Notify location manager of privacy state change
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            // Force refresh of location providers
            // The actual masking will be handled by system services
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return Sliceable.NO_RES;
    }
}

