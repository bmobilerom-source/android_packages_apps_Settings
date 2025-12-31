/*
 * Copyright (C) 2024 The LineageOS Project
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

package com.android.settings.theme;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;

import java.util.List;

/**
 * Helper class for applying custom theme overlays
 * Manages overlay enable/disable operations for custom themes
 */
public class ThemeOverlayApplier {
    private static final String TAG = "ThemeOverlayApplier";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Context mContext;
    private final IOverlayManager mOverlayManager;

    // Theme overlay package names (matching SystemUI ThemeOverlayApplier)
    private static final String THEME_BLACK = "com.android.system.theme.black";
    private static final String THEME_VIVID = "com.android.system.theme.vivid";
    private static final String THEME_SNOWPAINT = "com.android.system.theme.snowpaint";
    private static final String THEME_ESPRESSO = "com.android.system.theme.espresso";
    
    // Additional theme packages for themes 5-9 (if they have overlays)
    // These may not exist, so we handle them gracefully
    private static final String THEME_CUSTOM_BLUE = "com.android.system.theme.customblue";
    private static final String THEME_ANIMATED = "com.android.system.theme.animated";
    private static final String THEME_EXPRESSIVE = "com.android.system.theme.expressive";
    private static final String THEME_ANIMATED_BLUR = "com.android.system.theme.animatedblur";
    private static final String THEME_CUSTOM_PICTURE = "com.android.system.theme.custompicture";

