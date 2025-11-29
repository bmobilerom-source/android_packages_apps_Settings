/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.android.settings.awaken.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

    private static final int MAX_HEADER_IMAGES = 200;

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
     * Also sets the provider to "static" to enable image display.
     */
    public boolean setHeaderImageValue(String value) {
        boolean success = Settings.System.putStringForUser(mResolver, KEY_HEADER_IMAGE, value,
                UserHandle.USER_CURRENT);
        if (success) {
            // Notify SystemUI of the image change
            try {
                mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_IMAGE), null, true, UserHandle.USER_ALL);
                Log.d(TAG, "Notified SystemUI of QS header image change: " + value);
            } catch (Exception e) {
                Log.e(TAG, "Failed to notify QS header image change", e);
            }
            // Also set provider to "static" to enable image display
            success &= setHeaderProvider(PROVIDER_STATIC);
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

        // Scan all possible headers from both packages
        String[] packages = {"com.android.systemui", "com.android.systemui.res"};

        for (String pkg : packages) {
            try {
                PackageManager pm = mContext.getPackageManager();
                Resources res = pm.getResourcesForApplication(pkg);

                // Use reflection to get all drawable resource fields
                Class<?> drawableClass = Class.forName(pkg + ".R$drawable");
                java.lang.reflect.Field[] fields = drawableClass.getFields();

                for (java.lang.reflect.Field field : fields) {
                    String resourceName = field.getName();
                    if (resourceName.startsWith("qs_header_image_")) {
                        try {
                            int resId = field.getInt(null);
                            if (resId != 0) {
                                String value = pkg + "/" + resourceName;
                                if (seen.add(value)) {
                                    // Extract number from resource name for better display
                                    String numberPart = resourceName.substring("qs_header_image_".length());
                                    String displayName = getDisplayNameForHeader(numberPart);

                                    entries.add(new HeaderEntry(displayName, value));
                                    Log.d(TAG, "Found QS header via reflection: " + value + " -> " + displayName);
                                }
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Could not access field " + resourceName + " in " + pkg, e);
                        }
                    }
                }

                Log.d(TAG, "Scanned package " + pkg + " via reflection, found " + entries.size() + " headers so far");

            } catch (Exception e) {
                Log.w(TAG, "Failed to scan package " + pkg + " for drawable resources via reflection", e);
            }
        }

        // Also try direct file scanning as fallback for formats that might not be in R.java
        for (String pkg : packages) {
            try {
                // Try to scan drawable directory for qs_header_image files
                // This is a fallback for cases where reflection doesn't find all formats
                PackageManager pm = mContext.getPackageManager();
                Resources res = pm.getResourcesForApplication(pkg);

                // Check for known header numbers
                for (int i = 0; i <= MAX_HEADER_IMAGES; i++) {
                    String resourceName = "qs_header_image_" + i;
                    int resId = res.getIdentifier(resourceName, "drawable", pkg);
                    if (resId != 0) {
                        String value = pkg + "/" + resourceName;
                        if (seen.add(value)) {
                            String displayName = getDisplayNameForHeader(String.valueOf(i));
                            entries.add(new HeaderEntry(displayName, value));
                            Log.d(TAG, "Found QS header via direct check: " + value + " -> " + displayName);
                        }
                    }
                }

                Log.d(TAG, "After direct scanning, total headers found: " + entries.size());

            } catch (Exception e) {
                Log.w(TAG, "Failed direct scanning for package " + pkg, e);
            }
        }

        // Sort entries numerically by header number
        entries.sort((a, b) -> {
            String aNum = extractHeaderNumberString(a.value);
            String bNum = extractHeaderNumberString(b.value);

            // Try numeric comparison first
            try {
                int aInt = Integer.parseInt(aNum);
                int bInt = Integer.parseInt(bNum);
                return Integer.compare(aInt, bInt);
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                return aNum.compareTo(bNum);
            }
        });

        // If still empty, add default fallback
        if (entries.isEmpty()) {
            entries.add(new HeaderEntry("Default Header", "com.android.systemui.res/qs_header_image_0"));
            entries.add(new HeaderEntry("Header 1", "com.android.systemui.res/qs_header_image_1"));
            Log.w(TAG, "No QS headers found via reflection, using fallback defaults");
        } else {
            Log.d(TAG, "Successfully found " + entries.size() + " QS headers via reflection");
        }

        return entries;
    }

    /**
     * Extract header number string from value (e.g., "qs_header_image_42" -> "42")
     */
    private String extractHeaderNumberString(String value) {
        if (value == null) return "0";
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0) {
            String resourceName = value.substring(lastSlash + 1);
            if (resourceName.startsWith("qs_header_image_")) {
                return resourceName.substring("qs_header_image_".length());
            }
        }
        return "0";
    }

    /**
     * Build a user-friendly display name for a header based on its numeric id.
     * This adds simple categorisation (Abstract / Nature / City / Space) while
     * keeping things deterministic and lightweight.
     */
    private String getDisplayNameForHeader(String numberPart) {
        int number = -1;
        try {
            number = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            // Non-numeric suffix, just label generically
            return "Header " + numberPart;
        }

        if (number == 0) {
            return "Default header";
        } else if (number >= 1 && number <= 25) {
            return "Abstract " + number;
        } else if (number >= 26 && number <= 50) {
            return "Nature " + (number - 25);
        } else if (number >= 51 && number <= 75) {
            return "City " + (number - 50);
        } else if (number >= 76 && number <= 100) {
            return "Space " + (number - 75);
        } else {
            return "Header " + number;
        }
    }

    private int extractHeaderNumber(String value) {
        if (value == null) return 0;
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < value.length() - 1) {
            String name = value.substring(lastSlash + 1);
            if (name.startsWith("qs_header_image_")) {
                try {
                    return Integer.parseInt(name.substring("qs_header_image_".length()));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return 0;
    }

    private void scanHeadersFromPackage(List<HeaderEntry> entries, Set<String> seen,
            String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            Resources res = pm.getResourcesForApplication(packageName);

            // Try both package names (com.android.systemui and com.android.systemui.res)
            String[] packageNames = {packageName, "com.android.systemui.res", "com.android.systemui"};
            
            for (String pkg : packageNames) {
                try {
                    Resources pkgRes = pm.getResourcesForApplication(pkg);
                    for (int i = 0; i <= MAX_HEADER_IMAGES; i++) {
                        String resourceName = "qs_header_image_" + i;
                        int resId = pkgRes.getIdentifier(resourceName, "drawable", pkg);
                        if (resId != 0) {
                            String value = pkg + "/" + resourceName;
                            if (seen.add(value)) {
                                String displayName = i == 0 ? "Default Header" : "Header " + i;
                                entries.add(new HeaderEntry(displayName, value));
                                Log.d(TAG, "Found QS header: " + value + " (resId=" + resId + ")");
                            }
                        }
                    }
                } catch (Exception e) {
                    // Try next package name
                    continue;
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
        try {
            mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_PROVIDER), null, true, UserHandle.USER_ALL);
            // Also send broadcast
            Intent intent = new Intent("com.android.systemui.android.header.STATUS_BAR_HEADER_UPDATE");
            intent.setPackage("com.android.systemui");
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.d(TAG, "Notified SystemUI of QS header provider change");
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify QS header provider change", e);
        }
    }

    /**
     * Notify SystemUI of visibility changes
     */
    private void notifyVisibilityChange() {
        try {
            mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_VISIBILITY), null, true, UserHandle.USER_ALL);
            // Also send broadcast
            Intent intent = new Intent("com.android.systemui.android.header.STATUS_BAR_HEADER_UPDATE");
            intent.setPackage("com.android.systemui");
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.d(TAG, "Notified SystemUI of QS header visibility change");
        } catch (Exception e) {
            Log.e(TAG, "Failed to notify QS header visibility change", e);
        }
    }

    /**
     * Notify SystemUI of image changes
     */
    private void notifyImageChange() {
        try {
            // Notify via ContentObserver
            mResolver.notifyChange(Settings.System.getUriFor(KEY_HEADER_IMAGE), null, true, UserHandle.USER_ALL);
            
            // Also send broadcast as backup (for immediate update)
            Intent intent = new Intent("com.android.systemui.android.header.STATUS_BAR_HEADER_UPDATE");
            intent.setPackage("com.android.systemui");
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            
            Log.d(TAG, "Notified SystemUI of QS header image change (ContentObserver + Broadcast)");
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
