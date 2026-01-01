/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.settings.location;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

/**
 * LocationPrivacyManager handles location privacy enforcement including:
 * - Location masking when sharing/background access is disabled
 * - Location coarsening based on precision settings
 * - Integration with QS location tile
 */
public class LocationPrivacyManager {
    private static final String TAG = "LocationPrivacyManager";
    
    // Settings keys
    private static final String KEY_LOCATION_PRECISION = "location_precision";
    private static final String KEY_LOCATION_SHARING = "location_sharing_enabled";
    private static final String KEY_BACKGROUND_ACCESS = "location_background_access_enabled";
    private static final String KEY_TIME_RESTRICTIONS = "location_time_restrictions_enabled";
    
    // Coarse location parameters - significantly reduce accuracy
    private static final float COARSE_NOISE_RADIUS_METERS = 5000.0f; // 5km noise radius
    private static final float APPROXIMATE_NOISE_RADIUS_METERS = 1000.0f; // 1km noise radius
    private static final long COARSE_MIN_TIME_MS = 300000L; // 5 minutes
    private static final float COARSE_MIN_DISTANCE_M = 2000.0f; // 2km
    
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final LocationManager mLocationManager;
    private final Random mRandom = new Random();
    
    public LocationPrivacyManager(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * Check if location should be masked (blocked) based on privacy settings
     */
    public boolean shouldMaskLocation(String packageName, boolean isBackground) {
        // Check if location sharing is disabled
        boolean sharingEnabled = Settings.Secure.getInt(mContentResolver,
                KEY_LOCATION_SHARING, 1) == 1;
        if (!sharingEnabled) {
            Log.d(TAG, "Location masked: sharing disabled");
            return true;
        }
        
        // Check if background access is disabled and this is a background request
        if (isBackground) {
            boolean backgroundEnabled = Settings.Secure.getInt(mContentResolver,
                    KEY_BACKGROUND_ACCESS, 1) == 1;
            if (!backgroundEnabled) {
                Log.d(TAG, "Location masked: background access disabled");
                return true;
            }
        }
        
        // Check time restrictions
        boolean timeRestrictionsEnabled = Settings.Secure.getInt(mContentResolver,
                KEY_TIME_RESTRICTIONS, 0) == 1;
        if (timeRestrictionsEnabled && !isTimeAllowed()) {
            Log.d(TAG, "Location masked: time restrictions active");
            return true;
        }
        
        return false;
    }
    
    /**
     * Apply location coarsening based on precision setting
     * This significantly reduces location accuracy for privacy
     */
    public Location coarsenLocation(Location location) {
        if (location == null) {
            return null;
        }
        
        String precision = Settings.Secure.getString(mContentResolver, KEY_LOCATION_PRECISION);
        if (precision == null) {
            precision = "precise";
        }
        
        Location coarsenedLocation = new Location(location);
        
        switch (precision) {
            case "coarse":
                // Maximum privacy: Add significant noise (5km radius)
                coarsenLocationWithNoise(coarsenedLocation, COARSE_NOISE_RADIUS_METERS);
                // Reduce accuracy dramatically
                coarsenedLocation.setAccuracy(Math.max(coarsenedLocation.getAccuracy(), 5000.0f));
                break;
                
            case "approximate":
                // Medium privacy: Add moderate noise (1km radius)
                coarsenLocationWithNoise(coarsenedLocation, APPROXIMATE_NOISE_RADIUS_METERS);
                coarsenedLocation.setAccuracy(Math.max(coarsenedLocation.getAccuracy(), 1000.0f));
                break;
                
            case "precise":
            default:
                // No coarsening for precise mode
                break;
        }
        
        return coarsenedLocation;
    }
    
    /**
     * Add random noise to location coordinates to reduce accuracy
     */
    private void coarsenLocationWithNoise(Location location, float radiusMeters) {
        // Generate random angle and distance
        double angle = mRandom.nextDouble() * 2 * Math.PI;
        double distance = mRandom.nextDouble() * radiusMeters;
        
        // Calculate offset in meters
        double latOffset = distance * Math.cos(angle) / 111320.0; // meters to degrees (latitude)
        double lonOffset = distance * Math.sin(angle) / (111320.0 * Math.cos(Math.toRadians(location.getLatitude())));
        
        // Apply offset
        location.setLatitude(location.getLatitude() + latOffset);
        location.setLongitude(location.getLongitude() + lonOffset);
    }
    
    /**
     * Check if current time is within allowed time periods
     */
    private boolean isTimeAllowed() {
        // Get allowed time periods from settings
        String allowedTimes = Settings.Secure.getString(mContentResolver, "location_allowed_times");
        if (allowedTimes == null || "always".equals(allowedTimes)) {
            return true;
        }
        
        // Parse time restrictions
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        
        switch (allowedTimes) {
            case "daytime":
                // 6 AM to 10 PM
                return currentHour >= 6 && currentHour < 22;
            case "business":
                // 9 AM to 5 PM
                return currentHour >= 9 && currentHour < 17;
            case "evening":
                // 6 PM to 11 PM
                return currentHour >= 18 && currentHour < 23;
            case "custom":
                // Custom times would need additional settings
                // For now, default to daytime
                return currentHour >= 6 && currentHour < 22;
            default:
                return true;
        }
    }
    
    /**
     * Get recommended location request parameters based on privacy settings
     */
    public LocationRequestParams getRecommendedParams() {
        String precision = Settings.Secure.getString(mContentResolver, KEY_LOCATION_PRECISION);
        if (precision == null) {
            precision = "precise";
        }
        
        switch (precision) {
            case "coarse":
                return new LocationRequestParams(COARSE_MIN_TIME_MS, COARSE_MIN_DISTANCE_M, 5000.0f);
            case "approximate":
                return new LocationRequestParams(5000L, 100.0f, 1000.0f);
            case "precise":
            default:
                return new LocationRequestParams(1000L, 0.0f, 10.0f);
        }
    }
    
    /**
     * Check if location services should be available based on privacy settings
     */
    public boolean isLocationAvailable() {
        // Location is available if at least one privacy feature allows it
        boolean sharingEnabled = Settings.Secure.getInt(mContentResolver,
                KEY_LOCATION_SHARING, 1) == 1;
        return sharingEnabled;
    }
    
    /**
     * Location request parameters for privacy-aware requests
     */
    public static class LocationRequestParams {
        public final long minTimeMs;
        public final float minDistanceM;
        public final float maxAccuracyM;
        
        public LocationRequestParams(long minTimeMs, float minDistanceM, float maxAccuracyM) {
            this.minTimeMs = minTimeMs;
            this.minDistanceM = minDistanceM;
            this.maxAccuracyM = maxAccuracyM;
        }
    }
}

