/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/** Secure settings helpers for bMobile privacy features (auto-reboot, USB, DNS, etc.). */
public final class PrivacySecurityHelper {
    private static final String TAG = "PrivacySecurityHelper";

    private PrivacySecurityHelper() {}

    public static boolean isPinScrambleEnabled(Context context) {
        try {
            return lineageos.providers.LineageSettings.System.getIntForUser(
                    context.getContentResolver(),
                    lineageos.providers.LineageSettings.System.LOCKSCREEN_PIN_SCRAMBLE_LAYOUT,
                    0, android.os.UserHandle.USER_CURRENT) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get PIN scramble setting", e);
            return false;
        }
    }

    public static boolean setPinScrambleEnabled(Context context, boolean enabled) {
        try {
            return lineageos.providers.LineageSettings.System.putIntForUser(
                    context.getContentResolver(),
                    lineageos.providers.LineageSettings.System.LOCKSCREEN_PIN_SCRAMBLE_LAYOUT,
                    enabled ? 1 : 0, android.os.UserHandle.USER_CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set PIN scramble setting", e);
            return false;
        }
    }

    public static boolean isAutoRebootEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_ENABLED, 0) == 1;
    }

    public static boolean setAutoRebootEnabled(Context context, boolean enabled) {
        return Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_ENABLED, enabled ? 1 : 0);
    }

    public static long getAutoRebootInterval(Context context) {
        return Settings.Secure.getLong(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_DELAY, 60L * 60L * 1000L);
    }

    public static boolean setAutoRebootInterval(Context context, long delayMs) {
        return Settings.Secure.putLong(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_DELAY, delayMs);
    }

    public static boolean isUsbAccessoriesEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.USB_MASS_STORAGE_ENABLED, 1) == 1;
    }

    public static boolean setUsbAccessoriesEnabled(Context context, boolean enabled) {
        return Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.USB_MASS_STORAGE_ENABLED, enabled ? 1 : 0);
    }

    public static boolean isMacRandomizationEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, 0) == 1;
    }

    public static boolean setMacRandomizationEnabled(Context context, boolean enabled) {
        return Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, enabled ? 1 : 0);
    }

    public static String getPrivateDnsMode(Context context) {
        return Settings.Global.getString(context.getContentResolver(),
                Settings.Global.PRIVATE_DNS_MODE);
    }

    public static boolean setPrivateDnsMode(Context context, String mode) {
        return Settings.Global.putString(context.getContentResolver(),
                Settings.Global.PRIVATE_DNS_MODE, mode);
    }

    public static String getPrivateDnsSpecifier(Context context) {
        return Settings.Global.getString(context.getContentResolver(),
                Settings.Global.PRIVATE_DNS_SPECIFIER);
    }

    public static boolean setPrivateDnsSpecifier(Context context, String specifier) {
        return Settings.Global.putString(context.getContentResolver(),
                Settings.Global.PRIVATE_DNS_SPECIFIER, specifier);
    }

    public static boolean isClipboardNotificationsEnabled(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(),
                Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, 1,
                android.os.UserHandle.USER_CURRENT) == 1;
    }

    public static boolean setClipboardNotificationsEnabled(Context context, boolean enabled) {
        return Settings.Secure.putIntForUser(context.getContentResolver(),
                Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, enabled ? 1 : 0,
                android.os.UserHandle.USER_CURRENT);
    }
}
