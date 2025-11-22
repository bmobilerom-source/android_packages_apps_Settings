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

    /**
     * Get ambient text string
     */
    public static String getAmbientText(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getString(resolver, AMBIENT_TEXT_STRING);
    }

    /**
     * Set ambient text string
     */
    public static boolean setAmbientText(Context context, String text) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putString(resolver, AMBIENT_TEXT_STRING, text != null ? text : DEFAULT_AMBIENT_TEXT);
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
        return Settings.System.putInt(resolver, AMBIENT_TEXT_ALIGNMENT, alignment);
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
        return Settings.System.putInt(resolver, AMBIENT_TEXT_TYPE_COLOR, typeColor);
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
        return Settings.System.putInt(resolver, AMBIENT_TEXT_COLOR, color);
    }

    /**
     * Get ambient custom image URI
     */
    public static String getAmbientCustomImage(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getString(resolver, AMBIENT_CUSTOM_IMAGE);
    }

    /**
     * Set ambient custom image URI
     */
    public static boolean setAmbientCustomImage(Context context, String imageUri) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.putString(resolver, AMBIENT_CUSTOM_IMAGE, imageUri != null ? imageUri : DEFAULT_CUSTOM_IMAGE);
    }

    /**
     * Check if ambient customizations are enabled (has text or custom image)
     */
    public static boolean isAmbientCustomizationEnabled(Context context) {
        String text = getAmbientText(context);
        String imageUri = getAmbientCustomImage(context);
        return (text != null && !text.trim().isEmpty()) || (imageUri != null && !imageUri.trim().isEmpty());
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
}

