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

package com.android.settings.deviceinfo;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.widget.LayoutPreference;

import java.io.IOException;

/**
 * Controller for the device info header preference that displays ROM info, device name, and storage.
 * This is a safe, simplified version that won't crash when added to any page.
 */
public class DeviceInfoHeaderPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart {

    private static final String TAG = "DeviceInfoHeaderController";
    
    @VisibleForTesting
    static final String KEY_DEVICE_INFO_HEADER = "device_info_header";
    
    private LayoutPreference mPreference;
    private TextView mDeviceNameText;
    private TextView mStorageUsedText;
    private TextView mStorageInfoText;

    public DeviceInfoHeaderPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            initializeViews();
        }
    }

    private void initializeViews() {
        if (mPreference == null) return;
        
        try {
            mDeviceNameText = mPreference.findViewById(R.id.device_name_text);
            mStorageUsedText = mPreference.findViewById(R.id.storage_used_text);
            mStorageInfoText = mPreference.findViewById(R.id.storage_info_text);
            
            updateDeviceName();
            updateStorageInfo();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    @Override
    public void onStart() {
        updateDeviceName();
        updateStorageInfo();
    }

    private void updateDeviceName() {
        if (mDeviceNameText == null) return;
        
        try {
            String deviceName = Settings.Global.getString(mContext.getContentResolver(),
                    Settings.Global.DEVICE_NAME);
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = Build.MODEL;
            }
            mDeviceNameText.setText(deviceName);
        } catch (Exception e) {
            Log.e(TAG, "Error updating device name", e);
            mDeviceNameText.setText(Build.MODEL);
        }
    }

    private void updateStorageInfo() {
        if (mStorageUsedText == null || mStorageInfoText == null) return;
        
        try {
            StorageManager storageManager = mContext.getSystemService(StorageManager.class);
            if (storageManager == null) {
                setDefaultStorageInfo();
                return;
            }

            VolumeInfo primaryVolume = storageManager.getPrimaryStorageVolume();
            if (primaryVolume == null) {
                setDefaultStorageInfo();
                return;
            }

            StorageStatsManager statsManager = mContext.getSystemService(StorageStatsManager.class);
            if (statsManager == null) {
                setDefaultStorageInfo();
                return;
            }

            try {
                String fsUuid = primaryVolume.getFsUuid();
                if (fsUuid == null) {
                    // Fallback to StatFs for internal storage
                    StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
                    long totalBytes = stat.getTotalBytes();
                    long freeBytes = stat.getFreeBytes();
                    long usedBytes = totalBytes - freeBytes;
                    
                    String used = Formatter.formatFileSize(mContext, usedBytes, Formatter.FLAG_SHORTER);
                    String total = Formatter.formatFileSize(mContext, totalBytes, Formatter.FLAG_SHORTER);
                    
                    mStorageUsedText.setText(used);
                    mStorageInfoText.setText(mContext.getString(R.string.storage_card_info) + " " + total);
                } else {
                    long totalBytes = statsManager.getTotalBytes(fsUuid);
                    long freeBytes = statsManager.getFreeBytes(fsUuid);
                    long usedBytes = totalBytes - freeBytes;
                    
                    String used = Formatter.formatFileSize(mContext, usedBytes, Formatter.FLAG_SHORTER);
                    String total = Formatter.formatFileSize(mContext, totalBytes, Formatter.FLAG_SHORTER);
                    
                    mStorageUsedText.setText(used);
                    mStorageInfoText.setText(mContext.getString(R.string.storage_card_info) + " " + total);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting storage stats", e);
                setDefaultStorageInfo();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating storage info", e);
            setDefaultStorageInfo();
        }
    }

    private void setDefaultStorageInfo() {
        if (mStorageUsedText != null) {
            mStorageUsedText.setText("0 GB");
        }
        if (mStorageInfoText != null) {
            mStorageInfoText.setText(mContext.getString(R.string.storage_card_info));
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return null; // This preference doesn't have a summary
    }
}
