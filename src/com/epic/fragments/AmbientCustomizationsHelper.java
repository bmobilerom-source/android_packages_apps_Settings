/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class AmbientCustomizationsHelper {

    private static final String TAG = "AmbientCustomizationsHelper";

    // Settings keys
    public static final String AMBIENT_TEXT_STRING = "ambient_text_string";
    public static final String AMBIENT_TEXT_ALIGNMENT = "ambient_text_alignment";
    public static final String AMBIENT_TEXT_TYPE_COLOR = "ambient_text_type_color";
    public static final String AMBIENT_TEXT_COLOR = "ambient_text_color";
    public static final String AMBIENT_CUSTOM_IMAGE = "ambient_custom_image";

    // Default values
    private static final String DEFAULT_AMBIENT_TEXT = "";
    private static final int DEFAULT_TEXT_ALIGNMENT = 0; // Start Top
    private static final int DEFAULT_TEXT_TYPE_COLOR = 0; // Accent color
    private static final int DEFAULT_TEXT_COLOR = 0xFF3980FF; // Default accent blue
    private static final String DEFAULT_CUSTOM_IMAGE = "";
    private static final int DEFAULT_TEXT_SIZE = 30;

    /**
     * Get ambient text string
     */
    public static String getAmbientText(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String text = Settings.System.getString(resolver, AMBIENT_TEXT_STRING);
        return text != null ? text : DEFAULT_AMBIENT_TEXT;
    }

    /**
     * Set ambient text string
     */
    public static boolean setAmbientText(Context context, String text) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putString(resolver, AMBIENT_TEXT_STRING,
                text != null ? text : DEFAULT_AMBIENT_TEXT);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get ambient text alignment
     */
    public static int getAmbientTextAlignment(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, AMBIENT_TEXT_ALIGNMENT, DEFAULT_TEXT_ALIGNMENT);
    }

    /**
     * Set ambient text alignment
     */
    public static boolean setAmbientTextAlignment(Context context, int alignment) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putInt(resolver, AMBIENT_TEXT_ALIGNMENT, alignment);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get ambient text color type
     */
    public static int getAmbientTextTypeColor(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, AMBIENT_TEXT_TYPE_COLOR, DEFAULT_TEXT_TYPE_COLOR);
    }

    /**
     * Set ambient text color type
     */
    public static boolean setAmbientTextTypeColor(Context context, int typeColor) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putInt(resolver, AMBIENT_TEXT_TYPE_COLOR, typeColor);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get ambient text custom color
     */
    public static int getAmbientTextColor(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, AMBIENT_TEXT_COLOR, DEFAULT_TEXT_COLOR);
    }

    /**
     * Set ambient text custom color
     */
    public static boolean setAmbientTextColor(Context context, int color) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putInt(resolver, AMBIENT_TEXT_COLOR, color);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get ambient custom image URI
     */
    public static String getAmbientCustomImage(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String image = Settings.System.getString(resolver, AMBIENT_CUSTOM_IMAGE);
        return image != null ? image : DEFAULT_CUSTOM_IMAGE;
    }

    /**
     * Set ambient custom image URI
     */
    public static boolean setAmbientCustomImage(Context context, String imageUri) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putString(resolver, AMBIENT_CUSTOM_IMAGE,
                imageUri != null ? imageUri : DEFAULT_CUSTOM_IMAGE);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Check if ambient customizations are enabled (has text or custom image)
     */
    public static boolean isAmbientCustomizationEnabled(Context context) {
        String text = getAmbientText(context);
        String imageUri = getAmbientCustomImage(context);
        return (text != null && !text.trim().isEmpty()) ||
               (imageUri != null && !imageUri.trim().isEmpty());
    }

    /**
     * Reset all ambient customizations to defaults
     */
    public static void resetAmbientCustomizations(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putString(resolver, AMBIENT_TEXT_STRING, DEFAULT_AMBIENT_TEXT);
        Settings.System.putInt(resolver, AMBIENT_TEXT_ALIGNMENT, DEFAULT_TEXT_ALIGNMENT);
        Settings.System.putInt(resolver, AMBIENT_TEXT_TYPE_COLOR, DEFAULT_TEXT_TYPE_COLOR);
        Settings.System.putInt(resolver, AMBIENT_TEXT_COLOR, DEFAULT_TEXT_COLOR);
        Settings.System.putString(resolver, AMBIENT_CUSTOM_IMAGE, DEFAULT_CUSTOM_IMAGE);
        Settings.System.putInt(resolver, Settings.System.AMBIENT_TEXT, 0);
        Settings.System.putInt(resolver, Settings.System.AMBIENT_TEXT_ANIMATION, 0);
        Settings.System.putInt(resolver, Settings.System.AMBIENT_TEXT_SIZE, DEFAULT_TEXT_SIZE);
        Settings.System.putInt(resolver, Settings.System.AMBIENT_IMAGE, 0);
        notifyAmbientChange(context);
    }

    /**
     * Check if ambient text is enabled.
     */
    public static boolean isAmbientTextEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT, 0) == 1;
    }

    /**
     * Enable or disable ambient text.
     */
    public static boolean setAmbientTextEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Check if ambient text animation is enabled.
     */
    public static boolean isAmbientTextAnimationEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT_ANIMATION, 0) == 1;
    }

    /**
     * Enable or disable ambient text animation.
     */
    public static boolean setAmbientTextAnimationEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT_ANIMATION, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get ambient text size value.
     */
    public static int getAmbientTextSize(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT_SIZE, DEFAULT_TEXT_SIZE);
    }

    /**
     * Set ambient text size value.
     */
    public static boolean setAmbientTextSize(Context context, int size) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                Settings.System.AMBIENT_TEXT_SIZE, size);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Check if ambient custom image is enabled.
     */
    public static boolean isAmbientImageEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_IMAGE, 0) == 1;
    }

    /**
     * Enable or disable ambient custom image.
     */
    public static boolean setAmbientImageEnabled(Context context, boolean enabled) {
        boolean success = Settings.System.putInt(context.getContentResolver(),
                Settings.System.AMBIENT_IMAGE, enabled ? 1 : 0);
        if (success) {
            notifyAmbientChange(context);
        }
        return success;
    }

    /**
     * Get alignment name for display
     */
    public static String getAlignmentName(Context context, int alignment) {
        Resources res = context.getResources();
        String[] entries = res.getStringArray(com.android.settings.R.array.ambient_text_alignment_entries);
        if (alignment >= 0 && alignment < entries.length) {
            return entries[alignment];
        }
        return entries[0]; // Default
    }

    /**
     * Get color type name for display
     */
    public static String getColorTypeName(Context context, int typeColor) {
        Resources res = context.getResources();
        String[] entries = res.getStringArray(com.android.settings.R.array.ambient_text_type_color_entries);
        if (typeColor >= 0 && typeColor < entries.length) {
            return entries[typeColor];
        }
        return entries[0]; // Default
    }

    /**
     * Notify SystemUI of ambient changes
     */
    private static void notifyAmbientChange(Context context) {
        ContentResolver resolver = context.getContentResolver();
        try {
            // Notify all ambient-related settings
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_STRING), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_ALIGNMENT), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_TYPE_COLOR), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_TEXT_COLOR), null, true);
            resolver.notifyChange(Settings.System.getUriFor(AMBIENT_CUSTOM_IMAGE), null, true);
            resolver.notifyChange(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT), null, true);
            resolver.notifyChange(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_ANIMATION), null, true);
            resolver.notifyChange(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_SIZE), null, true);
            resolver.notifyChange(Settings.System.getUriFor(Settings.System.AMBIENT_IMAGE), null, true);
            Log.d("AmbientCustomizationsHelper", "Notified SystemUI of ambient changes");
        } catch (Exception e) {
            Log.e("AmbientCustomizationsHelper", "Failed to notify ambient changes", e);
        }
    }

    /**
     * Check if ambient display is available on this device
     */
    public static boolean isAmbientDisplayAvailable(Context context) {
        try {
            return context.getPackageManager().hasSystemFeature("android.hardware.screen.ambient");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get ambient display timeout
     */
    public static int getAmbientDisplayTimeout(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                "ambient_display_timeout", 30000); // Default 30 seconds
    }

    /**
     * Set ambient display timeout
     */
    public static boolean setAmbientDisplayTimeout(Context context, int timeoutMs) {
        return Settings.System.putInt(context.getContentResolver(),
                "ambient_display_timeout", timeoutMs);
    }
}