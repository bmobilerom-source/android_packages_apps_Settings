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
 * Controller for location background access toggle.
 * Enables/disables background location access for all apps.
 * 
 * AOSP Hierarchy:
 * - Location master toggle (LocationManager.isLocationEnabled()) must be ON
 * - Location sharing must be enabled
 * - Background access depends on both location and location sharing
 */
public class LocationBackgroundAccessController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "location_background_access_enabled";
    private static final String LOCATION_SHARING_KEY = "location_sharing_enabled";
    private static final String TAG = "LocationBackgroundAccess";
    private LocationManager mLocationManager;

    public LocationBackgroundAccessController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        // AOSP Rule: Background access requires both location and location sharing to be enabled
        if (!isLocationEnabled() || !isLocationSharingEnabled()) {
            return false;
        }
        // Default to enabled (1) - allow background access by default when prerequisites are met
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // AOSP Rule: Cannot enable background access if location or location sharing is disabled
        if (isChecked) {
            if (!isLocationEnabled()) {
                android.util.Log.w(TAG, "Cannot enable background access: location is disabled");
                return false;
            }
            if (!isLocationSharingEnabled()) {
                android.util.Log.w(TAG, "Cannot enable background access: location sharing is disabled");
                return false;
            }
        }
        
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, isChecked ? 1 : 0);
        if (result) {
            android.util.Log.d(TAG, "Background location access " + (isChecked ? "enabled" : "disabled"));
        }
        return result;
    }

    @Override
    public void updateState(androidx.preference.Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.SwitchPreference) {
            androidx.preference.SwitchPreference switchPref = (androidx.preference.SwitchPreference) preference;
            boolean locationEnabled = isLocationEnabled();
            boolean sharingEnabled = isLocationSharingEnabled();
            
            // Disable if prerequisites are not met (AOSP requirement)
            switchPref.setEnabled(locationEnabled && sharingEnabled);
            
            if (!locationEnabled) {
                switchPref.setSummary(R.string.location_background_requires_location_enabled);
            } else if (!sharingEnabled) {
                switchPref.setSummary(R.string.location_background_requires_sharing_enabled);
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

    /**
     * Check if location sharing is enabled
     */
    private boolean isLocationSharingEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(), LOCATION_SHARING_KEY, 1) != 0;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

