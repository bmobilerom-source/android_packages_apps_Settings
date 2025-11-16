/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.ContentResolver;
import android.os.UserHandle;
import android.provider.Settings;

/**
 * ROM-facing keys for custom dashboard settings (matches simplnc / Lineage-style trees).
 * Use these instead of array index when mapping style id → label.
 */
public final class DashboardSystemKeys {

    /** @see Settings.System#SETTINGS_DASHBOARD_STYLE when defined in framework */
    public static final String DASHBOARD_STYLE = "settings_dashboard_style";

    public static final String COMPACT_DASHBOARD = "settings_compact_dashboard_enabled";

    private DashboardSystemKeys() {}

    public static int getDashboardStyle(ContentResolver resolver, int defaultValue) {
        if (resolver == null) {
            return defaultValue;
        }
        try {
            return Settings.System.getIntForUser(
                    resolver, DASHBOARD_STYLE, defaultValue, UserHandle.USER_CURRENT);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean putDashboardStyle(ContentResolver resolver, int style) {
        if (resolver == null) {
            return false;
        }
        try {
            return Settings.System.putIntForUser(
                    resolver, DASHBOARD_STYLE, style, UserHandle.USER_CURRENT);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCompactDashboardEnabled(ContentResolver resolver) {
        if (resolver == null) {
            return false;
        }
        try {
            return Settings.System.getIntForUser(
                    resolver, COMPACT_DASHBOARD, 0, UserHandle.USER_CURRENT) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean putCompactDashboardEnabled(ContentResolver resolver, boolean enabled) {
        if (resolver == null) {
            return false;
        }
        try {
            return Settings.System.putIntForUser(
                    resolver, COMPACT_DASHBOARD, enabled ? 1 : 0, UserHandle.USER_CURRENT);
        } catch (Exception e) {
            return false;
        }
    }
}
