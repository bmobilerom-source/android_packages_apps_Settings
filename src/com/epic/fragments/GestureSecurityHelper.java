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

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class for Gesture Security Options.
 * Manages clipboard overlay, storage restrictions, window secure flags, and lock screen QS settings.
 */
public class GestureSecurityHelper {

    /**
     * Check if clipboard overlay is enabled.
     */
    public static boolean isClipboardOverlayEnabled(Context context) {
        if (context == null) {
            return true; // Default enabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.Secure.getInt(resolver, "show_clipboard_overlay", 1) == 1;
    }

    /**
     * Set clipboard overlay enabled state.
     */
    public static boolean setClipboardOverlayEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.Secure.putInt(resolver, "show_clipboard_overlay", enabled ? 1 : 0);
        if (success) {
            notifyGestureSecurityChange(resolver, Settings.Secure.getUriFor("show_clipboard_overlay"));
        }
        return success;
    }

    /**
     * Check if storage restrictions are disabled (no storage restrict).
     */
    public static boolean isNoStorageRestrictEnabled(Context context) {
        if (context == null) {
            return false; // Default disabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.Global.getInt(resolver, "no_storage_restrict", 0) == 1;
    }

    /**
     * Set storage restrictions disabled state.
     */
    public static boolean setNoStorageRestrictEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.Global.putInt(resolver, "no_storage_restrict", enabled ? 1 : 0);
    }

    /**
     * Check if window secure flags are ignored.
     */
    public static boolean isWindowIgnoreSecureEnabled(Context context) {
        if (context == null) {
            return false; // Default disabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.Global.getInt(resolver, "window_ignore_secure", 0) == 1;
    }

    /**
     * Set window secure flags ignored state.
     */
    public static boolean setWindowIgnoreSecureEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.Global.putInt(resolver, "window_ignore_secure", enabled ? 1 : 0);
    }

    /**
     * Check if secure lock screen QS is disabled.
     */
    public static boolean isSecureLockscreenQsDisabled(Context context) {
        if (context == null) {
            return false; // Default disabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, "secure_lockscreen_qs_disabled", 0) == 1;
    }

    /**
     * Set secure lock screen QS disabled state.
     */
    public static boolean setSecureLockscreenQsDisabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putInt(resolver, "secure_lockscreen_qs_disabled", enabled ? 1 : 0);
        if (success) {
            notifyGestureSecurityChange(resolver, Settings.System.getUriFor("secure_lockscreen_qs_disabled"));
        }
        return success;
    }

    /**
     * Notify SystemUI of gesture security changes
     */
    private static void notifyGestureSecurityChange(ContentResolver resolver, android.net.Uri uri) {
        try {
            resolver.notifyChange(uri, null, true);
            Log.d("GestureSecurityHelper", "Notified SystemUI of gesture security change");
        } catch (Exception e) {
            Log.e("GestureSecurityHelper", "Failed to notify gesture security change", e);
        }
    }
}


