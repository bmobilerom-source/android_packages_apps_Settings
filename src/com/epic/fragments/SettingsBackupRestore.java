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

package com.epic.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Settings fragment for Backup & Restore functionality.
 * Allows users to backup and restore their device settings to/from JSON files.
 */
public class SettingsBackupRestore extends SettingsPreferenceFragment {

    private static final String TAG = "SettingsBackupRestore";
    private static final String KEY_BACKUP_SETTINGS = "backup_settings";
    private static final String KEY_RESTORE_SETTINGS = "restore_settings";
    private static final String KEY_BACKUP_LOCATION = "backup_location";
    private static final String PREF_BACKUP_PATH = "backup_file_path";
    private static final String BACKUP_DIR = "SettingsBackup";
    
    private static final int REQUEST_CODE_CREATE_BACKUP = 1001;
    private static final int REQUEST_CODE_RESTORE_BACKUP = 1002;
    
    private Preference mBackupLocationPref;
    private ActivityResultLauncher<Intent> mCreateBackupLauncher;
    private ActivityResultLauncher<Intent> mRestoreBackupLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_backup_restore);
        
        // Initialize activity result launchers
        mCreateBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performBackup(uri);
                    }
                }
            });
        
        mRestoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performRestore(uri);
                    }
                }
            });
        
        setupPreferences();
        updateBackupLocationSummary();
    }

    private void setupPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }

        Preference backupPref = screen.findPreference(KEY_BACKUP_SETTINGS);
        if (backupPref != null) {
            backupPref.setOnPreferenceClickListener(preference -> {
                createBackup();
                return true;
            });
        }

        Preference restorePref = screen.findPreference(KEY_RESTORE_SETTINGS);
        if (restorePref != null) {
            restorePref.setOnPreferenceClickListener(preference -> {
                restoreBackup();
                return true;
            });
        }
        
        mBackupLocationPref = screen.findPreference(KEY_BACKUP_LOCATION);
    }
    
    private void createBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String filename = "settings_backup_" + timestamp + ".json";
            
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, filename);
            mCreateBackupLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup", e);
            Toast.makeText(getContext(), R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void restoreBackup() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            mRestoreBackupLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error restoring backup", e);
            Toast.makeText(getContext(), R.string.settings_restore_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void performBackup(Uri uri) {
        new Thread(() -> {
            try {
                JSONObject backupData = new JSONObject();
                
                // Add metadata
                backupData.put("version", 1);
                backupData.put("timestamp", System.currentTimeMillis());
                backupData.put("device_model", android.os.Build.MODEL);
                backupData.put("android_version", android.os.Build.VERSION.RELEASE);
                
                // Backup Settings.System
                JSONObject systemSettings = backupSystemSettings();
                backupData.put("system_settings", systemSettings);
                
                // Backup Settings.Secure
                JSONObject secureSettings = backupSecureSettings();
                backupData.put("secure_settings", secureSettings);
                
                // Backup Settings.Global (selected ones)
                JSONObject globalSettings = backupGlobalSettings();
                backupData.put("global_settings", globalSettings);
                
                // Write to file
                ContentResolver resolver = getContext().getContentResolver();
                try (OutputStream os = resolver.openOutputStream(uri);
                     BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                    writer.write(backupData.toString(2)); // Pretty print with 2-space indent
                    writer.flush();
                }
                
                // Save backup path
                saveBackupPath(uri.toString());
                
                // Update UI on main thread
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.settings_backup_success, Toast.LENGTH_SHORT).show();
                    updateBackupLocationSummary();
                });
                
                Log.d(TAG, "Backup completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error performing backup", e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void performRestore(Uri uri) {
        new Thread(() -> {
            try {
                ContentResolver resolver = getContext().getContentResolver();
                StringBuilder jsonString = new StringBuilder();
                
                try (InputStream is = resolver.openInputStream(uri);
                     BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonString.append(line).append("\n");
                    }
                }
                
                JSONObject backupData = new JSONObject(jsonString.toString());
                
                // Restore Settings.System
                if (backupData.has("system_settings")) {
                    restoreSystemSettings(backupData.getJSONObject("system_settings"));
                }
                
                // Restore Settings.Secure
                if (backupData.has("secure_settings")) {
                    restoreSecureSettings(backupData.getJSONObject("secure_settings"));
                }
                
                // Restore Settings.Global
                if (backupData.has("global_settings")) {
                    restoreGlobalSettings(backupData.getJSONObject("global_settings"));
                }
                
                // Save backup path
                saveBackupPath(uri.toString());
                
                // Update UI on main thread
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.settings_restore_success, Toast.LENGTH_SHORT).show();
                    updateBackupLocationSummary();
                });
                
                Log.d(TAG, "Restore completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error performing restore", e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.settings_restore_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private JSONObject backupSystemSettings() throws JSONException {
        JSONObject settings = new JSONObject();
        ContentResolver resolver = getContext().getContentResolver();
        
        // Backup common system settings
        String[] keys = {
            "screen_brightness",
            "screen_brightness_mode",
            "font_scale",
            "accelerometer_rotation",
            "user_rotation",
            "sound_effects_enabled",
            "haptic_feedback_enabled",
            "notification_light_pulse",
            "vibrate_when_ringing",
            "ringtone",
            "notification_sound",
            "alarm_alert"
        };
        
        for (String key : keys) {
            try {
                String value = Settings.System.getString(resolver, key);
                if (!TextUtils.isEmpty(value)) {
                    settings.put(key, value);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to backup system setting: " + key, e);
            }
        }
        
        return settings;
    }
    
    private JSONObject backupSecureSettings() throws JSONException {
        JSONObject settings = new JSONObject();
        ContentResolver resolver = getContext().getContentResolver();
        
        // Backup common secure settings
        String[] keys = {
            "install_app_whitelist_enabled",
            "block_app_dashboard_enabled",
            "block_location_settings_enabled",
            "block_account_dashboard_enabled",
            "block_usb_popup_enabled",
            "block_safety_center_enabled"
        };
        
        for (String key : keys) {
            try {
                int value = Settings.Secure.getInt(resolver, key, -1);
                if (value != -1) {
                    settings.put(key, value);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to backup secure setting: " + key, e);
            }
        }
        
        return settings;
    }
    
    private JSONObject backupGlobalSettings() throws JSONException {
        JSONObject settings = new JSONObject();
        ContentResolver resolver = getContext().getContentResolver();
        
        // Backup selected global settings
        String[] keys = {
            "wifi_on",
            "bluetooth_on",
            "airplane_mode_on",
            "auto_time",
            "auto_time_zone"
        };
        
        for (String key : keys) {
            try {
                int value = Settings.Global.getInt(resolver, key, -1);
                if (value != -1) {
                    settings.put(key, value);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to backup global setting: " + key, e);
            }
        }
        
        return settings;
    }
    
    private void restoreSystemSettings(JSONObject settings) throws JSONException {
        ContentResolver resolver = getContext().getContentResolver();
        
        for (String key : new String[]{
            "screen_brightness", "screen_brightness_mode", "font_scale",
            "accelerometer_rotation", "user_rotation", "sound_effects_enabled",
            "haptic_feedback_enabled", "notification_light_pulse",
            "vibrate_when_ringing", "ringtone", "notification_sound", "alarm_alert"
        }) {
            if (settings.has(key)) {
                try {
                    String value = settings.getString(key);
                    Settings.System.putString(resolver, key, value);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to restore system setting: " + key, e);
                }
            }
        }
    }
    
    private void restoreSecureSettings(JSONObject settings) throws JSONException {
        ContentResolver resolver = getContext().getContentResolver();
        
        for (String key : new String[]{
            "install_app_whitelist_enabled", "block_app_dashboard_enabled",
            "block_location_settings_enabled", "block_account_dashboard_enabled",
            "block_usb_popup_enabled", "block_safety_center_enabled"
        }) {
            if (settings.has(key)) {
                try {
                    int value = settings.getInt(key);
                    Settings.Secure.putInt(resolver, key, value);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to restore secure setting: " + key, e);
                }
            }
        }
    }
    
    private void restoreGlobalSettings(JSONObject settings) throws JSONException {
        ContentResolver resolver = getContext().getContentResolver();
        
        for (String key : new String[]{
            "wifi_on", "bluetooth_on", "airplane_mode_on",
            "auto_time", "auto_time_zone"
        }) {
            if (settings.has(key)) {
                try {
                    int value = settings.getInt(key);
                    Settings.Global.putInt(resolver, key, value);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to restore global setting: " + key, e);
                }
            }
        }
    }
    
    private void saveBackupPath(String path) {
        SharedPreferences prefs = getContext().getSharedPreferences("settings_backup_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_BACKUP_PATH, path).apply();
    }
    
    private String getBackupPath() {
        SharedPreferences prefs = getContext().getSharedPreferences("settings_backup_prefs", Context.MODE_PRIVATE);
        return prefs.getString(PREF_BACKUP_PATH, null);
    }
    
    private void updateBackupLocationSummary() {
        if (mBackupLocationPref == null) {
            return;
        }
        
        String path = getBackupPath();
        if (TextUtils.isEmpty(path)) {
            mBackupLocationPref.setSummary(R.string.settings_backup_no_backup);
        } else {
            try {
                Uri uri = Uri.parse(path);
                String displayName = uri.getLastPathSegment();
                if (displayName != null && displayName.length() > 0) {
                    mBackupLocationPref.setSummary(displayName);
                } else {
                    mBackupLocationPref.setSummary(path);
                }
            } catch (Exception e) {
                mBackupLocationPref.setSummary(path);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}


