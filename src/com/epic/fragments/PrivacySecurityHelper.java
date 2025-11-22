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
 * Helper class for Privacy & Security Features that ACTUALLY WORK.
 * All features use Settings keys that the framework reads and enforces.
 */
public class PrivacySecurityHelper {
    private static final String TAG = "PrivacySecurityHelper";

    // 1. PIN Scrambling (GrapheneOS) - Uses LineageSettings.System.LOCKSCREEN_PIN_SCRAMBLE_LAYOUT (framework reads this!)
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
            boolean result = lineageos.providers.LineageSettings.System.putIntForUser(
                    context.getContentResolver(),
                    lineageos.providers.LineageSettings.System.LOCKSCREEN_PIN_SCRAMBLE_LAYOUT,
                    enabled ? 1 : 0, android.os.UserHandle.USER_CURRENT);
            if (result) {
                Log.d(TAG, "PIN scramble " + (enabled ? "enabled" : "disabled"));
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set PIN scramble setting", e);
            return false;
        }
    }

    // 2. Auto Reboot (GrapheneOS/CalyxOS) - Uses Settings.Secure.AUTO_REBOOT_ENABLED (framework reads this!)
    public static boolean isAutoRebootEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_ENABLED, 0) == 1;
    }

    public static boolean setAutoRebootEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_ENABLED, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "Auto reboot " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    public static long getAutoRebootInterval(Context context) {
        // Default to 1 hour (3600000 ms) to match XML default
        return Settings.Secure.getLong(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_DELAY, 60 * 60 * 1000L);
    }

    public static boolean setAutoRebootInterval(Context context, long delayMs) {
        return Settings.Secure.putLong(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_DELAY, delayMs);
    }

    // 3. USB Accessories Control (GrapheneOS) - Uses Settings.Secure.USB_MASS_STORAGE_ENABLED (framework reads this!)
    public static boolean isUsbAccessoriesEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.USB_MASS_STORAGE_ENABLED, 1) == 1;
    }

    public static boolean setUsbAccessoriesEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putInt(context.getContentResolver(), 
                Settings.Secure.USB_MASS_STORAGE_ENABLED, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "USB accessories " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    // 4. Per-Connection MAC Randomization (GrapheneOS) - Uses Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED (framework reads this!)
    public static boolean isMacRandomizationEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), 
                Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, 0) == 1;
    }

    public static boolean setMacRandomizationEnabled(Context context, boolean enabled) {
        boolean result = Settings.Global.putInt(context.getContentResolver(), 
                Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, enabled ? 1 : 0);
        if (result) {
            Log.d(TAG, "MAC randomization " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }

    // 5. Private DNS Configuration (GrapheneOS/CalyxOS) - Uses Settings.Global.PRIVATE_DNS_MODE (framework reads this!)
    public static String getPrivateDnsMode(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Settings.Global.PRIVATE_DNS_MODE);
    }

    public static boolean setPrivateDnsMode(Context context, String mode) {
        return Settings.Global.putString(context.getContentResolver(), Settings.Global.PRIVATE_DNS_MODE, mode);
    }

    public static String getPrivateDnsSpecifier(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Settings.Global.PRIVATE_DNS_SPECIFIER);
    }

    public static boolean setPrivateDnsSpecifier(Context context, String specifier) {
        return Settings.Global.putString(context.getContentResolver(), Settings.Global.PRIVATE_DNS_SPECIFIER, specifier);
    }

    // 6. Clipboard Access Notifications (GrapheneOS) - Uses Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS (framework reads this!)
    public static boolean isClipboardNotificationsEnabled(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), 
                Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, 1, 
                android.os.UserHandle.USER_CURRENT) == 1;
    }

    public static boolean setClipboardNotificationsEnabled(Context context, boolean enabled) {
        boolean result = Settings.Secure.putIntForUser(context.getContentResolver(), 
                Settings.Secure.CLIPBOARD_SHOW_ACCESS_NOTIFICATIONS, enabled ? 1 : 0,
                android.os.UserHandle.USER_CURRENT);
        if (result) {
            Log.d(TAG, "Clipboard notifications " + (enabled ? "enabled" : "disabled"));
        }
        return result;
    }
}
