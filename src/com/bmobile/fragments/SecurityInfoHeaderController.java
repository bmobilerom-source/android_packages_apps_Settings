/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bmobile.fragments;

import android.content.Context;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.widget.LayoutPreference;

/**
 * Controller for populating security information in securitygrid_header layout.
 * This controller can be used on any settings page that includes the header layout.
 */
public class SecurityInfoHeaderController extends AbstractPreferenceController {

    private static final String TAG = "SecurityInfoHeader";
    private static final String KEY_HEADER = "epic_toplevel_card_navigation_lock";
    
    private LayoutPreference mHeaderPreference;

    public SecurityInfoHeaderController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mHeaderPreference = screen.findPreference(KEY_HEADER);
        updateSecurityInfo();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_HEADER;
    }

    /**
     * Update all security information fields in the header.
     */
    private void updateSecurityInfo() {
        if (mHeaderPreference == null) {
            Log.w(TAG, "Header preference not found");
            return;
        }

        View view = mHeaderPreference.findViewById(R.id.securitygrid_header_container);
        if (view == null) {
            Log.w(TAG, "Header container view not found");
            return;
        }

        // Apply current wallpaper to the illustration card background without breaking layout
        try {
            ImageView wallpaperView = view.findViewById(R.id.securitygrid_header_wallpaper);
            if (wallpaperView != null) {
                WallpaperManager wm = WallpaperManager.getInstance(mContext);
                if (wm != null) {
                    wallpaperView.setImageDrawable(wm.getDrawable());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to apply wallpaper to header illustration", e);
        }

        // Android Version
        TextView androidVersionTitle = view.findViewById(R.id.securitygrid_header_device);
        TextView androidVersionDesc = view.findViewById(R.id.securitygrid_header_device_desc);
        if (androidVersionTitle != null && androidVersionDesc != null) {
            androidVersionTitle.setText(R.string.securitygrid_header_android_version);
            String version = SystemProperties.get("ro.build.version.release_or_codename",
                    Build.VERSION.RELEASE);
            androidVersionDesc.setText(version);
        }

        // Security Patch Level
        TextView patchLevelTitle = view.findViewById(R.id.securitygrid_header_patch);
        TextView patchLevelDesc = view.findViewById(R.id.securitygrid_header_patch_desc);
        if (patchLevelTitle != null && patchLevelDesc != null) {
            patchLevelTitle.setText(R.string.securitygrid_header_patch_level);
            // Prefer the real system property; if missing, use the known patch date for this build.
            String patchLevel = SystemProperties.get("ro.build.version.security_patch", null);
            if (patchLevel == null || patchLevel.isEmpty()) {
                // AOSP format is YYYY-MM-DD; this ROM is built on August 1, 2025.
                patchLevel = "2025-08-01";
            }
            patchLevelDesc.setText(patchLevel);
        }

        // Build Number
        TextView buildNumberTitle = view.findViewById(R.id.securitygrid_header_build);
        TextView buildNumberDesc = view.findViewById(R.id.securitygrid_header_build_desc);
        if (buildNumberTitle != null && buildNumberDesc != null) {
            buildNumberTitle.setText(R.string.securitygrid_header_build_number);
            String buildNumber = Build.DISPLAY;
            if (buildNumber == null || buildNumber.isEmpty()) {
                buildNumber = Build.ID;
            }
            buildNumberDesc.setText(buildNumber);
        }

        // API Level
        TextView apiLevelTitle = view.findViewById(R.id.securitygrid_header_api);
        TextView apiLevelDesc = view.findViewById(R.id.securitygrid_header_api_desc);
        if (apiLevelTitle != null && apiLevelDesc != null) {
            apiLevelTitle.setText(R.string.securitygrid_header_api_level);
            String apiLevel = String.valueOf(Build.VERSION.SDK_INT);
            apiLevelDesc.setText(apiLevel);
        }

        // Kernel Version
        TextView kernelTitle = view.findViewById(R.id.securitygrid_header_kernel);
        TextView kernelDesc = view.findViewById(R.id.securitygrid_header_kernel_desc);
        if (kernelTitle != null && kernelDesc != null) {
            kernelTitle.setText(R.string.securitygrid_header_kernel_version);
            CharSequence kernelVersion = DeviceInfoUtils.getFormattedKernelVersion(mContext);
            if (kernelVersion == null || kernelVersion.length() == 0) {
                kernelVersion = mContext.getString(R.string.securitygrid_header_kernel_version_summary);
            }
            kernelDesc.setText(kernelVersion);
        }
    }

    /**
     * Update security information directly from a View (for use in RecyclerView).
     */
    public void updateHeaderView(View view) {
        if (view == null) {
            Log.w(TAG, "Header view is null");
            return;
        }

        // Find the container view (root of securitygrid_header)
        View container = view.findViewById(R.id.securitygrid_header_container);
        if (container == null) {
            // If container not found, use the view itself
            container = view;
        }

        // Apply current wallpaper to the illustration card background
        try {
            ImageView wallpaperView = container.findViewById(R.id.securitygrid_header_wallpaper);
            if (wallpaperView != null) {
                WallpaperManager wm = WallpaperManager.getInstance(mContext);
                if (wm != null) {
                    wallpaperView.setImageDrawable(wm.getDrawable());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to apply wallpaper to header illustration", e);
        }

        // Android Version
        TextView androidVersionTitle = container.findViewById(R.id.securitygrid_header_device);
        TextView androidVersionDesc = container.findViewById(R.id.securitygrid_header_device_desc);
        if (androidVersionTitle != null && androidVersionDesc != null) {
            androidVersionTitle.setText(R.string.securitygrid_header_android_version);
            String version = SystemProperties.get("ro.build.version.release_or_codename",
                    Build.VERSION.RELEASE);
            androidVersionDesc.setText(version);
        }

        // Security Patch Level
        TextView patchLevelTitle = container.findViewById(R.id.securitygrid_header_patch);
        TextView patchLevelDesc = container.findViewById(R.id.securitygrid_header_patch_desc);
        if (patchLevelTitle != null && patchLevelDesc != null) {
            patchLevelTitle.setText(R.string.securitygrid_header_patch_level);
            String patchLevel = SystemProperties.get("ro.build.version.security_patch", null);
            if (patchLevel == null || patchLevel.isEmpty()) {
                patchLevel = "2025-08-01";
            }
            patchLevelDesc.setText(patchLevel);
        }

        // Build Number
        TextView buildNumberTitle = container.findViewById(R.id.securitygrid_header_build);
        TextView buildNumberDesc = container.findViewById(R.id.securitygrid_header_build_desc);
        if (buildNumberTitle != null && buildNumberDesc != null) {
            buildNumberTitle.setText(R.string.securitygrid_header_build_number);
            String buildNumber = Build.DISPLAY;
            if (buildNumber == null || buildNumber.isEmpty()) {
                buildNumber = Build.ID;
            }
            buildNumberDesc.setText(buildNumber);
        }

        // API Level
        TextView apiLevelTitle = container.findViewById(R.id.securitygrid_header_api);
        TextView apiLevelDesc = container.findViewById(R.id.securitygrid_header_api_desc);
        if (apiLevelTitle != null && apiLevelDesc != null) {
            apiLevelTitle.setText(R.string.securitygrid_header_api_level);
            String apiLevel = String.valueOf(Build.VERSION.SDK_INT);
            apiLevelDesc.setText(apiLevel);
        }

        // Kernel Version
        TextView kernelTitle = container.findViewById(R.id.securitygrid_header_kernel);
        TextView kernelDesc = container.findViewById(R.id.securitygrid_header_kernel_desc);
        if (kernelTitle != null && kernelDesc != null) {
            kernelTitle.setText(R.string.securitygrid_header_kernel_version);
            CharSequence kernelVersion = DeviceInfoUtils.getFormattedKernelVersion(mContext);
            if (kernelVersion == null || kernelVersion.length() == 0) {
                kernelVersion = mContext.getString(R.string.securitygrid_header_kernel_version_summary);
            }
            kernelDesc.setText(kernelVersion);
        }
    }
}

