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

package com.bmobile.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Settings fragment for Backup & Restore functionality.
 * Allows users to backup and restore their device settings.
 */
public class SettingsBackupRestore extends SettingsPreferenceFragment {

    private static final String TAG = "SettingsBackupRestore";
    private static final String KEY_BACKUP_SETTINGS = "backup_settings";
    private static final String KEY_RESTORE_SETTINGS = "restore_settings";
    private static final String KEY_BACKUP_LOCATION = "backup_location";
    private static final String BACKUP_FILE_NAME = "bmobile-settings-backup.json";

    private static final String[] SECURE_KEYS = new String[] {
            "install_app_whitelist_enabled",
            "block_app_dashboard_enabled",
            "block_usb_popup_enabled",
            "block_location_settings_enabled",
            "block_account_dashboard_enabled",
            "block_safety_center_enabled",
            "block_device_tweaks_enabled",
            "block_system_optimization_enabled",
            "block_blocation_enabled",
            "location_sharing_enabled",
            "location_background_access_enabled",
            "location_time_restrictions_enabled",
            "location_allowed_times",
            "location_precision",
            Settings.Secure.ALLOW_MOCK_LOCATION,
            Settings.Secure.AUTO_REBOOT_ENABLED,
            Settings.Secure.AUTO_REBOOT_DELAY
    };

    private static final String[] SYSTEM_KEYS = new String[] {
            "background_app_limits_enabled",
            "network_optimization_enabled",
            "battery_optimization_enabled",
            "storage_optimization_enabled",
            "thermal_throttling_enabled"
    };

    private static final String[] GLOBAL_KEYS = new String[] {
            Settings.Global.ADB_ENABLED,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            "restrict_background_data",
            Settings.Global.LOW_POWER_MODE,
            "storage_optimization_enabled",
            "thermal_throttling_enabled"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_backup_restore);
        setupPreferences();
        updateBackupLocation();
    }

    private void setupPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }

        Preference backupPref = screen.findPreference(KEY_BACKUP_SETTINGS);
        if (backupPref != null) {
            backupPref.setOnPreferenceClickListener(preference -> {
                performBackup();
                return true;
            });
        }

        Preference restorePref = screen.findPreference(KEY_RESTORE_SETTINGS);
        if (restorePref != null) {
            restorePref.setOnPreferenceClickListener(preference -> {
                performRestore();
                return true;
            });
        }
    }

    private void performBackup() {
        try {
            File backupFile = getBackupFile();
            JSONObject root = new JSONObject();
            root.put("secure", exportNamespace("secure", SECURE_KEYS));
            root.put("system", exportNamespace("system", SYSTEM_KEYS));
            root.put("global", exportNamespace("global", GLOBAL_KEYS));

            try (FileOutputStream outputStream = new FileOutputStream(backupFile)) {
                outputStream.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            }

            updateBackupLocation();
            Toast.makeText(getContext(), "Settings backup saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Backup failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void performRestore() {
        try {
            File backupFile = getBackupFile();
            if (!backupFile.exists()) {
                Toast.makeText(getContext(), "No backup file found", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] data;
            try (FileInputStream inputStream = new FileInputStream(backupFile)) {
                data = inputStream.readAllBytes();
            }
            JSONObject root = new JSONObject(new String(data, StandardCharsets.UTF_8));
            importNamespace("secure", root.optJSONArray("secure"));
            importNamespace("system", root.optJSONArray("system"));
            importNamespace("global", root.optJSONArray("global"));

            Toast.makeText(getContext(), "Settings restored", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Restore failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private JSONArray exportNamespace(String namespace, String[] keys) throws Exception {
        JSONArray output = new JSONArray();
        for (String key : keys) {
            String value = readValue(namespace, key);
            if (value == null) {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("key", key);
            item.put("value", value);
            output.put(item);
        }
        return output;
    }

    private void importNamespace(String namespace, JSONArray values) throws Exception {
        if (values == null) {
            return;
        }
        for (int i = 0; i < values.length(); i++) {
            JSONObject entry = values.optJSONObject(i);
            if (entry == null) {
                continue;
            }
            String key = entry.optString("key", null);
            String value = entry.optString("value", null);
            if (key == null || value == null) {
                continue;
            }
            writeValue(namespace, key, value);
        }
    }

    private String readValue(String namespace, String key) {
        ContentResolver resolver = requireContext().getContentResolver();
        switch (namespace) {
            case "secure":
                return Settings.Secure.getString(resolver, key);
            case "system":
                return Settings.System.getString(resolver, key);
            case "global":
                return Settings.Global.getString(resolver, key);
            default:
                return null;
        }
    }

    private void writeValue(String namespace, String key, String value) {
        ContentResolver resolver = requireContext().getContentResolver();
        switch (namespace) {
            case "secure":
                Settings.Secure.putString(resolver, key, value);
                break;
            case "system":
                Settings.System.putString(resolver, key, value);
                break;
            case "global":
                Settings.Global.putString(resolver, key, value);
                break;
            default:
                break;
        }
    }

    private File getBackupFile() {
        File downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        return new File(downloads, BACKUP_FILE_NAME);
    }

    private void updateBackupLocation() {
        Preference locationPref = getPreferenceScreen().findPreference(KEY_BACKUP_LOCATION);
        if (locationPref == null) {
            return;
        }
        File backupFile = getBackupFile();
        if (backupFile.exists()) {
            locationPref.setSummary(backupFile.getAbsolutePath());
        } else {
            locationPref.setSummary(getString(R.string.settings_backup_no_backup));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

