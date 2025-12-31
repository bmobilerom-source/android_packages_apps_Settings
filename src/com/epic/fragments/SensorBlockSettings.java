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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This is part of the Sensor Block per-package feature.
 * Based on: https://github.com/BootleggersROM/packages_apps_BootlegDumpster/commit/431482bace0109c0c34914e1988d6fb6c2dde434
 * 
 * To transfer to other ROMs:
 * 1. Copy this file and sensor_block.xml
 * 2. Update package name if needed
 * 3. Ensure Settings.System is available
 * 4. Framework-side implementation may be needed for actual sensor blocking
 */

package com.epic.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;

/**
 * Sensor Block Settings
 * 
 * Allows enabling/disabling sensor block per-package feature.
 * Master switch that controls whether sensor blocking is active.
 */
public class SensorBlockSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "SensorBlockSettings";
    private static final String KEY_SENSOR_BLOCK = "sensor_block";
    private static final String KEY_APP_COUNT = "sensor_block_app_count";
    private static final String SETTING_KEY = "sensor_block_packages";
    private static final String KEY_SENSOR_BLOCK_LEVEL = "sensor_block_level";

    /**
     * Helper method to consistently access sensor block packages setting
     * Uses Settings.Secure first, falls back to Settings.System
     */
    private String getSensorBlockPackages() {
        final ContentResolver resolver = getActivity().getContentResolver();

        // Try Settings.Secure first
        String packages = Settings.Secure.getString(resolver, SETTING_KEY);
        if (packages != null) {
            return packages;
        }

        // Fallback to Settings.System
        return Settings.System.getString(resolver, SETTING_KEY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sensor_block);

        final ContentResolver resolver = getActivity().getContentResolver();

        // Update sensor block switch state
        Preference sensorBlockPref = findPreference(KEY_SENSOR_BLOCK);
        if (sensorBlockPref != null) {
            // Remove any restrictions and enable the preference
            if (sensorBlockPref instanceof com.android.settingslib.RestrictedPreference) {
                ((com.android.settingslib.RestrictedPreference) sensorBlockPref)
                        .setDisabledByAdmin(null);
            }
            sensorBlockPref.setEnabled(true);
            sensorBlockPref.setSelectable(true);
            
            // Try Settings.Secure first, fallback to Settings.System
            int sensorBlockEnabled = Settings.Secure.getInt(resolver, KEY_SENSOR_BLOCK, 
                    Settings.System.getInt(resolver, KEY_SENSOR_BLOCK, 0));
            if (sensorBlockPref instanceof androidx.preference.TwoStatePreference) {
                ((androidx.preference.TwoStatePreference) sensorBlockPref)
                        .setChecked(sensorBlockEnabled != 0);
            }
            sensorBlockPref.setOnPreferenceChangeListener(this);
        }

        // Ensure the add packages preference is enabled and clickable
        Preference addPackagesPref = findPreference("add_sensor_block_packages");
        if (addPackagesPref != null) {
            addPackagesPref.setEnabled(true);
            addPackagesPref.setSelectable(true);
            if (addPackagesPref instanceof com.android.settingslib.RestrictedPreference) {
                ((com.android.settingslib.RestrictedPreference) addPackagesPref)
                        .setDisabledByAdmin(null);
            }
        }

        // Update app count summary
        updateAppCountSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        final ContentResolver resolver = getActivity().getContentResolver();
        
        // Ensure preferences are enabled and update state
        Preference sensorBlockPref = findPreference(KEY_SENSOR_BLOCK);
        if (sensorBlockPref != null) {
            sensorBlockPref.setEnabled(true);
            sensorBlockPref.setSelectable(true);
            if (sensorBlockPref instanceof com.android.settingslib.RestrictedPreference) {
                ((com.android.settingslib.RestrictedPreference) sensorBlockPref)
                        .setDisabledByAdmin(null);
            }
            // Update switch state to reflect current setting
            int sensorBlockEnabled = Settings.Secure.getInt(resolver, KEY_SENSOR_BLOCK, 
                    Settings.System.getInt(resolver, KEY_SENSOR_BLOCK, 0));
            if (sensorBlockPref instanceof androidx.preference.TwoStatePreference) {
                ((androidx.preference.TwoStatePreference) sensorBlockPref)
                        .setChecked(sensorBlockEnabled != 0);
            }
        }
        
        Preference addPackagesPref = findPreference("add_sensor_block_packages");
        if (addPackagesPref != null) {
            addPackagesPref.setEnabled(true);
            addPackagesPref.setSelectable(true);
            if (addPackagesPref instanceof com.android.settingslib.RestrictedPreference) {
                ((com.android.settingslib.RestrictedPreference) addPackagesPref)
                        .setDisabledByAdmin(null);
            }
        }
        
        // Update app count when returning to this page
        updateAppCountSummary();
    }

    private void updateAppCountSummary() {
        Preference appCountPref = findPreference(KEY_APP_COUNT);
        if (appCountPref == null) {
            return;
        }

        try {
            String packages = getSensorBlockPackages();
            int count = 0;

            if (packages != null && !packages.isEmpty()) {
                String[] packageArray = packages.split(",");
                for (String pkg : packageArray) {
                    if (!pkg.trim().isEmpty()) {
                        count++;
                    }
                }
            }

            if (count == 0) {
                appCountPref.setSummary(R.string.sensor_block_app_count_summary_default);
            } else {
                String summary = getString(R.string.sensor_block_app_count_summary, count);
                appCountPref.setSummary(summary);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating app count summary", e);
            appCountPref.setSummary(R.string.sensor_block_app_count_summary_default);
        }

        // Update sensor block level preference
        ContentResolver resolver = getActivity().getContentResolver();
        androidx.preference.ListPreference blockLevelPref = 
                findPreference(KEY_SENSOR_BLOCK_LEVEL);
        if (blockLevelPref != null) {
            // Try Settings.Secure first, fallback to Settings.System
            int level = Settings.Secure.getInt(resolver, KEY_SENSOR_BLOCK_LEVEL,
                    Settings.System.getInt(resolver, KEY_SENSOR_BLOCK_LEVEL, 0));
            blockLevelPref.setValue(String.valueOf(level));
            updateBlockLevelSummary(blockLevelPref, level);
            blockLevelPref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String key = preference.getKey();

        try {
            if (KEY_SENSOR_BLOCK.equals(key)) {
                boolean enabled = (Boolean) newValue;
                // Try Settings.Secure first, fallback to Settings.System
                boolean saved = Settings.Secure.putInt(resolver, KEY_SENSOR_BLOCK, enabled ? 1 : 0);
                if (!saved) {
                    saved = Settings.System.putInt(resolver, KEY_SENSOR_BLOCK, enabled ? 1 : 0);
                }
                Log.d(TAG, "sensor_block set to: " + enabled + " (saved: " + saved + ")");
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.SENSOR_BLOCK_CHANGED");
                intent.putExtra("enabled", enabled);
                getActivity().sendBroadcast(intent);
                
                return saved;
            } else if (KEY_SENSOR_BLOCK_LEVEL.equals(key)) {
                int level = Integer.parseInt((String) newValue);
                // Try Settings.Secure first, fallback to Settings.System
                boolean saved = Settings.Secure.putInt(resolver, KEY_SENSOR_BLOCK_LEVEL, level);
                if (!saved) {
                    saved = Settings.System.putInt(resolver, KEY_SENSOR_BLOCK_LEVEL, level);
                }
                Log.d(TAG, "sensor_block_level set to: " + level + " (saved: " + saved + ")");
                
                if (preference instanceof androidx.preference.ListPreference) {
                    updateBlockLevelSummary((androidx.preference.ListPreference) preference, level);
                }
                
                // Broadcast change for framework to pick up
                android.content.Intent intent = new android.content.Intent("com.android.settings.SENSOR_BLOCK_LEVEL_CHANGED");
                intent.putExtra("level", level);
                getActivity().sendBroadcast(intent);
                
                return saved;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference: " + key, e);
        }

        return false;
    }

    private void updateBlockLevelSummary(androidx.preference.ListPreference pref, int level) {
        if (pref != null) {
            String[] entries = getResources().getStringArray(R.array.sensor_block_level_entries);
            if (entries != null && level >= 0 && level < entries.length) {
                pref.setSummary(entries[level]);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if ("add_sensor_block_packages".equals(key)) {
            // Manually launch the fragment to ensure it works
            new SubSettingLauncher(getContext())
                    .setDestination(SensorBlockAppPicker.class.getName())
                    .setSourceMetricsCategory(getMetricsCategory())
                    .launch();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

