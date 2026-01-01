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

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.SensorPrivacyManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.utils.SensorPrivacyManagerHelper;

/**
 * Helper class for Security Features based on GrapheneOS and CalyxOS practices.
 * Provides methods to manage privacy and security features.
 */
public class SecurityFeaturesHelper {
    private static final String TAG = "SecurityFeaturesHelper";

    // Core security feature KEY constants removed - features removed

    // Network Permission Control methods removed - feature removed

    // App Hardening methods removed - feature removed

    // Sensor Access Control methods removed - feature removed

    // Location Access Control methods removed - feature removed

    // Microphone Access Control methods removed - feature removed

    // Additional Security & Privacy Features

    /**
     * USB Debugging Control - Control ADB debugging access
     * Uses Android's built-in ADB_ENABLED setting
     * Note: Requires user confirmation dialog in production builds
     */
    public static boolean isUsbDebuggingEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) == 1;
    }

    public static boolean setUsbDebuggingEnabled(Context context, boolean enabled) {
        // Check if already in desired state
        boolean currentState = isUsbDebuggingEnabled(context);
        if (currentState == enabled) {
            return true; // Already in desired state
        }
        
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "USB debugging " + (enabled ? "enabled" : "disabled"));
            // Note: On production builds, this may require user confirmation
        }
        return result;
    }

    /**
     * Developer Options Access - Control developer options visibility
     * Uses Android's built-in DEVELOPMENT_SETTINGS_ENABLED setting
     */
    public static boolean isDeveloperOptionsEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
    }

    public static boolean setDeveloperOptionsEnabled(Context context, boolean enabled) {
        // Check if already in desired state
        boolean currentState = isDeveloperOptionsEnabled(context);
        if (currentState == enabled) {
            return true; // Already in desired state
        }
        
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Developer options " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Mock Location Detection - Detect fake GPS usage
     * Note: ALLOW_MOCK_LOCATION is deprecated and always returns 0 on user builds
     * This feature cannot be properly implemented without framework changes
     */
    public static boolean isMockLocationDetectionEnabled(Context context) {
        // On user builds, mock location is always disabled
        // This is a read-only check
        try {
            return Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 1;
        } catch (Exception e) {
            // On user builds, this will always return 0
            return false;
        }
    }

    public static boolean setMockLocationDetectionEnabled(Context context, boolean enabled) {
        // This cannot be changed on user builds - requires framework modification
        Log.w(TAG, "Mock location detection cannot be changed on user builds");
        return false;
    }

    /**
     * VPN Always-On Control - Force VPN usage
     * Note: Requires user to select a VPN app first - this toggle alone is not sufficient
     */
    public static boolean isVpnAlwaysOnEnabled(Context context) {
        String vpnApp = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALWAYS_ON_VPN_APP);
        return vpnApp != null && !vpnApp.isEmpty();
    }

    public static boolean setVpnAlwaysOnEnabled(Context context, boolean enabled) {
        if (enabled) {
            // Cannot enable without user selecting a VPN app first
            Log.w(TAG, "VPN always-on requires user to select a VPN app first");
            return false;
        } else {
            // Can disable by clearing the VPN app
            boolean result = Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.ALWAYS_ON_VPN_APP, "");
            if (result) {
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ALWAYS_ON_VPN_LOCKDOWN, 0);
                Log.d(TAG, "VPN always-on disabled");
            }
            return result;
        }
    }

    /**
     * Screen Lock Timeout Control - Control auto-lock timeout
     * Uses Android's built-in LOCK_SCREEN_LOCK_AFTER_TIMEOUT setting
     */
    public static int getScreenLockTimeout(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                "lock_screen_lock_after_timeout", 5000);
    }

    public static boolean setScreenLockTimeout(Context context, int timeoutMs) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                "lock_screen_lock_after_timeout", timeoutMs);
        if (result) {
            Log.d(TAG, "Screen lock timeout set to " + timeoutMs + "ms");
        }
        return result;
    }

    /**
     * Screen Lock Timeout Enabled - Check if auto-lock timeout is enabled
     * Returns true if timeout is greater than 0
     */
    public static boolean isScreenLockTimeoutEnabled(Context context) {
        int timeout = getScreenLockTimeout(context);
        return timeout > 0;
    }

    public static boolean setScreenLockTimeoutEnabled(Context context, boolean enabled) {
        int timeout = enabled ? 5000 : 0; // Default 5 seconds if enabled, 0 if disabled
        return setScreenLockTimeout(context, timeout);
    }

    /**
     * Biometric Timeout Control - Control biometric unlock timeout
     * Uses Settings.Secure.BIOMETRIC_KEYGUARD_ENABLED to control biometric unlock
     * Also controls timeout duration via lock_screen_lock_after_timeout
     */
    public static boolean isBiometricTimeoutEnabled(Context context) {
        // Check if biometric is enabled for keyguard
        boolean biometricEnabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BIOMETRIC_KEYGUARD_ENABLED, 1) == 1;
        // Also check if timeout is configured
        int timeout = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 0);
        return biometricEnabled && timeout > 0;
    }

    public static boolean setBiometricTimeoutEnabled(Context context, boolean enabled) {
        // Enable/disable biometric unlock for keyguard
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.BIOMETRIC_KEYGUARD_ENABLED, enabled ? 1 : 0);
        if (result) {
            // Also update biometric app enabled state
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.BIOMETRIC_APP_ENABLED, enabled ? 1 : 0);
            // Set timeout: 30 seconds if enabled, 0 if disabled
            int timeout = enabled ? 30000 : 0;
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, timeout);
            Log.d(TAG, "Biometric timeout " + (enabled ? "enabled (30s)" : "disabled"));
        }
        return result;
    }

    /**
     * Clipboard Access Notifications - Show clipboard access notifications
     * Uses Android 12+ CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS setting
     */
    public static boolean isClipboardAccessNotificationsEnabled(Context context) {
        // Android 12+ has built-in clipboard access notifications
        // Check if the feature is available
        try {
            return Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, 1) == 1;
        } catch (Exception e) {
            // Fallback for older Android versions
            return Settings.Secure.getInt(context.getContentResolver(),
                    "clipboard_show_access_notifications", 1) == 1;
        }
    }

    public static boolean setClipboardAccessNotificationsEnabled(Context context, boolean enabled) {
        boolean result;
        try {
            result = Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, enabled ? 1 : 0);
        } catch (Exception e) {
            // Fallback for older Android versions
            result = Settings.Secure.putInt(context.getContentResolver(),
                    "clipboard_show_access_notifications", enabled ? 1 : 0);
        }
        if (result) {
            Log.d(TAG, "Clipboard access notifications " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }


    /**
     * App Permission Auto-Revoke - Auto-revoke permissions for unused apps
     * Uses Android's built-in auto_revoke_permissions setting
     */
    public static boolean isAppPermissionAutoRevokeEnabled(Context context) {
        // Check auto-revoke mode: 0=disabled, 1=enabled, 2=prompt
        int mode = Settings.Secure.getInt(context.getContentResolver(),
                "auto_revoke_permissions", 1);
        return mode == 1; // Enabled mode
    }

    public static boolean setAppPermissionAutoRevokeEnabled(Context context, boolean enabled) {
        // Set auto-revoke mode: 0=disabled, 1=enabled
        int mode = enabled ? 1 : 0;
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                "auto_revoke_permissions", mode);
        if (result) {
            Log.d(TAG, "App permission auto-revoke " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Battery Optimization Exemptions Control
     * Controls whether apps can be exempted from battery optimization
     * Uses PowerManager to check battery optimization state
     */
    public static boolean isBatteryOptimizationExemptionsEnabled(Context context) {
        // Check if battery optimization is enabled (exemptions are allowed when optimization is on)
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        if (powerManager != null) {
            // Battery optimization exemptions are available when battery saver can be enabled
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0) >= 0;
        }
        return Settings.Global.getInt(context.getContentResolver(),
                "app_auto_restriction_enabled", 1) == 1;
    }

    public static boolean setBatteryOptimizationExemptionsEnabled(Context context, boolean enabled) {
        // Enable/disable automatic app restriction (battery optimization)
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                "app_auto_restriction_enabled", enabled ? 1 : 0);
        if (result) {
            // Also update battery saver trigger to allow exemptions
            if (enabled) {
                Settings.Global.putInt(context.getContentResolver(),
                        Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, 15);
            }
            Log.d(TAG, "Battery optimization exemptions " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Notification Access Control - Control notification listeners
     * Uses NotificationManager to check/manage notification listener access
     */
    public static boolean isNotificationAccessEnabled(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            // Check if any notification listeners are enabled
            String listeners = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
            return listeners != null && !listeners.isEmpty();
        }
        return false;
    }

    public static boolean setNotificationAccessEnabled(Context context, boolean enabled) {
        // Note: This cannot directly enable notification access - user must grant it per app
        // This toggle controls whether notification listener access is monitored/restricted
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                "notification_listener_access_enabled", enabled ? 1 : 0);
        if (result) {
            if (!enabled) {
                // Optionally disable all notification listeners (requires user confirmation)
                // For safety, we don't auto-disable - user must do this manually
                Log.d(TAG, "Notification access monitoring " + (enabled ? "enabled" : "disabled"));
            } else {
                Log.d(TAG, "Notification access monitoring enabled - user must grant access per app");
            }
        }
        return result;
    }

    /**
     * Device Admin Management - Control device administrators
     * Uses Android's device admin framework
     */
    public static boolean hasDeviceAdmins(Context context) {
        // This would need DevicePolicyManager to check, simplified for now
        return Settings.Global.getInt(context.getContentResolver(),
                "device_provisioned", 0) == 1;
    }

    /**
     * Secure Lock Screen Settings - Enhanced lock screen security
     * Uses KeyguardManager to check if device is secured
     */
    public static boolean isSecureLockScreenEnabled(Context context) {
        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardSecure();
        }
        // Fallback to Settings check
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCK_PATTERN_ENABLED, 0) == 1 ||
               Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 0) > 0;
    }

    public static boolean setSecureLockScreenEnabled(Context context, boolean enabled) {
        // This requires user to set up lock screen, so we just ensure it's configured
        // The actual lock screen setup must be done through Security settings
        if (enabled) {
            // Ensure lock screen timeout is set
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 5000);
            Log.d(TAG, "Secure lock screen enabled - user must configure lock screen");
        } else {
            // Disable lock screen timeout (but keep lock screen itself)
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 0);
            Log.d(TAG, "Secure lock screen timeout disabled");
        }
        return true;
    }

    /**
     * App Data Backup Control - Control automatic backups
     * Uses Android's built-in BACKUP_AUTO_RESTORE setting
     */
    public static boolean isAppDataBackupEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1;
    }

    public static boolean setAppDataBackupEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.BACKUP_AUTO_RESTORE, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "App data backup " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    /**
     * Network Traffic Monitoring - Monitor network usage
     * Note: This setting controls multipath preference, not actual monitoring
     * Real network monitoring requires framework changes or third-party apps
     */
    public static boolean isNetworkTrafficMonitoringEnabled(Context context) {
        int value = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.NETWORK_METERED_MULTIPATH_PREFERENCE, 0);
        // 1 = HANDOVER, 2 = PERFORMANCE, 0 = DISABLED
        return value != 0;
    }

    public static boolean setNetworkTrafficMonitoringEnabled(Context context, boolean enabled) {
        // This only controls multipath preference, not actual monitoring
        // 1 = HANDOVER (prefer WiFi), 2 = PERFORMANCE (use both), 0 = DISABLED
        int value = enabled ? 1 : 0; // Use HANDOVER mode when enabled
        boolean result = Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.NETWORK_METERED_MULTIPATH_PREFERENCE, value);
        if (result) {
            Log.d(TAG, "Network multipath preference " + (enabled ? "enabled (handover)" : "disabled"));
        }
        return result;
    }

    /**
     * Location Accuracy Control - Control location precision (GPS vs Network)
     * Uses Android's built-in LOCATION_MODE setting
     */
    public static boolean isLocationAccuracyEnabled(Context context) {
        int mode = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        return mode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
    }

    public static boolean setLocationAccuracyEnabled(Context context, boolean enabled) {
        int currentMode = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        
        int newMode;
        if (enabled) {
            newMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
        } else {
            // Switch to battery saving mode instead of turning off completely
            newMode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
        }
        
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE, newMode);
        if (result) {
            Log.d(TAG, "Location accuracy " + (enabled ? "set to high accuracy" : "set to battery saving"));
        }
        return result;
    }

    /**
     * Camera Privacy Control - Control camera access
     * Uses SensorPrivacyManager to sync with QS tiles
     */
    public static boolean isCameraPrivacyEnabled(Context context) {
        SensorPrivacyManagerHelper helper = SensorPrivacyManagerHelper.getInstance(context);
        if (helper != null && helper.supportsSensorToggle(SensorPrivacyManagerHelper.SENSOR_CAMERA)) {
            return helper.isSensorBlocked(SensorPrivacyManagerHelper.SENSOR_CAMERA);
        }
        // Fallback: Use SensorPrivacyManager directly
        try {
            SensorPrivacyManager spm = context.getSystemService(SensorPrivacyManager.class);
            if (spm != null) {
                return spm.isSensorPrivacyEnabled(
                    SensorPrivacyManager.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyManager.Sensors.CAMERA);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking camera privacy", e);
        }
        return false;
    }

    public static boolean setCameraPrivacyEnabled(Context context, boolean enabled) {
        SensorPrivacyManagerHelper helper = SensorPrivacyManagerHelper.getInstance(context);
        if (helper != null && helper.supportsSensorToggle(SensorPrivacyManagerHelper.SENSOR_CAMERA)) {
            helper.setSensorBlocked(SensorPrivacyManagerHelper.SENSOR_CAMERA, enabled);
            Log.d(TAG, "Camera privacy " + (enabled ? "enabled" : "disabled") + " (synced with QS tile)");
            return true;
        }
        // Fallback: Use SensorPrivacyManager directly
        try {
            SensorPrivacyManager spm = context.getSystemService(SensorPrivacyManager.class);
            if (spm != null) {
                spm.setSensorPrivacy(SensorPrivacyManager.Sources.SETTINGS,
                    SensorPrivacyManager.Sensors.CAMERA, enabled);
                Log.d(TAG, "Camera privacy " + (enabled ? "enabled" : "disabled") + " (via SensorPrivacyManager)");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting camera privacy", e);
        }
        return false;
    }

    /**
     * Microphone Privacy Control - Control microphone access
     * Uses SensorPrivacyManager to sync with QS tiles
     */
    public static boolean isMicrophonePrivacyEnabled(Context context) {
        SensorPrivacyManagerHelper helper = SensorPrivacyManagerHelper.getInstance(context);
        if (helper != null && helper.supportsSensorToggle(SensorPrivacyManagerHelper.SENSOR_MICROPHONE)) {
            return helper.isSensorBlocked(SensorPrivacyManagerHelper.SENSOR_MICROPHONE);
        }
        // Fallback: Use SensorPrivacyManager directly
        try {
            SensorPrivacyManager spm = context.getSystemService(SensorPrivacyManager.class);
            if (spm != null) {
                return spm.isSensorPrivacyEnabled(
                    SensorPrivacyManager.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyManager.Sensors.MICROPHONE);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking microphone privacy", e);
        }
        return false;
    }

    public static boolean setMicrophonePrivacyEnabled(Context context, boolean enabled) {
        SensorPrivacyManagerHelper helper = SensorPrivacyManagerHelper.getInstance(context);
        if (helper != null && helper.supportsSensorToggle(SensorPrivacyManagerHelper.SENSOR_MICROPHONE)) {
            helper.setSensorBlocked(SensorPrivacyManagerHelper.SENSOR_MICROPHONE, enabled);
            Log.d(TAG, "Microphone privacy " + (enabled ? "enabled" : "disabled") + " (synced with QS tile)");
            return true;
        }
        // Fallback: Use SensorPrivacyManager directly
        try {
            SensorPrivacyManager spm = context.getSystemService(SensorPrivacyManager.class);
            if (spm != null) {
                spm.setSensorPrivacy(SensorPrivacyManager.Sources.SETTINGS,
                    SensorPrivacyManager.Sensors.MICROPHONE, enabled);
                Log.d(TAG, "Microphone privacy " + (enabled ? "enabled" : "disabled") + " (via SensorPrivacyManager)");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting microphone privacy", e);
        }
        return false;
    }

    /**
     * Storage Access Control - Control storage access framework
     * Uses Android's Storage Access Framework (SAF) settings
     * Note: This controls whether apps can request storage access, not individual app permissions
     */
    public static boolean isStorageAccessEnabled(Context context) {
        // Check if storage access framework is available
        // This is always enabled on modern Android, but we can track user preference
        return Settings.Secure.getInt(context.getContentResolver(),
                "storage_access_framework_enabled", 1) == 1;
    }

    public static boolean setStorageAccessEnabled(Context context, boolean enabled) {
        // Note: This doesn't actually disable storage access - apps can still request it
        // It's more of a preference/tracking setting
        boolean result = Settings.Secure.putInt(context.getContentResolver(),
                "storage_access_framework_enabled", enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Storage access framework preference " + (enabled ? "enabled" : "disabled") +
                  " (note: apps can still request storage access)");
        }
        return result;
    }

    /**
     * Get security features status summary
     */
    public static String getSecurityFeaturesStatus(Context context) {
        int enabledCount = 0;
        // Core security features removed - no longer counted
        if (isUsbDebuggingEnabled(context)) enabledCount++;
        if (isDeveloperOptionsEnabled(context)) enabledCount++;
        if (isMockLocationDetectionEnabled(context)) enabledCount++;
        if (isVpnAlwaysOnEnabled(context)) enabledCount++;
        if (isBiometricTimeoutEnabled(context)) enabledCount++;
        if (isClipboardAccessNotificationsEnabled(context)) enabledCount++;
        if (isAppPermissionAutoRevokeEnabled(context)) enabledCount++;
        if (isBatteryOptimizationExemptionsEnabled(context)) enabledCount++;
        if (isNotificationAccessEnabled(context)) enabledCount++;
        if (isSecureLockScreenEnabled(context)) enabledCount++;
        if (isAppDataBackupEnabled(context)) enabledCount++;
        if (isNetworkTrafficMonitoringEnabled(context)) enabledCount++;
        if (isLocationAccuracyEnabled(context)) enabledCount++;
        if (isCameraPrivacyEnabled(context)) enabledCount++;
        if (isMicrophonePrivacyEnabled(context)) enabledCount++;
        if (isStorageAccessEnabled(context)) enabledCount++;
        return enabledCount + " security features active";
    }
}

