/*
 * Copyright (C) 2025 LineageOS
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

package com.epic.fragments;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for Security Features based on GrapheneOS and CalyxOS practices.
 * Provides methods to manage privacy and security features.
 */
public class SecurityFeaturesHelper {
    private static final String TAG = "SecurityFeaturesHelper";

    // Settings.Secure keys for security features
    private static final String KEY_NETWORK_PERMISSION_CONTROL = "network_permission_control_enabled";
    private static final String KEY_APP_HARDENING = "app_hardening_enabled";
    private static final String KEY_SENSOR_ACCESS_CONTROL = "sensor_access_control_enabled";
    private static final String KEY_LOCATION_ACCESS_CONTROL = "location_access_control_enabled";
    private static final String KEY_MICROPHONE_ACCESS_CONTROL = "microphone_access_control_enabled";

    /**
     * Network Permission Control - Control network access for apps
     * Based on GrapheneOS firewall implementation
     */
    public static boolean isNetworkPermissionControlEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                KEY_NETWORK_PERMISSION_CONTROL, 0) == 1;
    }

    public static boolean setNetworkPermissionControlEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                KEY_NETWORK_PERMISSION_CONTROL, enabled ? 1 : 0);
        if (result) {
            // Enable network permission enforcement
            Settings.Global.putInt(context.getContentResolver(),
                    "network_permission_enforcement", enabled ? 1 : 0);
            Log.d(TAG, "Network permission control " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * App Hardening - Enhanced security for apps
     * Based on GrapheneOS app hardening features
     */
    public static boolean isAppHardeningEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                KEY_APP_HARDENING, 0) == 1;
    }

    public static boolean setAppHardeningEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                KEY_APP_HARDENING, enabled ? 1 : 0);
        if (result) {
            // Enable app hardening features
            Settings.Global.putInt(context.getContentResolver(),
                    "app_hardening_enabled", enabled ? 1 : 0);
            Log.d(TAG, "App hardening " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Sensor Access Control - Control sensor access for apps
     * Based on CalyxOS sensor blocking features
     */
    public static boolean isSensorAccessControlEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                KEY_SENSOR_ACCESS_CONTROL, 0) == 1;
    }

    public static boolean setSensorAccessControlEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                KEY_SENSOR_ACCESS_CONTROL, enabled ? 1 : 0);
        if (result) {
            // Enable sensor access control
            Settings.Global.putInt(context.getContentResolver(),
                    "sensor_access_control", enabled ? 1 : 0);
            Log.d(TAG, "Sensor access control " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Location Access Control - Enhanced location access control
     * Based on GrapheneOS location privacy features
     */
    public static boolean isLocationAccessControlEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                KEY_LOCATION_ACCESS_CONTROL, 0) == 1;
    }

    public static boolean setLocationAccessControlEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                KEY_LOCATION_ACCESS_CONTROL, enabled ? 1 : 0);
        if (result) {
            // Enable enhanced location access control
            Settings.Secure.putInt(context.getContentResolver(),
                    "location_access_control_enabled", enabled ? 1 : 0);
            Log.d(TAG, "Location access control " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Microphone Access Control - Enhanced microphone access control
     * Based on GrapheneOS microphone privacy features
     */
    public static boolean isMicrophoneAccessControlEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                KEY_MICROPHONE_ACCESS_CONTROL, 0) == 1;
    }

    public static boolean setMicrophoneAccessControlEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                KEY_MICROPHONE_ACCESS_CONTROL, enabled ? 1 : 0);
        if (result) {
            // Enable enhanced microphone access control
            Settings.Secure.putInt(context.getContentResolver(),
                    "microphone_access_control_enabled", enabled ? 1 : 0);
            Log.d(TAG, "Microphone access control " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Get security features status summary
     */
    public static String getSecurityFeaturesStatus(Context context) {
        int enabledCount = 0;
        if (isNetworkPermissionControlEnabled(context)) enabledCount++;
        if (isAppHardeningEnabled(context)) enabledCount++;
        if (isSensorAccessControlEnabled(context)) enabledCount++;
        if (isLocationAccessControlEnabled(context)) enabledCount++;
        if (isMicrophoneAccessControlEnabled(context)) enabledCount++;
        return enabledCount + " security features active";
    }
}

