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

package com.bmobile.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.os.PowerManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for System Optimization features.
 * Provides methods to manage various system optimizations.
 */
public class SystemOptimizationHelper {
    private static final String TAG = "SystemOptimizationHelper";

    // Settings.System keys for optimizations
    // Only features that work out of the box without kernel/framework changes
    private static final String KEY_BACKGROUND_APP_LIMITS = "background_app_limits_enabled";
    private static final String KEY_NETWORK_OPTIMIZATION = "network_optimization_enabled";
    private static final String KEY_BATTERY_OPTIMIZATION = "battery_optimization_enabled";
    private static final String KEY_STORAGE_OPTIMIZATION = "storage_optimization_enabled";
    private static final String KEY_THERMAL_THROTTLING = "thermal_throttling_enabled";
    
    // Removed features (require kernel/framework):
    // - CPU Governor Optimization (needs kernel)
    // - I/O Scheduler Optimization (needs kernel)
    // - ZRAM Optimization (needs kernel)
    // - Memory Optimization (needs framework)
    // - Performance Mode (needs kernel)

    /**
     * Background App Limits - Restrict background processes
     */
    public static boolean isBackgroundAppLimitsEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                KEY_BACKGROUND_APP_LIMITS, 0) == 1;
    }

    public static boolean setBackgroundAppLimitsEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                KEY_BACKGROUND_APP_LIMITS, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Background app limits " + (enabled ? "enabled" : "disabled"));
            // Apply optimization via ActivityManager if available
            applyBackgroundAppLimits(context, enabled);
        }
        return result;
    }

    private static void applyBackgroundAppLimits(Context context, boolean enabled) {
        try {
            int limit = enabled ? 4 : -1; // -1 means use default
            if (limit > 0) {
                // Use DeviceConfig to set process limit (framework reads this)
                DeviceConfig.setProperty(
                    DeviceConfig.NAMESPACE_ACTIVITY_MANAGER,
                    "max_cached_processes",
                    String.valueOf(limit),
                    false); // false = don't make it the default
                Log.d(TAG, "Background app limits set to " + limit + " processes via DeviceConfig");
            } else {
                // Remove the property to use default
                DeviceConfig.deleteProperty(
                    DeviceConfig.NAMESPACE_ACTIVITY_MANAGER,
                    "max_cached_processes");
                Log.d(TAG, "Background app limits reset to default");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set background app limits via DeviceConfig", e);
        }
    }

    /**
     * Network Optimization - Optimize network usage
     */
    public static boolean isNetworkOptimizationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                KEY_NETWORK_OPTIMIZATION, 0) == 1;
    }

    public static boolean setNetworkOptimizationEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                KEY_NETWORK_OPTIMIZATION, enabled ? 1 : 0);
        if (result) {
            applyNetworkOptimization(context, enabled);
        }
        return result;
    }

    private static void applyNetworkOptimization(Context context, boolean enabled) {
        // AOSP: same API as DataSaverBackend / DataSaverSummary (NetworkPolicyManager).
        try {
            NetworkPolicyManager npm = NetworkPolicyManager.from(context);
            npm.setRestrictBackground(enabled);
            Log.d(TAG, "Network optimization (Data Saver) "
                    + (enabled ? "enabled" : "disabled"));
        } catch (SecurityException e) {
            Log.w(TAG, "Cannot set data saver: requires system permissions", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set network optimization", e);
        }
    }

    /** Reflects live Data Saver state (restrict background), for status text. */
    public static boolean isDataSaverActive(Context context) {
        try {
            return NetworkPolicyManager.from(context).getRestrictBackground();
        } catch (Exception e) {
            Log.e(TAG, "Failed to read data saver state", e);
            return false;
        }
    }

    /**
     * Battery Optimization - Aggressive battery saving
     */
    public static boolean isBatteryOptimizationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                KEY_BATTERY_OPTIMIZATION, 0) == 1;
    }

    public static boolean setBatteryOptimizationEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                KEY_BATTERY_OPTIMIZATION, enabled ? 1 : 0);
        if (result) {
            applyBatteryOptimization(context, enabled);
        }
        return result;
    }

    private static void applyBatteryOptimization(Context context, boolean enabled) {
        // Persist low power mode intent even when direct API calls are blocked.
        Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.LOW_POWER_MODE, enabled ? 1 : 0);
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                pm.setPowerSaveModeEnabled(enabled);
                Log.d(TAG, "Battery optimization (Power Save Mode) " + 
                    (enabled ? "enabled" : "disabled"));
            }
        } catch (SecurityException e) {
            // May require system permissions - use Settings.Global as fallback
            Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.LOW_POWER_MODE, enabled ? 1 : 0);
            Log.d(TAG, "Battery optimization set via Settings.Global (fallback)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set battery optimization", e);
        }
    }

    /**
     * Storage Optimization - Optimize storage access
     */
    public static boolean isStorageOptimizationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                KEY_STORAGE_OPTIMIZATION, 0) == 1;
    }

    public static boolean setStorageOptimizationEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                KEY_STORAGE_OPTIMIZATION, enabled ? 1 : 0);
        if (result) {
            // Enable storage optimization
            Settings.Global.putInt(context.getContentResolver(),
                    "storage_optimization_enabled", enabled ? 1 : 0);
            Log.d(TAG, "Storage optimization " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Thermal Throttling - Monitor thermal throttling status
     * Note: Can read status, but control requires framework support
     */
    public static boolean isThermalThrottlingEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                KEY_THERMAL_THROTTLING, 0) == 1;
    }

    public static boolean setThermalThrottlingEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                KEY_THERMAL_THROTTLING, enabled ? 1 : 0);
        if (result) {
            // Control thermal throttling
            Settings.Global.putInt(context.getContentResolver(),
                    "thermal_throttling_enabled", enabled ? 1 : 0);
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    // Get current thermal status for monitoring
                    int thermalStatus = pm.getCurrentThermalStatus();
                    Log.d(TAG, "Thermal throttling " + (enabled ? "enabled" : "disabled") + 
                          ", current thermal status: " + thermalStatus);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get thermal status", e);
            }
        }
        return result;
    }
    
    /**
     * Get current thermal status for display
     */
    public static int getCurrentThermalStatus(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                return pm.getCurrentThermalStatus();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get thermal status", e);
        }
        return PowerManager.THERMAL_STATUS_NONE;
    }
    
    /**
     * Get available memory for display
     */
    public static long getAvailableMemory(Context context) {
        try {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.getMemoryInfo(memInfo);
                return memInfo.availMem;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get memory info", e);
        }
        return 0;
    }

    /**
     * Get optimization status summary
     */
    public static String getOptimizationStatus(Context context) {
        int enabledCount = 0;
        if (isBackgroundAppLimitsEnabled(context)) enabledCount++;
        if (isNetworkOptimizationEnabled(context)) enabledCount++;
        if (isBatteryOptimizationEnabled(context)) enabledCount++;
        if (isStorageOptimizationEnabled(context)) enabledCount++;
        if (isThermalThrottlingEnabled(context)) enabledCount++;
        return enabledCount + " optimizations active";
    }
    
    /**
     * Get running process count for Background App Limits status
     */
    public static int getRunningProcessCount(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                return am.getRunningAppProcesses().size();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get running process count", e);
        }
        return 0;
    }
    
    /**
     * Get current battery level for Battery Optimization status
     */
    public static int getBatteryLevel(Context context) {
        try {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = context.registerReceiver(null, ifilter, 
                Context.RECEIVER_NOT_EXPORTED);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    return (level * 100) / scale;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get battery level", e);
        }
        return -1;
    }
    
    /**
     * Get available storage space for Storage Optimization status
     */
    public static long getAvailableStorage(Context context) {
        try {
            android.os.StatFs stat = new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());
            return stat.getAvailableBytes();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get available storage", e);
        }
        return 0;
    }
    
    /**
     * Get network connectivity status for Network Optimization
     */
    public static String getNetworkStatus(Context context) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                android.net.Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork != null) {
                    android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                    if (caps != null) {
                        if (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "WiFi";
                        } else if (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return "Mobile";
                        } else if (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return "Ethernet";
                        } else {
                            return "Connected";
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get network status", e);
        }
        return "Not connected";
    }
    
    /**
     * Get thermal status string for display
     */
    public static String getThermalStatusString(Context context) {
        int status = getCurrentThermalStatus(context);
        switch (status) {
            case PowerManager.THERMAL_STATUS_NONE:
                return "Normal";
            case PowerManager.THERMAL_STATUS_LIGHT:
                return "Light";
            case PowerManager.THERMAL_STATUS_MODERATE:
                return "Moderate";
            case PowerManager.THERMAL_STATUS_SEVERE:
                return "Severe";
            case PowerManager.THERMAL_STATUS_CRITICAL:
                return "Critical";
            case PowerManager.THERMAL_STATUS_EMERGENCY:
                return "Emergency";
            case PowerManager.THERMAL_STATUS_SHUTDOWN:
                return "Shutdown";
            default:
                return "Unknown";
        }
    }
}

