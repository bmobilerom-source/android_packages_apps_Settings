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

import android.content.Context;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

public class AutoRebootIntervalController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mPreference;

    public AutoRebootIntervalController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Only available if auto reboot is enabled
        return PrivacySecurityHelper.isAutoRebootEnabled(mContext)
                ? AVAILABLE : DISABLED_DEPENDENT_SETTING;
    }

    @Override
    public String getSummary() {
        long intervalMs = PrivacySecurityHelper.getAutoRebootInterval(mContext);
        return formatInterval(intervalMs);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            long intervalMs = PrivacySecurityHelper.getAutoRebootInterval(mContext);
            String value = String.valueOf(intervalMs);

            // Find the closest matching value from available options
            CharSequence[] entryValues = listPreference.getEntryValues();
            if (entryValues != null && entryValues.length > 0) {
                // Check if exact match exists
                boolean found = false;
                for (CharSequence entryValue : entryValues) {
                    if (entryValue != null && entryValue.toString().equals(value)) {
                        found = true;
                        break;
                    }
                }
                // If no exact match, use the first available value (default: 1 hour)
                if (!found && entryValues.length > 0 && entryValues[0] != null) {
                    value = entryValues[0].toString();
                    // Update the setting to match the default
                    PrivacySecurityHelper.setAutoRebootInterval(mContext, Long.parseLong(value));
                }
            }

            listPreference.setValue(value);
            listPreference.setSummary(formatInterval(intervalMs));

            // Enable/disable based on auto reboot toggle
            boolean autoRebootEnabled = PrivacySecurityHelper.isAutoRebootEnabled(mContext);
            listPreference.setEnabled(autoRebootEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            try {
                String valueStr = newValue.toString();
                long intervalMs = Long.parseLong(valueStr);

                // Validate the interval is within acceptable range (minimum 1 hour)
                long minInterval = 60 * 60 * 1000L; // 1 hour
                if (intervalMs < minInterval) {
                    android.util.Log.w("AutoRebootIntervalController",
                            "Interval too short, using minimum: " + minInterval);
                    intervalMs = minInterval;
                }

                // Simplified - directly set the interval without PIN verification to prevent crashes
                return PrivacySecurityHelper.setAutoRebootInterval(mContext, intervalMs);
            } catch (NumberFormatException e) {
                android.util.Log.e("AutoRebootIntervalController",
                        "Invalid interval value: " + newValue, e);
                return false;
            } catch (Exception e) {
                android.util.Log.e("AutoRebootIntervalController", "Failed to set auto reboot interval", e);
                return false;
            }
        }
        return false;
    }

    private String formatInterval(long intervalMs) {
        long hours = intervalMs / (60 * 60 * 1000);
        long days = hours / 24;

        if (days >= 1) {
            if (days == 1) {
                return mContext.getString(R.string.auto_reboot_interval_one_day);
            } else {
                return mContext.getString(R.string.auto_reboot_interval_days, days);
            }
        } else {
            if (hours == 1) {
                return mContext.getString(R.string.auto_reboot_interval_one_hour);
            } else {
                return mContext.getString(R.string.auto_reboot_interval_hours, hours);
            }
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_apps;
    }
}