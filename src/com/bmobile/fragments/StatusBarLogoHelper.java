/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.bmobile.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class StatusBarLogoHelper {

    // Settings keys
    public static final String STATUS_BAR_LOGO = "status_bar_logo";
    public static final String STATUS_BAR_LOGO_POSITION = "status_bar_logo_position";
    public static final String STATUS_BAR_LOGO_STYLE = "status_bar_logo_style";

    // Default values
    private static final int DEFAULT_LOGO_ENABLED = 0;
    private static final int DEFAULT_LOGO_POSITION = 0;
    private static final int DEFAULT_LOGO_STYLE = 0;

    /**
     * Check if status bar logo is enabled
     */
    public static boolean isStatusBarLogoEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getIntForUser(resolver, STATUS_BAR_LOGO, DEFAULT_LOGO_ENABLED,
                UserHandle.USER_CURRENT) != 0;
    }

    /**
     * Set status bar logo enabled state
     */
    public static boolean setStatusBarLogoEnabled(Context context, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO, enabled ? 1 : 0, UserHandle.USER_CURRENT);
        if (success) {
            notifyStatusBarChange(context);
        }
        return success;
    }

    /**
     * Get status bar logo position
     */
    public static int getStatusBarLogoPosition(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getIntForUser(resolver, STATUS_BAR_LOGO_POSITION,
                DEFAULT_LOGO_POSITION, UserHandle.USER_CURRENT);
    }

    /**
     * Set status bar logo position
     */
    public static boolean setStatusBarLogoPosition(Context context, int position) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO_POSITION, position, UserHandle.USER_CURRENT);
        if (success) {
            notifyStatusBarChange(context);
        }
        return success;
    }

    /**
     * Get status bar logo style
     */
    public static int getStatusBarLogoStyle(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getIntForUser(resolver, STATUS_BAR_LOGO_STYLE, DEFAULT_LOGO_STYLE,
                UserHandle.USER_CURRENT);
    }

    /**
     * Set status bar logo style
     */
    public static boolean setStatusBarLogoStyle(Context context, int style) {
        ContentResolver resolver = context.getContentResolver();
        boolean success = Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO_STYLE, style, UserHandle.USER_CURRENT);
        if (success) {
            notifyStatusBarChange(context);
        }
        return success;
    }

    /**
     * Get position name for display
     */
    public static String getPositionName(Context context, int position) {
        String[] entries = context.getResources().getStringArray(com.android.settings.R.array.status_bar_logo_position_entries);
        if (position >= 0 && position < entries.length) {
            return entries[position];
        }
        return entries[0]; // Default
    }

    /**
     * Get style name for display
     */
    public static String getStyleName(Context context, int style) {
        String[] entries = context.getResources().getStringArray(com.android.settings.R.array.status_bar_logo_style_entries);
        if (style >= 0 && style < entries.length) {
            return entries[style];
        }
        return entries[0]; // Default
    }

    /**
     * Reset all status bar logo settings to defaults
     */
    public static void resetStatusBarLogoSettings(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO, DEFAULT_LOGO_ENABLED,
                UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO_POSITION, DEFAULT_LOGO_POSITION,
                UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver, STATUS_BAR_LOGO_STYLE, DEFAULT_LOGO_STYLE,
                UserHandle.USER_CURRENT);
        notifyStatusBarChange(context);
    }

    /**
     * Notify SystemUI of status bar changes
     */
    public static void notifyStatusBarChange(Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();
            resolver.notifyChange(Settings.System.getUriFor(STATUS_BAR_LOGO), null, true,
                    UserHandle.USER_ALL);
            resolver.notifyChange(Settings.System.getUriFor(STATUS_BAR_LOGO_POSITION), null, true,
                    UserHandle.USER_ALL);
            resolver.notifyChange(Settings.System.getUriFor(STATUS_BAR_LOGO_STYLE), null, true,
                    UserHandle.USER_ALL);
            Log.d("StatusBarLogoHelper", "Notified SystemUI of status bar logo changes");
        } catch (Exception e) {
            Log.e("StatusBarLogoHelper", "Failed to notify status bar changes", e);
        }
    }

    /**
     * Check if status bar logo feature is available
     */
    public static boolean isLogoFeatureAvailable(Context context) {
        // Always available on modern Android devices
        return true;
    }
}

