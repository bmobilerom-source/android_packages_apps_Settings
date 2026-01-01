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
import android.database.ContentObserver;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

/**
 * Controller for location service status indicators.
 * Shows GPS, Network, and Wi-Fi location service status.
 */
public class LocationServiceStatusController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private LocationManager mLocationManager;
    private GnssStatus.Callback mGnssCallback;
    private Preference mPreference;
    private SettingObserver mSettingObserver;

    public LocationServiceStatusController(Context context, String key) {
        super(context, key);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mSettingObserver = new SettingObserver(new Handler(Looper.getMainLooper()));
        setupGnssCallback();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            mPreference.setLayoutResource(R.layout.adaptive_preference_card_top);
        }
    }

    @Override
    public void onStart() {
        mSettingObserver.register(mContext.getContentResolver());
    }

    @Override
    public void onStop() {
        mSettingObserver.unregister(mContext.getContentResolver());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        String statusText = getLocationServiceStatus();
        String accuracyText = getLocationAccuracy();
        preference.setTitle(statusText);
        preference.setSummary(accuracyText);
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

    private String getLocationAccuracy() {
        // Get accuracy from settings
        String precision = Settings.Secure.getString(
                mContext.getContentResolver(),
                "location_precision");
        if (precision != null) {
            switch (precision) {
                case "precise":
                    return mContext.getString(R.string.location_accuracy_high);
                case "approximate":
                    return mContext.getString(R.string.location_accuracy_medium);
                case "coarse":
                    return mContext.getString(R.string.location_accuracy_low);
            }
        }
        return mContext.getString(R.string.location_accuracy_high);
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

    public void onDestroy() {
        if (mGnssCallback != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mLocationManager.unregisterGnssStatusCallback(mGnssCallback);
        }
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void register(android.content.ContentResolver resolver) {
            resolver.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.LOCATION_MODE),
                    false, this);
            resolver.registerContentObserver(
                    Settings.Secure.getUriFor("location_precision"),
                    false, this);
        }

        public void unregister(android.content.ContentResolver resolver) {
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (mPreference != null) {
                updateState(mPreference);
            }
        }
    }
}

