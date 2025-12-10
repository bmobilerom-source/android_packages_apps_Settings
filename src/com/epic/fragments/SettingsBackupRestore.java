/*
 * Copyright (C) 2025 LineageOS
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Fragment for backing up and restoring Settings preferences.
 * Allows users to save and restore their Settings configuration.
 */
public class SettingsBackupRestore extends SettingsPreferenceFragment
        implements OnPreferenceClickListener {

    private static final String TAG = "SettingsBackupRestore";
    private static final String KEY_BACKUP_SETTINGS = "backup_settings";
    private static final String KEY_RESTORE_SETTINGS = "restore_settings";
    private static final int REQUEST_CODE_BACKUP = 1001;
    private static final int REQUEST_CODE_RESTORE = 1002;

    private Preference mBackupPreference;
    private Preference mRestorePreference;

    // Settings keys to backup/restore
    private static final String[] BACKUP_SETTINGS_KEYS = {
            Settings.System.SETTINGS_DASHBOARD_STYLE,
            Settings.Secure.SYSTEM_CUSTOM_THEME,
            Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED,
            "settings_blocked_pages_list"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_backup_restore);

        mBackupPreference = findPreference(KEY_BACKUP_SETTINGS);
        if (mBackupPreference != null) {
            mBackupPreference.setOnPreferenceClickListener(this);
        }

        mRestorePreference = findPreference(KEY_RESTORE_SETTINGS);
        if (mRestorePreference != null) {
            mRestorePreference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mBackupPreference) {
            backupSettings();
            return true;
        } else if (preference == mRestorePreference) {
            restoreSettings();
            return true;
        }
        return false;
    }

    private void backupSettings() {
        try {
            ContentResolver resolver = getActivity().getContentResolver();
            JSONObject backupData = new JSONObject();

            // Backup System settings
            JSONObject systemSettings = new JSONObject();
            for (String key : BACKUP_SETTINGS_KEYS) {
                try {
                    if (key.equals(Settings.System.SETTINGS_DASHBOARD_STYLE) ||
                        key.equals(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                        int value = Settings.System.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        systemSettings.put(key, value);
                    } else if (key.equals(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                        int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        systemSettings.put(key, value);
                    } else if (key.equals("settings_blocked_pages_list")) {
                        String value = Settings.System.getStringForUser(resolver, key, UserHandle.USER_CURRENT);
                        if (value != null && !value.isEmpty()) {
                            systemSettings.put(key, value);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error backing up setting: " + key, e);
                }
            }
            backupData.put("system_settings", systemSettings);
            backupData.put("backup_version", 1);
            backupData.put("timestamp", System.currentTimeMillis());

            // Save to file
            File backupFile = new File(getActivity().getExternalFilesDir(null), "settings_backup.json");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
                writer.write(backupData.toString(2));
            }

            Toast.makeText(getActivity(),
                    getString(R.string.settings_backup_success, backupFile.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Settings backed up to: " + backupFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error backing up settings", e);
            Toast.makeText(getActivity(), R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreSettings() {
        try {
            File backupFile = new File(getActivity().getExternalFilesDir(null), "settings_backup.json");
            if (!backupFile.exists()) {
                Toast.makeText(getActivity(), R.string.settings_restore_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }

            JSONObject backupData = new JSONObject(jsonContent.toString());
            JSONObject systemSettings = backupData.getJSONObject("system_settings");

            ContentResolver resolver = getActivity().getContentResolver();
            int restored = 0;

            for (String key : BACKUP_SETTINGS_KEYS) {
                if (systemSettings.has(key)) {
                    try {
                        if (key.equals(Settings.System.SETTINGS_DASHBOARD_STYLE) ||
                            key.equals(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                            int value = systemSettings.getInt(key);
                            Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            restored++;
                        } else if (key.equals(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                            int value = systemSettings.getInt(key);
                            Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            restored++;
                        } else {
                            String value = systemSettings.getString(key);
                            Settings.System.putStringForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            restored++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error restoring setting: " + key, e);
                    }
                }
            }

            Toast.makeText(getActivity(),
                    getString(R.string.settings_restore_success, restored),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Restored " + restored + " settings");

            // Reload activity to apply changes
            if (getActivity() != null) {
                getActivity().recreate();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring settings", e);
            Toast.makeText(getActivity(), R.string.settings_restore_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DASHBOARD_SUMMARY;
    }
}

