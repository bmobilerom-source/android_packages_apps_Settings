/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.location;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.OnActivityResultListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

import java.util.List;

/**
 * Controller for managing mock location apps preference.
 * Allows users to select which apps can use mock locations.
 */
public class MockLocationAppPreferenceController extends BasePreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnActivityResultListener {

    private static final String MOCK_LOCATION_APP_KEY = "mock_location_app";
    private static final int[] MOCK_LOCATION_APP_OPS = new int[]{AppOpsManager.OP_MOCK_LOCATION};
    private static final int REQUEST_MOCK_LOCATION_APP = 1001;

    @Nullable
    private final MockLocationsSettings mFragment;
    private final AppOpsManager mAppsOpsManager;
    private final PackageManager mPackageManager;
    private Preference mPreference;

    public MockLocationAppPreferenceController(Context context, String key,
            @Nullable MockLocationsSettings fragment) {
        super(context, key);
        mFragment = fragment;
        mAppsOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(androidx.preference.PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        updateSummary();
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }

        // Launch app picker to select mock location app
        // Use DevelopmentAppPicker if available, otherwise use AppPicker
        try {
            final Bundle args = new Bundle();
            args.putString(com.android.settings.development.DevelopmentAppPicker.EXTRA_REQUESTING_PERMISSION,
                    Manifest.permission.ACCESS_MOCK_LOCATION);
            
            // Try to use DevelopmentAppPicker first
            try {
                Class<?> devAppPickerClass = Class.forName(
                    "com.android.settings.development.DevelopmentAppPicker");
                new SubSettingLauncher(mContext)
                        .setDestination(devAppPickerClass.getName())
                        .setArguments(args)
                        .setTitleRes(R.string.mock_location_apps_title)
                        .setSourceMetricsCategory(com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                        .setResultListener(mFragment, REQUEST_MOCK_LOCATION_APP)
                        .launch();
            } catch (ClassNotFoundException e) {
                // Fallback to AppPicker
                Class<?> appPickerClass = Class.forName(
                    "com.android.settings.applications.AppPicker");
                Intent intent = new Intent(mContext, appPickerClass);
                intent.putExtra("requesting_permission", Manifest.permission.ACCESS_MOCK_LOCATION);
                if (mFragment != null && mFragment.getActivity() != null) {
                    mFragment.getActivity().startActivityForResult(intent, REQUEST_MOCK_LOCATION_APP);
                } else {
                    mContext.startActivity(intent);
                }
            }
        } catch (Exception e) {
            // If app picker is not available, just show a message
            android.widget.Toast.makeText(mContext, 
                R.string.mock_location_app_picker_unavailable, 
                android.widget.Toast.LENGTH_SHORT).show();
        }
        
        return true;
    }

    @Override
    public void onResume() {
        updateSummary();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MOCK_LOCATION_APP && resultCode == Activity.RESULT_OK && data != null) {
            String mockLocationAppName = data.getAction();
            if (mockLocationAppName != null) {
                writeMockLocation(mockLocationAppName);
                updateSummary();
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        final String mockLocationApp = getCurrentMockLocationApp();
        if (!TextUtils.isEmpty(mockLocationApp)) {
            return mContext.getString(R.string.mock_location_app_set,
                    getAppLabel(mockLocationApp));
        } else {
            return mContext.getString(R.string.mock_location_app_not_set);
        }
    }

    private void updateSummary() {
        if (mPreference != null) {
            mPreference.setSummary(getSummary());
        }
    }

    private String getAppLabel(String mockLocationApp) {
        try {
            final ApplicationInfo ai = mPackageManager.getApplicationInfo(
                    mockLocationApp, PackageManager.MATCH_DISABLED_COMPONENTS);
            final CharSequence appLabel = mPackageManager.getApplicationLabel(ai);
            return appLabel != null ? appLabel.toString() : mockLocationApp;
        } catch (PackageManager.NameNotFoundException e) {
            return mockLocationApp;
        }
    }

    private String getCurrentMockLocationApp() {
        final List<AppOpsManager.PackageOps> packageOps = mAppsOpsManager.getPackagesForOps(
                MOCK_LOCATION_APP_OPS);
        if (packageOps != null) {
            for (AppOpsManager.PackageOps packageOp : packageOps) {
                if (packageOp.getOps().get(0).getMode() == AppOpsManager.MODE_ALLOWED) {
                    return packageOp.getPackageName();
                }
            }
        }
        return null;
    }

    private void writeMockLocation(String mockLocationAppName) {
        removeAllMockLocations();
        // Enable the app op of the new mock location app if such.
        if (!TextUtils.isEmpty(mockLocationAppName)) {
            try {
                final ApplicationInfo ai = mPackageManager.getApplicationInfo(
                        mockLocationAppName, PackageManager.MATCH_DISABLED_COMPONENTS);
                mAppsOpsManager.setMode(AppOpsManager.OP_MOCK_LOCATION, ai.uid,
                        mockLocationAppName, AppOpsManager.MODE_ALLOWED);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }
    }

    private void removeAllMockLocations() {
        // Disable the app op of the previous mock location app if such.
        final List<AppOpsManager.PackageOps> packageOps = mAppsOpsManager.getPackagesForOps(
                MOCK_LOCATION_APP_OPS);
        if (packageOps == null) {
            return;
        }
        // Should be one but in case we are in a bad state due to use of command line tools.
        for (AppOpsManager.PackageOps packageOp : packageOps) {
            if (packageOp.getOps().get(0).getMode() != AppOpsManager.MODE_ERRORED) {
                removeMockLocationForApp(packageOp.getPackageName());
            }
        }
    }

    private void removeMockLocationForApp(String appName) {
        try {
            final ApplicationInfo ai = mPackageManager.getApplicationInfo(
                    appName, PackageManager.MATCH_DISABLED_COMPONENTS);
            mAppsOpsManager.setMode(AppOpsManager.OP_MOCK_LOCATION, ai.uid,
                    appName, AppOpsManager.MODE_ERRORED);
        } catch (PackageManager.NameNotFoundException e) {
            // Ignore
        }
    }
}

