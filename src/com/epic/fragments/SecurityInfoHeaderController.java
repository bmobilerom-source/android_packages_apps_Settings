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

package com.epic.fragments;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.widget.LayoutPreference;

/**
 * Controller for populating security information in the xd_about_phone_header layout.
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

        View view = mHeaderPreference.findViewById(R.id.container);
        if (view == null) {
            Log.w(TAG, "Header container view not found");
            return;
        }

        // Android Version
        TextView androidVersionTitle = view.findViewById(R.id.xd_about_phone_header_device);
        TextView androidVersionDesc = view.findViewById(R.id.xd_about_phone_header_device_desc);
        if (androidVersionTitle != null && androidVersionDesc != null) {
            androidVersionTitle.setText(R.string.xd_security_android_version);
            String version = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY;
            if (version == null || version.isEmpty()) {
                version = Build.VERSION.RELEASE;
            }
            androidVersionDesc.setText(version);
        }

        // Security Patch Level
        TextView patchLevelTitle = view.findViewById(R.id.xd_about_phone_header_chipset);
        TextView patchLevelDesc = view.findViewById(R.id.xd_about_phone_header_chipset_desc);
        if (patchLevelTitle != null && patchLevelDesc != null) {
            patchLevelTitle.setText(R.string.xd_security_patch_level);
            String patchLevel = Build.VERSION.SECURITY_PATCH;
            if (patchLevel == null || patchLevel.isEmpty() || "".equals(patchLevel)) {
                patchLevel = mContext.getString(R.string.xd_security_patch_level_summary);
            }
            patchLevelDesc.setText(patchLevel);
        }

        // Build Number
        TextView buildNumberTitle = view.findViewById(R.id.xd_about_phone_header_gpu);
        TextView buildNumberDesc = view.findViewById(R.id.xd_about_phone_header_gpu_desc);
        if (buildNumberTitle != null && buildNumberDesc != null) {
            buildNumberTitle.setText(R.string.xd_security_build_number);
            String buildNumber = Build.DISPLAY;
            if (buildNumber == null || buildNumber.isEmpty()) {
                buildNumber = Build.ID;
            }
            buildNumberDesc.setText(buildNumber);
        }

        // API Level
        TextView apiLevelTitle = view.findViewById(R.id.xd_about_phone_header_camera);
        TextView apiLevelDesc = view.findViewById(R.id.xd_about_phone_header_camera_desc);
        if (apiLevelTitle != null && apiLevelDesc != null) {
            apiLevelTitle.setText(R.string.xd_security_api_level);
            String apiLevel = String.valueOf(Build.VERSION.SDK_INT);
            apiLevelDesc.setText(apiLevel);
        }

        // Kernel Version
        TextView kernelTitle = view.findViewById(R.id.xd_about_phone_header_screen);
        TextView kernelDesc = view.findViewById(R.id.xd_about_phone_header_screen_desc);
        if (kernelTitle != null && kernelDesc != null) {
            kernelTitle.setText(R.string.xd_security_kernel_version);
            CharSequence kernelVersion = DeviceInfoUtils.getFormattedKernelVersion(mContext);
            if (kernelVersion == null || kernelVersion.length() == 0) {
                kernelVersion = mContext.getString(R.string.xd_security_kernel_version_summary);
            }
            kernelDesc.setText(kernelVersion);
        }
    }
}

