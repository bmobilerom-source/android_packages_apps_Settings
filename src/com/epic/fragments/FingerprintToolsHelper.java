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

/**
 * Helper class for Fingerprint Tools Options.
 * Manages fingerprint authentication ripple effects and vibration settings.
 */
public class FingerprintToolsHelper {

    /**
     * Check if authentication ripple effect is enabled.
     */
    public static boolean isAuthRippleEnabled(Context context) {
        if (context == null) {
            return true; // Default enabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, "auth_ripple_enabled", 1) == 1;
    }

    /**
     * Set authentication ripple effect enabled state.
     */
    public static boolean setAuthRippleEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver, "auth_ripple_enabled", enabled ? 1 : 0);
    }

    /**
     * Check if fingerprint success vibration is enabled.
     */
    public static boolean isFpSuccessVibrateEnabled(Context context) {
        if (context == null) {
            return true; // Default enabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, "fp_success_vibrate", 1) == 1;
    }

    /**
     * Set fingerprint success vibration enabled state.
     */
    public static boolean setFpSuccessVibrateEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver, "fp_success_vibrate", enabled ? 1 : 0);
    }

    /**
     * Check if fingerprint error vibration is enabled.
     */
    public static boolean isFpErrorVibrateEnabled(Context context) {
        if (context == null) {
            return true; // Default enabled
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, "fp_error_vibrate", 1) == 1;
    }

    /**
     * Set fingerprint error vibration enabled state.
     */
    public static boolean setFpErrorVibrateEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putInt(resolver, "fp_error_vibrate", enabled ? 1 : 0);
    }
}

