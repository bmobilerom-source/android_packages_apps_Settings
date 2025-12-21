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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.concurrent.TimeUnit;

/**
 * Controller for location accuracy indicators.
 * Shows current accuracy level, battery impact, and last update time.
 */
public class LocationAccuracyIndicatorController extends BasePreferenceController
        implements LocationListener {

    private LocationManager mLocationManager;
    private Location mLastLocation;
    private long mLastUpdateTime;
    private Preference mPreference;

    public LocationAccuracyIndicatorController(Context context, String key) {
        super(context, key);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        requestLocationUpdates();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(androidx.preference.PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        if (mLastLocation != null) {
            String accuracyText = getAccuracyText();
            String lastUpdateText = getLastUpdateText();

            preference.setTitle(mContext.getString(R.string.location_accuracy_title));
            preference.setSummary(accuracyText + " • " + lastUpdateText);
        } else {
            preference.setTitle(mContext.getString(R.string.location_accuracy_title));
            preference.setSummary(mContext.getString(R.string.location_accuracy_unknown));
        }
    }

    private String getAccuracyText() {
        if (mLastLocation == null) {
            return mContext.getString(R.string.location_accuracy_unknown);
        }

        float accuracy = mLastLocation.getAccuracy();

        if (accuracy <= 3.0f) {
            return mContext.getString(R.string.location_accuracy_high);
        } else if (accuracy <= 30.0f) {
            return mContext.getString(R.string.location_accuracy_medium);
        } else {
            return mContext.getString(R.string.location_accuracy_low);
        }
    }

    private String getLastUpdateText() {
        if (mLastUpdateTime == 0) {
            return mContext.getString(R.string.location_last_update, "Never");
        }

        long timeDiff = System.currentTimeMillis() - mLastUpdateTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);

        if (minutes < 1) {
            return mContext.getString(R.string.location_last_update, "Just now");
        } else if (minutes < 60) {
            return mContext.getString(R.string.location_last_update, minutes + " min ago");
        } else {
            long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
            return mContext.getString(R.string.location_last_update, hours + " hr ago");
        }
    }

    private void requestLocationUpdates() {
        try {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000, // 1 minute
                10, // 10 meters
                this
            );
            mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                60000, // 1 minute
                10, // 10 meters
                this
            );
        } catch (SecurityException e) {
            // Handle permission issues
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = System.currentTimeMillis();
        // Notify preference to update
        if (mPreference != null) {
            updateState(mPreference);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changes if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Handle provider enabled
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Handle provider disabled
    }

    public void onDestroy() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }
}

