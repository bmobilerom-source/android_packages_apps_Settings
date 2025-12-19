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
import android.location.GnssStatus;
import android.location.LocationManager;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for location service status indicators.
 * Shows GPS, Network, and Wi-Fi location service status.
 */
public class LocationServiceStatusController extends BasePreferenceController {

    private LocationManager mLocationManager;
    private GnssStatus.Callback mGnssCallback;

    public LocationServiceStatusController(Context context, String key) {
        super(context, key);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        setupGnssCallback();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        String statusText = getLocationServiceStatus();
        preference.setTitle(statusText);
    }

    private String getLocationServiceStatus() {
        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gpsEnabled && networkEnabled) {
            return mContext.getString(R.string.location_status_gps_active);
        } else if (gpsEnabled) {
            return mContext.getString(R.string.location_status_gps_active);
        } else if (networkEnabled) {
            return mContext.getString(R.string.location_status_network_active);
        } else {
            return mContext.getString(R.string.location_status_gps_inactive);
        }
    }

    private void setupGnssCallback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mGnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    // GPS status changed - could update UI here if needed
                }
            };

            try {
                mLocationManager.registerGnssStatusCallback(mGnssCallback, null);
            } catch (Exception e) {
                // Handle registration failure
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGnssCallback != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mLocationManager.unregisterGnssStatusCallback(mGnssCallback);
        }
    }
}
