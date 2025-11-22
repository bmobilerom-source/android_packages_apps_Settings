/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.android.settings.awaken.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class QsHeaderHelper {

    private static final String TAG = "QsHeaderHelper";

    private static final int MAX_HEADER_IMAGES = 300;

    private Context mContext;
    private ContentResolver mResolver;

    private static class HeaderEntry {
        final String name;
        final String value;

        HeaderEntry(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    // Provider constants
    public static final String PROVIDER_STATIC = "static";
    public static final String PROVIDER_FILE = "file";
    public static final String PROVIDER_DAYLIGHT = "daylight";

    // Settings keys
    private static final String KEY_HEADER_PROVIDER = Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER;
    private static final String KEY_HEADER_IMAGE = Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE;
    // Use the correct key for visibility/enablement (Master Switch)
    private static final String KEY_HEADER_VISIBILITY = Settings.System.STATUS_BAR_CUSTOM_HEADER;

    public QsHeaderHelper(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
    }

    /**
     * Get current header provider
     */
    public String getHeaderProvider() {
        String provider = Settings.System.getStringForUser(mResolver, KEY_HEADER_PROVIDER, UserHandle.USER_CURRENT);
        return (provider != null && !provider.isEmpty()) ? provider : PROVIDER_STATIC;
    }

    /**
     * Set header provider
     */
    public boolean setHeaderProvider(String provider) {
        boolean success = Settings.System.putStringForUser(mResolver, KEY_HEADER_PROVIDER, provider, UserHandle.USER_CURRENT);
        if (success) {
            notifyProviderChange();
        }
        return success;
    }

    /**
     * Get header visibility
     */
    public boolean getHeaderVisibility() {
        return Settings.System.getIntForUser(mResolver, KEY_HEADER_VISIBILITY, 1, UserHandle.USER_CURRENT) == 1;
    }

    /**
     * Set header visibility
     */
    public boolean setHeaderVisibility(boolean visible) {
        int value = visible ? 1 : 0;
        boolean success = Settings.System.putIntForUser(mResolver, KEY_HEADER_VISIBILITY, value, UserHandle.USER_CURRENT);
        if (success) {
            notifyVisibilityChange();
        }
        return success;
    }

    /**
     * Get current header image value.
     */
    public String getCurrentHeaderValue() {
        return Settings.System.getStringForUser(mResolver, KEY_HEADER_IMAGE, UserHandle.USER_CURRENT);
    }

    /**
     * Set header image using the given package/resource value.
     */
    public boolean setHeaderImageValue(String value) {
        boolean success = Settings.System.putStringForUser(mResolver, KEY_HEADER_IMAGE, value,
                UserHandle.USER_CURRENT);
        if (success) {
            notifyImageChange();
        }
        return success;
    }

    /**
     * Get available image names.
     */
    public List<String> getAvailableImageNames() {
        List<HeaderEntry> entries = getHeaderEntries();
        List<String> names = new ArrayList<>(entries.size());
        for (HeaderEntry entry : entries) {
            names.add(entry.name);
        }
        return names;
    }

    /**
     * Get available image values (package/resource pairs).
     */
    public List<String> getAvailableImageValues() {
        List<HeaderEntry> entries = getHeaderEntries();
        List<String> values = new ArrayList<>(entries.size());
        for (HeaderEntry entry : entries) {
            values.add(entry.value);
        }
        return values;
    }

    private List<HeaderEntry> getHeaderEntries() {
        List<HeaderEntry> entries = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        scanHeadersFromPackage(entries, seen, "com.android.systemui.res");
        scanHeadersFromPackage(entries, seen, "com.android.systemui");
        if (entries.isEmpty()) {
            entries.add(new HeaderEntry("Header 1", "com.android.systemui/qs_header_image_1"));
        }
        return entries;
    }

    private void scanHeadersFromPackage(List<HeaderEntry> entries, Set<String> seen,
            String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            Resources res = pm.getResourcesForApplication(packageName);

            for (int i = 1; i <= MAX_HEADER_IMAGES; i++) {
                String resourceName = "qs_header_image_" + i;
                int resId = res.getIdentifier(resourceName, "drawable", packageName);
                if (resId != 0) {
                    String value = packageName + "/" + resourceName;
                    if (seen.add(value)) {
                        entries.add(new HeaderEntry("Header " + i, value));
                    }
                }
            }

            Log.d(TAG, "Loaded headers from " + packageName + ": current total=" + entries.size());
        } catch (Exception e) {
            Log.w(TAG, "Unable to load headers from " + packageName, e);
        }
    }

    /**
     * Notify SystemUI of provider changes
     */
    private void notifyProviderChange() {
        mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_PROVIDER), null, true);
    }

    /**
     * Notify SystemUI of visibility changes
     */
    private void notifyVisibilityChange() {
        mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_VISIBILITY), null, true);
    }

    /**
     * Notify SystemUI of image changes
     */
    private void notifyImageChange() {
        try {
            mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_IMAGE), null, true);
            Log.d(TAG, "Notified SystemUI of QS header image change");
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify QS header image change", e);
        }
    }

    /**
     * Check if QS header feature is available
     */
    public boolean isQsHeaderAvailable() {
        try {
            return mContext.getPackageManager().hasSystemFeature("android.software.live_wallpaper");
        } catch (Exception e) {
            return true; // Assume available if we can't check
        }
    }

    /**
     * Get QS header brightness level
     */
    public int getHeaderBrightness() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
    }

    /**
     * Set QS header brightness level
     */
    public boolean setHeaderBrightness(int brightness) {
        boolean result = Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, brightness);
        if (result) {
            notifyBrightnessChange();
        }
        return result;
    }

    /**
     * Notify SystemUI of brightness changes
     */
    private void notifyBrightnessChange() {
        try {
            mResolver.notifyChange(Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW), null, true);
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify brightness change", e);
        }
    }
}
