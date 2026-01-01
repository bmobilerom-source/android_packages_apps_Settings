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
import android.location.LocationManager;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for location sharing toggle.
 * Enables/disables global location sharing for all apps.
 * 
 * AOSP Hierarchy:
 * - Location master toggle (LocationManager.isLocationEnabled()) must be ON
 * - Location sharing can only be enabled if location is enabled
 * - Location background access depends on location sharing
 */
public class LocationSharingController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "location_sharing_enabled";
    private static final String TAG = "LocationSharing";
    private LocationManager mLocationManager;

    public LocationSharingController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        // Check if location is enabled first (AOSP requirement)
        if (!isLocationEnabled()) {
            return false;
        }
        // Default to enabled (1) - allow sharing by default when location is on
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // AOSP Rule: Cannot enable location sharing if location is disabled
        if (isChecked && !isLocationEnabled()) {
            android.util.Log.w(TAG, "Cannot enable location sharing: location is disabled");
            return false;
        }
        
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, isChecked ? 1 : 0);
        if (result) {
            android.util.Log.d(TAG, "Location sharing " + (isChecked ? "enabled" : "disabled"));
            
            // If disabling location sharing, also disable background access (AOSP hierarchy)
            if (!isChecked) {
                Settings.Secure.putInt(mContext.getContentResolver(), 
                    "location_background_access_enabled", 0);
            }
        }
        return result;
    }

    @Override
    public void updateState(androidx.preference.Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.SwitchPreference) {
            androidx.preference.SwitchPreference switchPref = (androidx.preference.SwitchPreference) preference;
            // Disable if location is off (AOSP requirement)
            switchPref.setEnabled(isLocationEnabled());
            if (!isLocationEnabled()) {
                switchPref.setSummary(R.string.location_sharing_requires_location_enabled);
            }
        }
    }

    /**
     * Check if location is enabled (AOSP master toggle)
     */
    private boolean isLocationEnabled() {
        if (mLocationManager == null) {
            return false;
        }
        try {
            return mLocationManager.isLocationEnabled();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error checking location enabled state", e);
            return false;
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

