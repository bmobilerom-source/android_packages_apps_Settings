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
 *
 * INTEGRATION WITH SYSTEM SERVICES:
 * =================================
 * This helper class is designed to be used by LocationManagerService
 * in frameworks/base/services/core/java/com/android/server/LocationManagerService.java
 *
 * To integrate:
 * 1. Copy LocationPrivacyManager.java to frameworks/base/core/java/android/location/
 * 2. In LocationManagerService, import and use LocationPrivacyManager
 * 3. Before returning location to apps, call:
 *    - LocationPrivacyManager.shouldMaskLocation() to check if location should be blocked
 *    - LocationPrivacyManager.coarsenLocation() to apply privacy coarsening
 * 4. Listen to broadcasts:
 *    - "com.android.settings.location.SHARING_CHANGED"
 *    - "com.android.settings.location.BACKGROUND_ACCESS_CHANGED"
 *    - "com.android.settings.location.TIME_RESTRICTIONS_CHANGED"
 *    - "com.android.settings.location.accuracy.changed"
 */

package com.android.settings.location;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;

/**
 * Helper class for system services to integrate location privacy features.
 * This provides static methods that can be called from LocationManagerService.
 */
public class LocationPrivacyServiceHelper {
    
    /**
     * Check if location should be masked for a given package and request type.
     * Called by LocationManagerService before returning location to apps.
     * 
     * @param context System context
     * @param packageName Package requesting location
     * @param isBackground Whether this is a background location request
     * @return true if location should be masked/blocked, false otherwise
     */
    public static boolean shouldMaskLocation(Context context, String packageName, boolean isBackground) {
        LocationPrivacyManager manager = new LocationPrivacyManager(context);
        return manager.shouldMaskLocation(packageName, isBackground);
    }
    
    /**
     * Apply location coarsening based on user's precision preference.
     * Called by LocationManagerService before returning location to apps.
     * 
     * @param context System context
     * @param location Original location from GPS/network
     * @return Coarsened location with reduced accuracy
     */
    public static Location coarsenLocation(Context context, Location location) {
        LocationPrivacyManager manager = new LocationPrivacyManager(context);
        return manager.coarsenLocation(location);
    }
    
    /**
     * Get recommended location request parameters based on privacy settings.
     * Apps can use this to adjust their location requests.
     * 
     * @param context System context
     * @return LocationRequestParams with recommended minTime, minDistance, maxAccuracy
     */
    public static LocationPrivacyManager.LocationRequestParams getRecommendedParams(Context context) {
        LocationPrivacyManager manager = new LocationPrivacyManager(context);
        return manager.getRecommendedParams();
    }
    
    /**
     * Check if location services should be available based on privacy settings.
     * Used by QS location tile to determine if location can be enabled.
     * 
     * @param context System context
     * @return true if location is available, false if blocked by privacy settings
     */
    public static boolean isLocationAvailable(Context context) {
        LocationPrivacyManager manager = new LocationPrivacyManager(context);
        return manager.isLocationAvailable();
    }
    
    /**
     * Get current location precision setting.
     * 
     * @param context System context
     * @return "precise", "approximate", or "coarse"
     */
    public static String getLocationPrecision(Context context) {
        String precision = Settings.Secure.getString(context.getContentResolver(), "location_precision");
        return precision != null ? precision : "precise";
    }
}

