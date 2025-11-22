/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.epic.fragments;

import android.content.ContentResolver;
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

    // crDroid Features
    public static boolean isFastChargingEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_FAST_CHARGING, 0) == 1;
    }

    public static boolean setFastChargingEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_FAST_CHARGING, enabled ? 1 : 0);
        if (result) {
            Settings.Global.putInt(resolver, "fast_charging_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_FAST_CHARGING);
            Log.d(TAG, "Fast charging " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isChargingLedEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_CHARGING_LED, 1) == 1;
    }

    public static boolean setChargingLedEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_CHARGING_LED, enabled ? 1 : 0);
        if (result) {
            Settings.System.putInt(resolver, "charging_led_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_CHARGING_LED);
            Log.d(TAG, "Charging LED " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isBatterySaverAutoEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_BATTERY_SAVER_AUTO, 0) == 1;
    }

    public static boolean setBatterySaverAutoEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_BATTERY_SAVER_AUTO, enabled ? 1 : 0);
        if (result) {
            // Auto-enable battery saver at 15% battery
            Settings.Global.putInt(resolver, "low_power_trigger_level", enabled ? 15 : 0);
            notifyPowerTweakChange(resolver, KEY_BATTERY_SAVER_AUTO);
            Log.d(TAG, "Auto battery saver " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isWakeOnChargeEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_WAKE_ON_CHARGE, 0) == 1;
    }

    public static boolean setWakeOnChargeEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_WAKE_ON_CHARGE, enabled ? 1 : 0);
        if (result) {
            Settings.System.putInt(resolver, "wake_on_charge", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_WAKE_ON_CHARGE);
            Log.d(TAG, "Wake on charge " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isChargingSoundEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_CHARGING_SOUND, 1) == 1;
    }

    public static boolean setChargingSoundEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_CHARGING_SOUND, enabled ? 1 : 0);
        if (result) {
            Settings.System.putInt(resolver, "charging_sound_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_CHARGING_SOUND);
            Log.d(TAG, "Charging sound " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    // Axion A16 Features
    public static boolean isSmartChargingEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_SMART_CHARGING, 0) == 1;
    }

    public static boolean setSmartChargingEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_SMART_CHARGING, enabled ? 1 : 0);
        if (result) {
            // Smart charging: slow down charging when battery is above 80%
            Settings.Global.putInt(resolver, "smart_charging_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_SMART_CHARGING);
            Log.d(TAG, "Smart charging " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isBatteryCalibrationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_BATTERY_CALIBRATION, 0) == 1;
    }

    public static boolean setBatteryCalibrationEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_BATTERY_CALIBRATION, enabled ? 1 : 0);
        if (result) {
            Settings.Global.putInt(resolver, "battery_calibration_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_BATTERY_CALIBRATION);
            Log.d(TAG, "Battery calibration " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isPowerEfficientModeEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_POWER_EFFICIENT_MODE, 0) == 1;
    }

    public static boolean setPowerEfficientModeEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_POWER_EFFICIENT_MODE, enabled ? 1 : 0);
        if (result) {
            Settings.Global.putInt(resolver, "power_efficient_mode", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_POWER_EFFICIENT_MODE);
            Log.d(TAG, "Power efficient mode " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isScreenOffOptimizationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_SCREEN_OFF_OPTIMIZATION, 0) == 1;
    }

    public static boolean setScreenOffOptimizationEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_SCREEN_OFF_OPTIMIZATION, enabled ? 1 : 0);
        if (result) {
            Settings.Global.putInt(resolver, "screen_off_optimization", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_SCREEN_OFF_OPTIMIZATION);
            Log.d(TAG, "Screen off optimization " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static boolean isChargingAnimationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY_CHARGING_ANIMATION, 1) == 1;
    }

    public static boolean setChargingAnimationEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean result = Settings.System.putInt(resolver, KEY_CHARGING_ANIMATION, enabled ? 1 : 0);
        if (result) {
            Settings.System.putInt(resolver, "charging_animation_enabled", enabled ? 1 : 0);
            notifyPowerTweakChange(resolver, KEY_CHARGING_ANIMATION);
            Log.d(TAG, "Charging animation " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Notify SystemUI of power tweak changes
     */
    private static void notifyPowerTweakChange(ContentResolver resolver, String key) {
        try {
            resolver.notifyChange(Settings.System.getUriFor(key), null, true);
            Log.d(TAG, "Notified SystemUI of power tweak change: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify power tweak change: " + key, e);
        }
    }
}

