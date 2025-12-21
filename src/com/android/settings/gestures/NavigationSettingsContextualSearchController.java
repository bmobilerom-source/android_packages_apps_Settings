/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.gestures;

import static android.app.contextualsearch.ContextualSearchManager.FEATURE_CONTEXTUAL_SEARCH;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.settings.core.TogglePreferenceController;

import java.util.List;

/**
 * Configures behaviour of Contextual Search setting.
 * On Lineage ROM without native Circle to Search, launches drawing apps instead.
 */
public class NavigationSettingsContextualSearchController extends TogglePreferenceController {

    private static final String DRAWING_APP_PACKAGE_1 = "com.drawanywhere";
    private static final String DRAWING_APP_PACKAGE_2 = "com.screen.draw";
    private static final String DRAWING_APP_PACKAGE_3 = "com.annotation.screen";
    private static final String DRAWING_APP_PACKAGE_4 = "com.drawing.screen";

    public NavigationSettingsContextualSearchController(@NonNull Context context,
            @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        // First check if native Circle to Search is available
        if (mContext.getPackageManager().hasSystemFeature(FEATURE_CONTEXTUAL_SEARCH)) {
            boolean onByDefault = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_searchAllEntrypointsEnabledDefault);
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.SEARCH_ALL_ENTRYPOINTS_ENABLED, onByDefault ? 1 : 0)
                    == 1;
        }

        // On Lineage ROM without native feature, check if drawing app is configured
        return isDrawingAppAvailable();
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // First check if native Circle to Search is available
        if (mContext.getPackageManager().hasSystemFeature(FEATURE_CONTEXTUAL_SEARCH)) {
            return Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.SEARCH_ALL_ENTRYPOINTS_ENABLED, isChecked ? 1 : 0);
        }

        // On Lineage ROM, handle drawing app functionality
        if (isChecked) {
            return launchDrawingApp();
        } else {
            // When disabled, just return true (setting is "disabled")
            return true;
        }
    }

    @Override
    public int getAvailabilityStatus() {
        // Always available - either native Circle to Search or drawing app fallback
        return AVAILABLE;
    }

    /**
     * Check if any drawing app is available on the device
     */
    private boolean isDrawingAppAvailable() {
        PackageManager pm = mContext.getPackageManager();

        // Check for known drawing app packages
        String[] drawingPackages = {
            DRAWING_APP_PACKAGE_1,
            DRAWING_APP_PACKAGE_2,
            DRAWING_APP_PACKAGE_3,
            DRAWING_APP_PACKAGE_4
        };

        for (String packageName : drawingPackages) {
            try {
                pm.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, continue checking
            }
        }

        // Check for apps that can handle drawing intents
        Intent drawIntent = new Intent(Intent.ACTION_MAIN);
        drawIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        drawIntent.setType("image/*");

        List<ResolveInfo> drawingApps = pm.queryIntentActivities(drawIntent, 0);
        return drawingApps != null && !drawingApps.isEmpty();
    }

    /**
     * Launch the best available drawing app
     */
    private boolean launchDrawingApp() {
        PackageManager pm = mContext.getPackageManager();

        // Try known drawing app packages first
        String[] drawingPackages = {
            DRAWING_APP_PACKAGE_1,
            DRAWING_APP_PACKAGE_2,
            DRAWING_APP_PACKAGE_3,
            DRAWING_APP_PACKAGE_4
        };

        for (String packageName : drawingPackages) {
            if (launchAppByPackage(packageName)) {
                showToast("Launching drawing app: " + getAppName(packageName));
                return true;
            }
        }

        // Fallback: Try to find any drawing app
        if (launchGenericDrawingApp()) {
            showToast("Launching drawing app");
            return true;
        }

        // If no drawing app found, show message and open Play Store
        showToast("No drawing app found. Opening Play Store...");
        openPlayStoreForDrawingApps();
        return false;
    }

    /**
     * Launch app by package name
     */
    private boolean launchAppByPackage(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(launchIntent);
                return true;
            }
        } catch (Exception e) {
            // App not available or failed to launch
        }
        return false;
    }

    /**
     * Try to launch a generic drawing app
     */
    private boolean launchGenericDrawingApp() {
        try {
            Intent drawIntent = new Intent(Intent.ACTION_MAIN);
            drawIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            drawIntent.setType("image/*");
            drawIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (drawIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(drawIntent);
                return true;
            }
        } catch (Exception e) {
            // Failed to launch generic drawing app
        }
        return false;
    }

    /**
     * Open Play Store to search for drawing apps
     */
    private void openPlayStoreForDrawingApps() {
        try {
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse("market://search?q=drawing+app+screen"));
            playStoreIntent.setPackage("com.android.vending");
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(playStoreIntent);
        } catch (Exception e) {
            // Fallback: Open in browser
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("https://play.google.com/store/search?q=drawing+app+screen"));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(webIntent);
            } catch (Exception e2) {
                showToast("Unable to open Play Store");
            }
        }
    }

    /**
     * Get human-readable app name from package
     */
    private String getAppName(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    /**
     * Show toast message to user
     */
    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isSliceable() {
        return false;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }
}
