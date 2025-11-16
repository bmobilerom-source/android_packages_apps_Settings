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

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Improved fragment for backing up and restoring Settings preferences.
 * Allows users to select which settings categories to backup/restore.
 */
public class SettingsBackupRestoreImproved extends SettingsPreferenceFragment
        implements OnPreferenceClickListener, OnPreferenceChangeListener {

    private static final String TAG = "SettingsBackupRestore";
    private static final String KEY_BACKUP_SETTINGS = "backup_settings";
    private static final String KEY_RESTORE_SETTINGS = "restore_settings";
    private static final String KEY_BACKUP_SELECTION = "backup_selection";

    private Preference mBackupPreference;
    private Preference mRestorePreference;
    private MultiSelectListPreference mBackupSelectionPreference;

    // Settings categories with descriptions
    private static final String CATEGORY_DASHBOARD = "dashboard_style";
    private static final String CATEGORY_THEME = "custom_theme";
    private static final String CATEGORY_WALLPAPER = "wallpaper_background";
    private static final String CATEGORY_BLOCKED_PAGES = "blocked_pages";
    private static final String CATEGORY_SECURITY = "security_features";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_backup_restore_improved);

        mBackupPreference = findPreference(KEY_BACKUP_SETTINGS);
        if (mBackupPreference != null) {
            mBackupPreference.setOnPreferenceClickListener(this);
        }

        mRestorePreference = findPreference(KEY_RESTORE_SETTINGS);
        if (mRestorePreference != null) {
            mRestorePreference.setOnPreferenceClickListener(this);
        }

        mBackupSelectionPreference = (MultiSelectListPreference) findPreference(KEY_BACKUP_SELECTION);
        if (mBackupSelectionPreference != null) {
            // Default to all categories selected
            Set<String> defaultSelection = new HashSet<>();
            defaultSelection.add(CATEGORY_DASHBOARD);
            defaultSelection.add(CATEGORY_THEME);
            defaultSelection.add(CATEGORY_WALLPAPER);
            defaultSelection.add(CATEGORY_BLOCKED_PAGES);
            defaultSelection.add(CATEGORY_SECURITY);
            mBackupSelectionPreference.setValues(defaultSelection);
            mBackupSelectionPreference.setOnPreferenceChangeListener(this);
            updateBackupSelectionSummary();
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBackupSelectionPreference) {
            @SuppressWarnings("unchecked")
            Set<String> selected = (Set<String>) newValue;
            updateBackupSelectionSummary();
            return true;
        }
        return false;
    }

    private void updateBackupSelectionSummary() {
        if (mBackupSelectionPreference == null) return;
        Set<String> selected = mBackupSelectionPreference.getValues();
        int count = selected != null ? selected.size() : 0;
        mBackupSelectionPreference.setSummary(
                getString(R.string.settings_backup_selection_summary, count));
    }

    private void backupSettings() {
        try {
            ContentResolver resolver = getActivity().getContentResolver();
            Set<String> selectedCategories = mBackupSelectionPreference.getValues();
            
            if (selectedCategories == null || selectedCategories.isEmpty()) {
                Toast.makeText(getActivity(), 
                        R.string.settings_backup_no_selection, 
                        Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject backupData = new JSONObject();
            JSONObject systemSettings = new JSONObject();
            int backedUp = 0;

            // Backup selected categories
            if (selectedCategories.contains(CATEGORY_DASHBOARD)) {
                int value = Settings.System.getIntForUser(resolver, 
                        Settings.System.SETTINGS_DASHBOARD_STYLE, 0, UserHandle.USER_CURRENT);
                systemSettings.put(Settings.System.SETTINGS_DASHBOARD_STYLE, value);
                backedUp++;
            }

            if (selectedCategories.contains(CATEGORY_THEME)) {
                int value = Settings.Secure.getIntForUser(resolver, 
                        Settings.Secure.SYSTEM_CUSTOM_THEME, 0, UserHandle.USER_CURRENT);
                systemSettings.put(Settings.Secure.SYSTEM_CUSTOM_THEME, value);
                backedUp++;
            }

            if (selectedCategories.contains(CATEGORY_WALLPAPER)) {
                int value = Settings.System.getIntForUser(resolver, 
                        Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, 0, UserHandle.USER_CURRENT);
                systemSettings.put(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, value);
                backedUp++;
            }

            if (selectedCategories.contains(CATEGORY_BLOCKED_PAGES)) {
                String value = Settings.System.getStringForUser(resolver, 
                        "settings_blocked_pages_list", UserHandle.USER_CURRENT);
                if (value != null && !value.isEmpty()) {
                    systemSettings.put("settings_blocked_pages_list", value);
                    backedUp++;
                }
            }

            if (selectedCategories.contains(CATEGORY_SECURITY)) {
                // Backup security feature settings
                JSONObject securitySettings = new JSONObject();
                String[] securityKeys = {
                    "block_analytics_enabled",
                    "block_diagnostic_data_enabled",
                    "block_personalization_enabled",
                    "block_android_intelligence_enabled",
                    "block_ad_services_enabled",
                    "block_cloud_backup_enabled",
                    "block_location_services_enabled",
                    "block_network_scanning_enabled",
                    "block_webview_updates_enabled",
                    "block_system_updates_enabled",
                    "block_app_installation_enabled"
                };
                for (String key : securityKeys) {
                    int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                    securitySettings.put(key, value);
                }
                backupData.put("security_settings", securitySettings);
                backedUp += securityKeys.length;
            }

            backupData.put("system_settings", systemSettings);
            backupData.put("backup_version", 2);
            backupData.put("timestamp", System.currentTimeMillis());
            backupData.put("selected_categories", new org.json.JSONArray(selectedCategories));

            // Save to file
            File backupFile = new File(getActivity().getExternalFilesDir(null), "settings_backup.json");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
                writer.write(backupData.toString(2));
            }

            Toast.makeText(getActivity(),
                    getString(R.string.settings_backup_success_detailed, backedUp, backupFile.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Settings backed up: " + backedUp + " items to " + backupFile.getAbsolutePath());
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

            // Restore system settings
            if (systemSettings.has(Settings.System.SETTINGS_DASHBOARD_STYLE)) {
                int value = systemSettings.getInt(Settings.System.SETTINGS_DASHBOARD_STYLE);
                Settings.System.putIntForUser(resolver, Settings.System.SETTINGS_DASHBOARD_STYLE, value, UserHandle.USER_CURRENT);
                restored++;
            }

            if (systemSettings.has(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                int value = systemSettings.getInt(Settings.Secure.SYSTEM_CUSTOM_THEME);
                Settings.Secure.putIntForUser(resolver, Settings.Secure.SYSTEM_CUSTOM_THEME, value, UserHandle.USER_CURRENT);
                restored++;
            }

            if (systemSettings.has(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                int value = systemSettings.getInt(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED);
                Settings.System.putIntForUser(resolver, Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, value, UserHandle.USER_CURRENT);
                restored++;
            }

            if (systemSettings.has("settings_blocked_pages_list")) {
                String value = systemSettings.getString("settings_blocked_pages_list");
                Settings.System.putStringForUser(resolver, "settings_blocked_pages_list", value, UserHandle.USER_CURRENT);
                restored++;
            }

            // Restore security settings if present
            if (backupData.has("security_settings")) {
                JSONObject securitySettings = backupData.getJSONObject("security_settings");
                String[] securityKeys = {
                    "block_analytics_enabled",
                    "block_diagnostic_data_enabled",
                    "block_personalization_enabled",
                    "block_android_intelligence_enabled",
                    "block_ad_services_enabled",
                    "block_cloud_backup_enabled",
                    "block_location_services_enabled",
                    "block_network_scanning_enabled",
                    "block_webview_updates_enabled",
                    "block_system_updates_enabled",
                    "block_app_installation_enabled"
                };
                for (String key : securityKeys) {
                    if (securitySettings.has(key)) {
                        int value = securitySettings.getInt(key);
                        Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                        restored++;
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






