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
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Enhanced production-ready fragment for backing up and restoring Settings preferences.
 * Features:
 * - Storage Access Framework (SAF) for file picker
 * - Backup of System Optimizations and Security Features
 * - Robust error handling and validation
 * - File verification
 * - Progress feedback
 */
public class SettingsBackupRestoreImproved extends SettingsPreferenceFragment
        implements OnPreferenceClickListener, OnPreferenceChangeListener {

    private static final String TAG = "SettingsBackupRestore";
    private static final int BACKUP_VERSION = 3; // Incremented for new features
    private static final int REQUEST_CODE_BACKUP = 1001;
    private static final int REQUEST_CODE_RESTORE = 1002;

    private static final String KEY_BACKUP_SETTINGS = "backup_settings";
    private static final String KEY_RESTORE_SETTINGS = "restore_settings";
    private static final String KEY_BACKUP_SELECTION = "backup_selection";

    private Preference mBackupPreference;
    private Preference mRestorePreference;
    private MultiSelectListPreference mBackupSelectionPreference;

    // Settings categories
    private static final String CATEGORY_DASHBOARD = "dashboard_style";
    private static final String CATEGORY_THEME = "custom_theme";
    private static final String CATEGORY_WALLPAPER = "wallpaper_background";
    private static final String CATEGORY_BLOCKED_PAGES = "blocked_pages";
    private static final String CATEGORY_SECURITY = "security_features";
    private static final String CATEGORY_SYSTEM_OPTIMIZATIONS = "system_optimizations";
    private static final String CATEGORY_SECURITY_FEATURES = "security_features_new";

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
            Set<String> defaultSelection = new HashSet<>();
            defaultSelection.add(CATEGORY_DASHBOARD);
            defaultSelection.add(CATEGORY_THEME);
            defaultSelection.add(CATEGORY_WALLPAPER);
            defaultSelection.add(CATEGORY_BLOCKED_PAGES);
            defaultSelection.add(CATEGORY_SECURITY);
            defaultSelection.add(CATEGORY_SYSTEM_OPTIMIZATIONS);
            defaultSelection.add(CATEGORY_SECURITY_FEATURES);
            mBackupSelectionPreference.setValues(defaultSelection);
            mBackupSelectionPreference.setOnPreferenceChangeListener(this);
            updateBackupSelectionSummary();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mBackupPreference) {
            startBackupFilePicker();
            return true;
        } else if (preference == mRestorePreference) {
            startRestoreFilePicker();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            if (requestCode == REQUEST_CODE_BACKUP || requestCode == REQUEST_CODE_RESTORE) {
                Toast.makeText(getActivity(), 
                        getString(R.string.settings_backup_restore_cancelled),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Uri uri = data.getData();
        if (requestCode == REQUEST_CODE_BACKUP) {
            backupSettings(uri);
        } else if (requestCode == REQUEST_CODE_RESTORE) {
            restoreSettings(uri);
        }
    }

    private void updateBackupSelectionSummary() {
        if (mBackupSelectionPreference == null) return;
        Set<String> selected = mBackupSelectionPreference.getValues();
        int count = selected != null ? selected.size() : 0;
        mBackupSelectionPreference.setSummary(
                getString(R.string.settings_backup_selection_summary, count));
    }

    private void startBackupFilePicker() {
        Set<String> selectedCategories = mBackupSelectionPreference.getValues();
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            Toast.makeText(getActivity(), 
                    R.string.settings_backup_no_selection, 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "settings_backup_" + System.currentTimeMillis() + ".json");
        startActivityForResult(intent, REQUEST_CODE_BACKUP);
    }

    private void startRestoreFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_CODE_RESTORE);
    }

    private void backupSettings(Uri uri) {
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
            JSONObject secureSettings = new JSONObject();
            JSONObject globalSettings = new JSONObject();
            int backedUp = 0;

            // Backup Dashboard Style
            if (selectedCategories.contains(CATEGORY_DASHBOARD)) {
                int value = Settings.System.getIntForUser(resolver, 
                        Settings.System.SETTINGS_DASHBOARD_STYLE, 0, UserHandle.USER_CURRENT);
                systemSettings.put(Settings.System.SETTINGS_DASHBOARD_STYLE, value);
                backedUp++;
            }

            // Backup Custom Theme
            if (selectedCategories.contains(CATEGORY_THEME)) {
                int value = Settings.Secure.getIntForUser(resolver, 
                        Settings.Secure.SYSTEM_CUSTOM_THEME, 0, UserHandle.USER_CURRENT);
                secureSettings.put(Settings.Secure.SYSTEM_CUSTOM_THEME, value);
                backedUp++;
            }

            // Backup Wallpaper Background
            if (selectedCategories.contains(CATEGORY_WALLPAPER)) {
                int value = Settings.System.getIntForUser(resolver, 
                        Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, 0, UserHandle.USER_CURRENT);
                systemSettings.put(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, value);
                backedUp++;
            }

            // Backup Blocked Pages
            if (selectedCategories.contains(CATEGORY_BLOCKED_PAGES)) {
                try {
                    String value = Settings.System.getStringForUser(resolver, 
                            "settings_blocked_pages_list", UserHandle.USER_CURRENT);
                    if (value != null && !value.isEmpty()) {
                        systemSettings.put("settings_blocked_pages_list", value);
                        backedUp++;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Blocked pages setting not found", e);
                }
            }

            // Backup Legacy Security Features
            if (selectedCategories.contains(CATEGORY_SECURITY)) {
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
                    try {
                        int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        securitySettings.put(key, value);
                        backedUp++;
                    } catch (Exception e) {
                        Log.w(TAG, "Security setting not found: " + key, e);
                    }
                }
                if (securitySettings.length() > 0) {
                    backupData.put("security_settings", securitySettings);
                }
            }

            // Backup System Optimizations (NEW)
            if (selectedCategories.contains(CATEGORY_SYSTEM_OPTIMIZATIONS)) {
                JSONObject optimizationSettings = new JSONObject();
                String[] optimizationKeys = {
                    "background_app_limits_enabled",
                    "network_optimization_enabled",
                    "battery_optimization_enabled",
                    "storage_optimization_enabled",
                    "thermal_throttling_enabled"
                };
                for (String key : optimizationKeys) {
                    try {
                        int value = Settings.System.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        optimizationSettings.put(key, value);
                        backedUp++;
                    } catch (Exception e) {
                        Log.w(TAG, "Optimization setting not found: " + key, e);
                    }
                }
                if (optimizationSettings.length() > 0) {
                    backupData.put("system_optimizations", optimizationSettings);
                }
            }

            // Backup Security Features (NEW - from SecurityFeaturesHelper)
            if (selectedCategories.contains(CATEGORY_SECURITY_FEATURES)) {
                JSONObject securityFeaturesSettings = new JSONObject();
                String[] securityFeatureKeys = {
                    "network_permission_control_enabled",
                    "app_hardening_enabled",
                    "sensor_access_control_enabled",
                    "location_access_control_enabled",
                    "microphone_access_control_enabled"
                };
                for (String key : securityFeatureKeys) {
                    try {
                        int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        securityFeaturesSettings.put(key, value);
                        backedUp++;
                    } catch (Exception e) {
                        Log.w(TAG, "Security feature setting not found: " + key, e);
                    }
                }
                if (securityFeaturesSettings.length() > 0) {
                    backupData.put("security_features_new", securityFeaturesSettings);
                }
            }

            // Add metadata
            backupData.put("system_settings", systemSettings);
            backupData.put("secure_settings", secureSettings);
            backupData.put("global_settings", globalSettings);
            backupData.put("backup_version", BACKUP_VERSION);
            backupData.put("timestamp", System.currentTimeMillis());
            backupData.put("android_version", android.os.Build.VERSION.RELEASE);
            backupData.put("device_model", android.os.Build.MODEL);
            backupData.put("selected_categories", new JSONArray(selectedCategories));

            // Write to file using SAF
            try (OutputStream outputStream = resolver.openOutputStream(uri);
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                writer.write(backupData.toString(2));
                writer.flush();
            }

            String fileName = getFileNameFromUri(uri);
            Toast.makeText(getActivity(),
                    getString(R.string.settings_backup_success_detailed, backedUp, fileName),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Settings backed up: " + backedUp + " items to " + fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error backing up settings", e);
            Toast.makeText(getActivity(), 
                    getString(R.string.settings_backup_error) + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void restoreSettings(Uri uri) {
        try {
            ContentResolver resolver = getActivity().getContentResolver();
            
            // Read file using SAF
            StringBuilder jsonContent = new StringBuilder();
            try (InputStream inputStream = resolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line).append("\n");
                }
            }

            if (jsonContent.length() == 0) {
                Toast.makeText(getActivity(), 
                        R.string.settings_restore_invalid_file, 
                        Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject backupData = new JSONObject(jsonContent.toString());
            
            // Validate backup version
            int backupVersion = backupData.optInt("backup_version", 1);
            if (backupVersion > BACKUP_VERSION) {
                Toast.makeText(getActivity(),
                        getString(R.string.settings_restore_version_newer, backupVersion, BACKUP_VERSION),
                        Toast.LENGTH_LONG).show();
                return;
            }

            int restored = 0;
            JSONObject systemSettings = backupData.optJSONObject("system_settings");
            JSONObject secureSettings = backupData.optJSONObject("secure_settings");
            JSONObject globalSettings = backupData.optJSONObject("global_settings");

            // Restore System Settings
            if (systemSettings != null) {
                restored += restoreSystemSettings(resolver, systemSettings);
            }

            // Restore Secure Settings
            if (secureSettings != null) {
                restored += restoreSecureSettings(resolver, secureSettings);
            }

            // Restore Global Settings
            if (globalSettings != null) {
                restored += restoreGlobalSettings(resolver, globalSettings);
            }

            // Restore Legacy Security Settings
            if (backupData.has("security_settings")) {
                JSONObject securitySettings = backupData.getJSONObject("security_settings");
                restored += restoreSecuritySettings(resolver, securitySettings);
            }

            // Restore System Optimizations (NEW)
            if (backupData.has("system_optimizations")) {
                JSONObject optimizationSettings = backupData.getJSONObject("system_optimizations");
                restored += restoreOptimizationSettings(resolver, optimizationSettings);
            }

            // Restore Security Features (NEW)
            if (backupData.has("security_features_new")) {
                JSONObject securityFeaturesSettings = backupData.getJSONObject("security_features_new");
                restored += restoreSecurityFeaturesSettings(resolver, securityFeaturesSettings);
            }

            Toast.makeText(getActivity(),
                    getString(R.string.settings_restore_success, restored),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Restored " + restored + " settings");

            // Reload activity to apply changes
            if (getActivity() != null) {
                getActivity().recreate();
            }
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Invalid backup file format", e);
            Toast.makeText(getActivity(), 
                    R.string.settings_restore_invalid_file, 
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error restoring settings", e);
            Toast.makeText(getActivity(), 
                    getString(R.string.settings_restore_error) + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private int restoreSystemSettings(ContentResolver resolver, JSONObject settings) {
        int restored = 0;
        try {
            if (settings.has(Settings.System.SETTINGS_DASHBOARD_STYLE)) {
                int value = settings.getInt(Settings.System.SETTINGS_DASHBOARD_STYLE);
                Settings.System.putIntForUser(resolver, Settings.System.SETTINGS_DASHBOARD_STYLE, value, UserHandle.USER_CURRENT);
                restored++;
            }
            if (settings.has(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                int value = settings.getInt(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED);
                Settings.System.putIntForUser(resolver, Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED, value, UserHandle.USER_CURRENT);
                restored++;
            }
            if (settings.has("settings_blocked_pages_list")) {
                String value = settings.getString("settings_blocked_pages_list");
                Settings.System.putStringForUser(resolver, "settings_blocked_pages_list", value, UserHandle.USER_CURRENT);
                restored++;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring system settings", e);
        }
        return restored;
    }

    private int restoreSecureSettings(ContentResolver resolver, JSONObject settings) {
        int restored = 0;
        try {
            if (settings.has(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                int value = settings.getInt(Settings.Secure.SYSTEM_CUSTOM_THEME);
                Settings.Secure.putIntForUser(resolver, Settings.Secure.SYSTEM_CUSTOM_THEME, value, UserHandle.USER_CURRENT);
                restored++;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring secure settings", e);
        }
        return restored;
    }

    private int restoreGlobalSettings(ContentResolver resolver, JSONObject settings) {
        // Placeholder for global settings restoration
        return 0;
    }

    private int restoreSecuritySettings(ContentResolver resolver, JSONObject settings) {
        int restored = 0;
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
            try {
                if (settings.has(key)) {
                    int value = settings.getInt(key);
                    Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                    restored++;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error restoring security setting: " + key, e);
            }
        }
        return restored;
    }

    private int restoreOptimizationSettings(ContentResolver resolver, JSONObject settings) {
        int restored = 0;
        String[] optimizationKeys = {
            "background_app_limits_enabled",
            "network_optimization_enabled",
            "battery_optimization_enabled",
            "storage_optimization_enabled",
            "thermal_throttling_enabled"
        };
        for (String key : optimizationKeys) {
            try {
                if (settings.has(key)) {
                    int value = settings.getInt(key);
                    Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                    restored++;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error restoring optimization setting: " + key, e);
            }
        }
        return restored;
    }

    private int restoreSecurityFeaturesSettings(ContentResolver resolver, JSONObject settings) {
        int restored = 0;
        String[] securityFeatureKeys = {
            "network_permission_control_enabled",
            "app_hardening_enabled",
            "sensor_access_control_enabled",
            "location_access_control_enabled",
            "microphone_access_control_enabled"
        };
        for (String key : securityFeatureKeys) {
            try {
                if (settings.has(key)) {
                    int value = settings.getInt(key);
                    Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                    restored++;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error restoring security feature setting: " + key, e);
            }
        }
        return restored;
    }

    private String getFileNameFromUri(Uri uri) {
        try {
            // Try to get filename from URI path
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                    return path.substring(lastSlash + 1);
                }
            }
            // Fallback to query parameter
            String displayName = uri.getQueryParameter("displayName");
            if (displayName != null) {
                return displayName;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get file name from URI", e);
        }
        return uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "backup.json";
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DASHBOARD_SUMMARY;
    }
}
