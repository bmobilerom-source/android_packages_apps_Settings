/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.bmobile.fragments;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for Power Tweaks based on crDroid and Axion A16 features.
 * Simple tweaks that won't break the build.
 */
public class PowerTweaksHelper {
    private static final String TAG = "PowerTweaksHelper";

    // Settings.System keys for power tweaks
    // crDroid features (5)
    private static final String KEY_FAST_CHARGING = "fast_charging_enabled";
    private static final String KEY_CHARGING_LED = "charging_led_enabled";
    private static final String KEY_BATTERY_SAVER_AUTO = "battery_saver_auto_enabled";
    private static final String KEY_WAKE_ON_CHARGE = "wake_on_charge_enabled";
    private static final String KEY_CHARGING_SOUND = "charging_sound_enabled";
    
    // Axion A16 features (5)
    private static final String KEY_SMART_CHARGING = "smart_charging_enabled";
    private static final String KEY_BATTERY_CALIBRATION = "battery_calibration_enabled";
    private static final String KEY_POWER_EFFICIENT_MODE = "power_efficient_mode_enabled";
    private static final String KEY_SCREEN_OFF_OPTIMIZATION = "screen_off_optimization_enabled";
    private static final String KEY_CHARGING_ANIMATION = "charging_animation_enabled";

    // Standard Android feature methods removed - use built-in Settings implementations

    /**
     * Enable/disable fast charging
     * Uses Settings.System key for fast charging control
     */
    public static boolean isFastChargingEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_FAST_CHARGING, 1) == 1;
    }

    public static boolean setFastChargingEnabled(Context context, boolean enabled) {
        boolean result = Settings.System.putInt(context.getContentResolver(), KEY_FAST_CHARGING, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Fast charging " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Stay awake when charging - Keep screen on while plugged in
     * Uses Settings.Global.STAY_ON_WHILE_PLUGGED_IN
     * Values: 0=off, 1=AC, 2=USB, 4=Wireless, 7=All
     */
    public static boolean isStayAwakeWhenChargingEnabled(Context context) {
        int stayOn = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
        // Check if stay on is enabled for any charging method
        return stayOn > 0;
    }

    public static boolean setStayAwakeWhenChargingEnabled(Context context, boolean enabled) {
        // Enable for AC (1), USB (2), and Wireless (4) = 7 (all)
        int flags = enabled ? 7 : 0;
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, flags);
        if (result) {
            Log.d(TAG, "Stay awake when charging " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Adaptive Battery - Learn from app usage to optimize battery
     * Uses Settings.Global.ADAPTIVE_BATTERY_MANAGEMENT_ENABLED
     */
    public static boolean isAdaptiveBatteryEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                "adaptive_battery_management_enabled", 1) == 1;
    }

    public static boolean setAdaptiveBatteryEnabled(Context context, boolean enabled) {
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                "adaptive_battery_management_enabled", enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Adaptive battery " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Get screen timeout in milliseconds
     */
    public static int getScreenTimeout(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 30000); // Default 30 seconds
    }

    /**
     * Set screen timeout in milliseconds
     */
    public static boolean setScreenTimeout(Context context, int timeoutMs) {
        boolean result = Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, timeoutMs);
        if (result) {
            Log.d(TAG, "Screen timeout set to " + timeoutMs + "ms");
        }
        return result;
    }
}

