/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;

public final class AmbientCustomizationsHelper {

    private static final String TAG = "AmbientCustomizationsHelper";

    public static final String AMBIENT_TEXT_STRING = "ambient_text_string";
    public static final String AMBIENT_TEXT_ALIGNMENT = "ambient_text_alignment";
    public static final String AMBIENT_TEXT_TYPE_COLOR = "ambient_text_type_color";
    public static final String AMBIENT_TEXT_COLOR = "ambient_text_color";
    public static final String AMBIENT_CUSTOM_IMAGE = "ambient_custom_image";
    public static final String AMBIENT_IMAGE_FILE = "ambient_image_file";

    public static final String SETTING_AMBIENT_TEXT = "ambient_text";
    public static final String SETTING_AMBIENT_TEXT_ANIMATION = "ambient_text_animation";
    public static final String SETTING_AMBIENT_TEXT_SIZE = "ambient_text_size";
    public static final String SETTING_AMBIENT_IMAGE = "ambient_image";

    private static final String DEFAULT_AMBIENT_TEXT = "";
    private static final int DEFAULT_TEXT_ALIGNMENT = 0;
    private static final int DEFAULT_TEXT_TYPE_COLOR = 0;
    private static final int DEFAULT_TEXT_COLOR = 0xFF3980FF;
    private static final String DEFAULT_CUSTOM_IMAGE = "";
    private static final int DEFAULT_TEXT_SIZE = 30;

    private AmbientCustomizationsHelper() {
    }

    /** Enable Always-on display defaults the first time ambient customization is opened. */
    public static void ensureAmbientDefaults(Context context) {
        if (context == null) {
            return;
        }
        final ContentResolver resolver = context.getContentResolver();
        try {
            if (Settings.Secure.getIntForUser(resolver, Settings.Secure.DOZE_ENABLED, 1,
                    UserHandle.USER_CURRENT) == 0) {
                Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_ENABLED, 1,
                        UserHandle.USER_CURRENT);
            }
            if (Settings.Secure.getIntForUser(resolver, Settings.Secure.DOZE_ALWAYS_ON, 0,
                    UserHandle.USER_CURRENT) == 0) {
                Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_ALWAYS_ON, 1,
                        UserHandle.USER_CURRENT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply ambient defaults", e);
        }
    }

