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
    private Preference mBackupLocationPreference;

    // Security Features Settings.Secure keys
    private static final String[] SECURITY_SETTINGS_KEYS = {
            "network_permission_control_enabled",
            "app_hardening_enabled",
            "sensor_access_control_enabled",
            "location_access_control_enabled",
            "microphone_access_control_enabled"
    };

    // Power Tweaks Settings.System keys
    private static final String[] POWER_TWEAKS_SETTINGS_KEYS = {
            Settings.System.HAPTIC_FEEDBACK_ENABLED,
            Settings.System.ACCELEROMETER_ROTATION,
            "status_bar_show_battery_percent",
            Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD,
            Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
            "status_bar_clock",
            "lock_screen_rotation_enabled",
            "long_press_copy_enabled",
            "double_tap_to_wake_enabled",
            "swipe_screenshot_enabled",
            "volume_key_music_control_enabled",
            "pocket_mode_enabled",
            "edge_lighting_enabled",
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
            Settings.Secure.ACCESSIBILITY_LARGE_POINTER_ICON,
            Settings.System.SCREEN_OFF_TIMEOUT,
            Settings.Secure.NOTIFICATION_BADGING
    };

    // AOSPMods Settings.System keys
    private static final String[] AOSPMODS_SETTINGS_KEYS = {
            "status_bar_height_factor",
            "notification_icon_limit",
            "combined_signal_icons",
            "hide_roaming_state",
            "volte_icon_enabled",
            "vowifi_icon_enabled",
            "hide_privacy_chip",
            "system_icons_multi_row",
            "notification_area_multi_row",
            "network_on_sb_enabled"
    };

    // General Settings keys
    private static final String[] GENERAL_SETTINGS_KEYS = {
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

        // Show backup location
        updateBackupLocation();
    }

    private void updateBackupLocation() {
        File backupFile = new File(getActivity().getExternalFilesDir(null), "settings_backup.json");
        Preference locationPref = findPreference("backup_location");
        if (locationPref != null) {
            if (backupFile.exists()) {
                locationPref.setSummary(backupFile.getAbsolutePath());
                locationPref.setVisible(true);
            } else {
                locationPref.setSummary(R.string.settings_backup_no_backup);
                locationPref.setVisible(true);
            }
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

            // Backup Security Features (Settings.Secure)
            JSONObject securitySettings = new JSONObject();
            for (String key : SECURITY_SETTINGS_KEYS) {
                try {
                    int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                    securitySettings.put(key, value);
                } catch (Exception e) {
                    Log.w(TAG, "Error backing up security setting: " + key, e);
                }
            }
            backupData.put("security_settings", securitySettings);

            // Backup Power Tweaks (Settings.System)
            JSONObject powerTweaksSettings = new JSONObject();
            for (String key : POWER_TWEAKS_SETTINGS_KEYS) {
                try {
                    if (key.startsWith("Settings.System.") || key.startsWith("Settings.Secure.")) {
                        // Skip constants, handle separately
                        continue;
                    }
                    int value = Settings.System.getIntForUser(resolver, key, -1, UserHandle.USER_CURRENT);
                    if (value != -1) {
                        powerTweaksSettings.put(key, value);
                    }
                } catch (Exception e) {
                    // Try Secure
                    try {
                        int value = Settings.Secure.getIntForUser(resolver, key, -1, UserHandle.USER_CURRENT);
                        if (value != -1) {
                            powerTweaksSettings.put(key, value);
                        }
                    } catch (Exception e2) {
                        Log.w(TAG, "Error backing up power tweak: " + key, e2);
                    }
                }
            }
            // Handle known Settings constants
            try {
                powerTweaksSettings.put(Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    Settings.System.getIntForUser(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.System.ACCELEROMETER_ROTATION,
                    Settings.System.getIntForUser(resolver, Settings.System.ACCELEROMETER_ROTATION, 1, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.getIntForUser(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.System.SCREEN_OFF_TIMEOUT,
                    Settings.System.getIntForUser(resolver, Settings.System.SCREEN_OFF_TIMEOUT, 60000, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD,
                    Settings.Secure.getIntForUser(resolver, Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                    Settings.Secure.getIntForUser(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
                    Settings.Secure.getIntForUser(resolver, Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.Secure.ACCESSIBILITY_LARGE_POINTER_ICON,
                    Settings.Secure.getIntForUser(resolver, Settings.Secure.ACCESSIBILITY_LARGE_POINTER_ICON, 0, UserHandle.USER_CURRENT));
                powerTweaksSettings.put(Settings.Secure.NOTIFICATION_BADGING,
                    Settings.Secure.getIntForUser(resolver, Settings.Secure.NOTIFICATION_BADGING, 1, UserHandle.USER_CURRENT));
            } catch (Exception e) {
                Log.w(TAG, "Error backing up system constants", e);
            }
            backupData.put("power_tweaks_settings", powerTweaksSettings);

            // Backup AOSPMods Settings (Settings.System)
            JSONObject aospmodsSettings = new JSONObject();
            for (String key : AOSPMODS_SETTINGS_KEYS) {
                try {
                    int value = Settings.System.getIntForUser(resolver, key, -1, UserHandle.USER_CURRENT);
                    if (value != -1) {
                        aospmodsSettings.put(key, value);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error backing up AOSPMods setting: " + key, e);
                }
            }
            backupData.put("aospmods_settings", aospmodsSettings);

            // Backup General Settings
            JSONObject generalSettings = new JSONObject();
            for (String key : GENERAL_SETTINGS_KEYS) {
                try {
                    if (key.equals(Settings.System.SETTINGS_DASHBOARD_STYLE) ||
                        key.equals(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                        int value = Settings.System.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        generalSettings.put(key, value);
                    } else if (key.equals(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                        int value = Settings.Secure.getIntForUser(resolver, key, 0, UserHandle.USER_CURRENT);
                        generalSettings.put(key, value);
                    } else if (key.equals("settings_blocked_pages_list")) {
                        String value = Settings.System.getStringForUser(resolver, key, UserHandle.USER_CURRENT);
                        if (value != null && !value.isEmpty()) {
                            generalSettings.put(key, value);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error backing up general setting: " + key, e);
                }
            }
            backupData.put("general_settings", generalSettings);

            backupData.put("backup_version", 2);
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
            
            // Update backup location display
            updateBackupLocation();
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
            ContentResolver resolver = getActivity().getContentResolver();
            int restored = 0;

            // Restore Security Features
            if (backupData.has("security_settings")) {
                JSONObject securitySettings = backupData.getJSONObject("security_settings");
                for (String key : SECURITY_SETTINGS_KEYS) {
                    if (securitySettings.has(key)) {
                        try {
                            int value = securitySettings.getInt(key);
                            Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            restored++;
                        } catch (Exception e) {
                            Log.w(TAG, "Error restoring security setting: " + key, e);
                        }
                    }
                }
            }

            // Restore Power Tweaks
            if (backupData.has("power_tweaks_settings")) {
                JSONObject powerTweaksSettings = backupData.getJSONObject("power_tweaks_settings");
                for (String key : POWER_TWEAKS_SETTINGS_KEYS) {
                    if (powerTweaksSettings.has(key)) {
                        try {
                            int value = powerTweaksSettings.getInt(key);
                            // Try System first, then Secure
                            try {
                                Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            } catch (Exception e) {
                                Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            }
                            restored++;
                        } catch (Exception e) {
                            Log.w(TAG, "Error restoring power tweak: " + key, e);
                        }
                    }
                }
                // Handle known constants
                try {
                    if (powerTweaksSettings.has(Settings.System.HAPTIC_FEEDBACK_ENABLED)) {
                        Settings.System.putIntForUser(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            powerTweaksSettings.getInt(Settings.System.HAPTIC_FEEDBACK_ENABLED), UserHandle.USER_CURRENT);
                        restored++;
                    }
                    if (powerTweaksSettings.has(Settings.System.ACCELEROMETER_ROTATION)) {
                        Settings.System.putIntForUser(resolver, Settings.System.ACCELEROMETER_ROTATION,
                            powerTweaksSettings.getInt(Settings.System.ACCELEROMETER_ROTATION), UserHandle.USER_CURRENT);
                        restored++;
                    }
                    if (powerTweaksSettings.has(Settings.Secure.NOTIFICATION_BADGING)) {
                        Settings.Secure.putIntForUser(resolver, Settings.Secure.NOTIFICATION_BADGING,
                            powerTweaksSettings.getInt(Settings.Secure.NOTIFICATION_BADGING), UserHandle.USER_CURRENT);
                        restored++;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error restoring system constants", e);
                }
            }

            // Restore AOSPMods Settings
            if (backupData.has("aospmods_settings")) {
                JSONObject aospmodsSettings = backupData.getJSONObject("aospmods_settings");
                for (String key : AOSPMODS_SETTINGS_KEYS) {
                    if (aospmodsSettings.has(key)) {
                        try {
                            int value = aospmodsSettings.getInt(key);
                            Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                            restored++;
                        } catch (Exception e) {
                            Log.w(TAG, "Error restoring AOSPMods setting: " + key, e);
                        }
                    }
                }
            }

            // Restore General Settings (backward compatibility)
            if (backupData.has("system_settings")) {
                JSONObject systemSettings = backupData.getJSONObject("system_settings");
                for (String key : GENERAL_SETTINGS_KEYS) {
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
                            Log.w(TAG, "Error restoring general setting: " + key, e);
                        }
                    }
                }
            }
            if (backupData.has("general_settings")) {
                JSONObject generalSettings = backupData.getJSONObject("general_settings");
                for (String key : GENERAL_SETTINGS_KEYS) {
                    if (generalSettings.has(key)) {
                        try {
                            if (key.equals(Settings.System.SETTINGS_DASHBOARD_STYLE) ||
                                key.equals(Settings.System.SETTINGS_WALLPAPER_BACKGROUND_ENABLED)) {
                                int value = generalSettings.getInt(key);
                                Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                                restored++;
                            } else if (key.equals(Settings.Secure.SYSTEM_CUSTOM_THEME)) {
                                int value = generalSettings.getInt(key);
                                Settings.Secure.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
                                restored++;
                            } else {
                                String value = generalSettings.getString(key);
                                Settings.System.putStringForUser(resolver, key, value, UserHandle.USER_CURRENT);
                                restored++;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error restoring general setting: " + key, e);
                        }
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