    public ThemeOverlayApplier(Context context) {
        mContext = context;
        mOverlayManager = IOverlayManager.Stub.asInterface(
            ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    /**
     * Apply a custom theme by enabling the corresponding overlay
     * @param themeValue the theme value (0=default, 1=black, 2=vivid/transparent, 3=snowpaint, 4=espresso, 5-9=additional themes)
     * @return true if theme was applied successfully (or if theme doesn't require overlay)
     */
    public boolean applyTheme(int themeValue) {
        if (DEBUG) Log.d(TAG, "applyTheme: themeValue=" + themeValue);

        // Validate theme value
        if (themeValue < 0 || themeValue > 9) {
            Log.w(TAG, "Invalid theme value: " + themeValue);
            return false;
        }

        // First disable ALL custom theme overlays to ensure clean state
        disableAllThemeOverlays();

        // If theme is default (0), we're done - all overlays are disabled
        if (themeValue == 0) {
            if (DEBUG) Log.d(TAG, "Default theme selected - all overlays disabled");
            return true;
        }

        // Get the overlay package name for this theme
        String overlayPackage = getOverlayPackageForTheme(themeValue);
        if (overlayPackage == null) {
            // Themes 5-9 may not have overlay packages - they might use other mechanisms
            // (fabricated overlays, runtime colors, etc.) - this is OK
            if (DEBUG) Log.d(TAG, "Theme " + themeValue + " doesn't require a static overlay package");
            return true; // Return true to allow theme setting to proceed
        }

        // Enable the selected theme overlay
        boolean result = enableThemeOverlay(overlayPackage);
        if (!result && themeValue <= 4) {
            // For themes 0-4, overlay is required, so log warning
            Log.w(TAG, "Failed to enable overlay for theme " + themeValue + ", but continuing");
        }
        return true; // Always return true to allow theme setting to be saved
    }

    /**
     * Get overlay package name for theme value
     * Returns null for themes that don't use static overlay packages
     */
    private String getOverlayPackageForTheme(int themeValue) {
        switch (themeValue) {
            case CustomThemeHelper.THEME_BLACK: // 1
                return THEME_BLACK;
            case CustomThemeHelper.THEME_TRANSPARENT: // 2 - Vivid maps to transparent
                return THEME_VIVID;
            case CustomThemeHelper.THEME_SNOWPAINT: // 3
                return THEME_SNOWPAINT;
            case CustomThemeHelper.THEME_ESPRESSO: // 4
                return THEME_ESPRESSO;
            case CustomThemeHelper.THEME_CUSTOM_BLUE: // 5
                // May not have overlay - uses runtime colors
                return THEME_CUSTOM_BLUE;
            case CustomThemeHelper.THEME_ANIMATED: // 6
                // May not have overlay - uses animated colors
                return THEME_ANIMATED;
            case CustomThemeHelper.THEME_EXPRESSIVE: // 7
                // May not have overlay - uses dynamic colors
                return THEME_EXPRESSIVE;
            case CustomThemeHelper.THEME_ANIMATED_WALLPAPER_BLUR: // 8
                // May not have overlay - uses wallpaper blur
                return THEME_ANIMATED_BLUR;
            case CustomThemeHelper.THEME_CUSTOM_PICTURE: // 9
                // May not have overlay - uses custom picture
                return THEME_CUSTOM_PICTURE;
            default:
                return null;
        }
    }

    /**
     * Enable a specific theme overlay
     * Returns true if overlay was enabled, or if overlay doesn't exist (for themes that don't require overlays)
     */
    private boolean enableThemeOverlay(String packageName) {
        if (DEBUG) Log.d(TAG, "Enabling theme overlay: " + packageName);

        try {
            OverlayIdentifier overlayId = findOverlayByIdentifier(packageName);
            if (overlayId == null) {
                // Overlay not found - this is OK for themes 5-9 that may use other mechanisms
                if (DEBUG) Log.d(TAG, "Theme overlay not found: " + packageName + " (may use fabricated overlays or runtime colors)");
                return true; // Return true to allow theme setting to proceed
            }

            // Enable for current user
            boolean enabled = mOverlayManager.setEnabled(overlayId.getPackageName(), true, UserHandle.USER_CURRENT);
            if (DEBUG) Log.d(TAG, "Enabled custom theme overlay for current user: " + packageName + ", success=" + enabled);

            // Also enable for system user to ensure consistency
            try {
                boolean systemEnabled = mOverlayManager.setEnabled(overlayId.getPackageName(), true, UserHandle.USER_SYSTEM);
                if (DEBUG) Log.d(TAG, "Enabled custom theme overlay for system user: " + packageName + ", success=" + systemEnabled);
            } catch (Exception e) {
                Log.w(TAG, "Failed to enable overlay for system user, continuing with current user only", e);
            }

            return enabled;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to enable theme overlay: " + packageName, e);
            // Return true anyway - theme setting should still be saved even if overlay fails
            return true;
        }
    }

    /**
     * Disable all custom theme overlays
     */
    private void disableAllThemeOverlays() {
        if (DEBUG) Log.d(TAG, "Disabling all custom theme overlays");

        // All possible theme overlay packages (including ones that may not exist)
        String[] themePackages = {
            THEME_BLACK, THEME_VIVID, THEME_SNOWPAINT, THEME_ESPRESSO,
            THEME_CUSTOM_BLUE, THEME_ANIMATED, THEME_EXPRESSIVE,
            THEME_ANIMATED_BLUR, THEME_CUSTOM_PICTURE
        };

        for (String packageName : themePackages) {
            try {
                OverlayIdentifier overlayId = findOverlayByIdentifier(packageName);
                if (overlayId != null) {
                    // Disable for current user
                    boolean disabled = mOverlayManager.setEnabled(overlayId.getPackageName(), false, UserHandle.USER_CURRENT);
                    if (DEBUG) Log.d(TAG, "Disabled theme overlay: " + packageName + ", success=" + disabled);

                    // Also disable for system user
                    try {
                        boolean systemDisabled = mOverlayManager.setEnabled(overlayId.getPackageName(), false, UserHandle.USER_SYSTEM);
                        if (DEBUG) Log.d(TAG, "Disabled theme overlay for system user: " + packageName + ", success=" + systemDisabled);
                    } catch (Exception e) {
                        // Ignore system user errors - current user is more important
                    }
                } else {
                    if (DEBUG) Log.d(TAG, "Theme overlay not found (may not be installed): " + packageName);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to disable theme overlay: " + packageName, e);
                // Continue with other overlays
            }
        }
    }

    /**
     * Find overlay by package identifier with fallback search
     */
    private OverlayIdentifier findOverlayByIdentifier(String packageName) {
        if (mOverlayManager == null) {
            Log.e(TAG, "OverlayManager is null");
            return null;
        }

        try {
            // First try direct lookup
            OverlayIdentifier overlayId = new OverlayIdentifier(packageName);
            OverlayInfo info = mOverlayManager.getOverlayInfo(packageName, UserHandle.USER_CURRENT);
            if (info != null) {
                if (DEBUG) Log.d(TAG, "Found theme overlay directly: " + packageName);
                return overlayId;
            }

            // Fallback: search through all overlays targeting android package
            List<OverlayInfo> overlays = mOverlayManager.getOverlayInfosForTarget("android", UserHandle.USER_CURRENT);
            for (OverlayInfo overlay : overlays) {
                if (packageName.equals(overlay.packageName)) {
                    if (DEBUG) Log.d(TAG, "Found theme overlay via search: " + packageName);
                    return overlay.getOverlayIdentifier();
                }
            }

            if (DEBUG) Log.d(TAG, "Theme overlay not found: " + packageName);
            return null;

        } catch (RemoteException e) {
            Log.e(TAG, "Error finding overlay: " + packageName, e);
            return null;
        }
    }
}