    /** Experimental overlay features disabled until custom AOD clock / visualizer are fixed. */
    public static void disableExperimentalAmbientFeatures(Context context) {
        if (context == null) {
            return;
        }
        try {
            Settings.Secure.putInt(context.getContentResolver(), "aod_clock_style", 0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to reset aod_clock_style", e);
        }
    }

    /** Master switch: Always-on display plus custom ambient text overlay. */
    public static boolean isAmbientDisplayEnabled(Context context) {
        if (context == null) {
            return false;
        }
        final ContentResolver resolver = context.getContentResolver();
        final boolean alwaysOn = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        return alwaysOn && isAmbientTextEnabled(context);
    }

    public static boolean setAmbientDisplayEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        boolean success = true;
        if (enabled) {
            ensureAmbientDefaults(context);
        }
        success &= setAmbientTextEnabled(context, enabled);
        try {
            success &= Settings.Secure.putIntForUser(context.getContentResolver(),
                    Settings.Secure.DOZE_ALWAYS_ON, enabled ? 1 : 0, UserHandle.USER_CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set DOZE_ALWAYS_ON", e);
            success = false;
        }
        if (!enabled) {
            disableExperimentalAmbientFeatures(context);
        }
        return success;
    }

    public static String getAmbientText(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String text = Settings.System.getString(resolver, AMBIENT_TEXT_STRING);
        return text != null ? text : DEFAULT_AMBIENT_TEXT;
    }

    public static boolean setAmbientText(Context context, String text) {
        boolean success = Settings.System.putString(context.getContentResolver(),
                AMBIENT_TEXT_STRING, text != null ? text : DEFAULT_AMBIENT_TEXT);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static int getAmbientTextAlignment(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                AMBIENT_TEXT_ALIGNMENT, DEFAULT_TEXT_ALIGNMENT);
    }

    public static boolean setAmbientTextAlignment(Context context, int alignment) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                AMBIENT_TEXT_ALIGNMENT, alignment);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static int getAmbientTextTypeColor(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                AMBIENT_TEXT_TYPE_COLOR, DEFAULT_TEXT_TYPE_COLOR);
    }

    public static boolean setAmbientTextTypeColor(Context context, int typeColor) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                AMBIENT_TEXT_TYPE_COLOR, typeColor);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static int getAmbientTextColor(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                AMBIENT_TEXT_COLOR, DEFAULT_TEXT_COLOR);
    }

    public static boolean setAmbientTextColor(Context context, int color) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                AMBIENT_TEXT_COLOR, color);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static String getAmbientCustomImage(Context context) {
        String image = Settings.System.getString(context.getContentResolver(),
                AMBIENT_CUSTOM_IMAGE);
        return image != null ? image : DEFAULT_CUSTOM_IMAGE;
    }

    public static boolean setAmbientCustomImage(Context context, String imageUri) {
        boolean success = Settings.System.putString(context.getContentResolver(),
                AMBIENT_CUSTOM_IMAGE, imageUri != null ? imageUri : DEFAULT_CUSTOM_IMAGE);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static boolean disableAll(Context context) {
        boolean success = true;
        success &= setAmbientTextEnabled(context, false);
        success &= setAmbientImageEnabled(context, false);
        success &= setAmbientTextAnimationEnabled(context, false);
        return success;
    }

    public static boolean isAmbientCustomizationEnabled(Context context) {
        return isAmbientTextEnabled(context) || isAmbientImageEnabled(context);
    }

    public static boolean isAmbientTextEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT, 0) == 1;
    }

    public static boolean setAmbientTextEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static boolean isAmbientTextAnimationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT_ANIMATION, 0) == 1;
    }

    public static boolean setAmbientTextAnimationEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT_ANIMATION, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static int getAmbientTextSize(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT_SIZE, DEFAULT_TEXT_SIZE);
    }

    public static boolean setAmbientTextSize(Context context, int size) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                SETTING_AMBIENT_TEXT_SIZE, size);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static boolean isAmbientImageEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                SETTING_AMBIENT_IMAGE, 0) == 1;
    }

    public static boolean setAmbientImageEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                SETTING_AMBIENT_IMAGE, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static String getAmbientImageFile(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String filePath = Settings.System.getStringForUser(resolver, AMBIENT_IMAGE_FILE,
                UserHandle.USER_CURRENT);
        return filePath != null ? filePath : "";
    }

    public static boolean setAmbientImageFile(Context context, String filePath) {
        boolean success = Settings.System.putStringForUser(context.getContentResolver(),
                AMBIENT_IMAGE_FILE, filePath != null ? filePath : "", UserHandle.USER_CURRENT);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    public static String getAlignmentSummary(Context context, int alignmentValue) {
        Resources res = context.getResources();
        String[] entries = res.getStringArray(R.array.ambient_text_alignment_entries);
        String[] values = res.getStringArray(R.array.ambient_text_alignment_values);
        for (int i = 0; i < values.length; i++) {
            if (String.valueOf(alignmentValue).equals(values[i])) {
                return entries[i];
            }
        }
        return entries.length > 0 ? entries[0] : "";
    }

    private static void notifyAmbientChange(Context context) {
        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_STRING), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_ALIGNMENT), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_TYPE_COLOR), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_COLOR), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_CUSTOM_IMAGE), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_IMAGE_FILE), null, true);
            resolver.notifyChange(Settings.System.getUriFor(SETTING_AMBIENT_TEXT), null, true);
            resolver.notifyChange(Settings.System.getUriFor(SETTING_AMBIENT_TEXT_ANIMATION), null,
                    true);
            resolver.notifyChange(Settings.System.getUriFor(SETTING_AMBIENT_TEXT_SIZE), null,
                    true);
            resolver.notifyChange(Settings.System.getUriFor(SETTING_AMBIENT_IMAGE), null, true);
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify ambient changes", e);
        }
    }
}
