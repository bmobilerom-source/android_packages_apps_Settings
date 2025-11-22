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
import android.content.Context;
import android.content.Intent;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
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
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        // AbstractPreferenceController automatically sets OnPreferenceChangeListener
        // if the controller implements it (see AbstractPreferenceController.displayPreference)
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null && mPreference instanceof ListPreference) {
            // Ensure listener is set (base class should do this automatically, but verify)
            ListPreference listPref = (ListPreference) mPreference;
            if (listPref.getOnPreferenceChangeListener() == null) {
                listPref.setOnPreferenceChangeListener(this);
            }
            updateState(mPreference);
        }
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
                // If no exact match, find the closest value or use default
                if (!found) {
                    long closestDiff = Long.MAX_VALUE;
                    String closestValue = null;
                    for (CharSequence entryValue : entryValues) {
                        if (entryValue != null) {
                            try {
                                long entryMs = Long.parseLong(entryValue.toString());
                                long diff = Math.abs(entryMs - intervalMs);
                                if (diff < closestDiff) {
                                    closestDiff = diff;
                                    closestValue = entryValue.toString();
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid values
                            }
                        }
                    }
                    if (closestValue != null) {
                        value = closestValue;
                        // Update the setting to match the closest available value
                        PrivacySecurityHelper.setAutoRebootInterval(mContext, Long.parseLong(value));
                    } else if (entryValues.length > 0 && entryValues[0] != null) {
                        // Fallback to first value
                        value = entryValues[0].toString();
                        PrivacySecurityHelper.setAutoRebootInterval(mContext, Long.parseLong(value));
                    }
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
        if (preference instanceof ListPreference && newValue != null) {
            try {
                String valueStr = newValue.toString();
                long intervalMs = Long.parseLong(valueStr);
                
                // Validate the interval is within acceptable range (minimum 1 hour)
                long minInterval = 60 * 60 * 1000L; // 1 hour
                if (intervalMs < minInterval) {
                    android.util.Log.w("AutoRebootIntervalController", 
                            "Interval too short, using minimum: " + minInterval);
                    intervalMs = minInterval;
                    valueStr = String.valueOf(intervalMs);
                }
                
                boolean success = PrivacySecurityHelper.setAutoRebootInterval(mContext, intervalMs);
                if (success) {
                    // Notify that the setting changed - this triggers framework to reschedule
                    android.content.ContentResolver resolver = mContext.getContentResolver();
                    resolver.notifyChange(
                            android.provider.Settings.Secure.getUriFor(
                                    android.provider.Settings.Secure.AUTO_REBOOT_DELAY),
                            null, false);
                    
                    // Also notify the enabled setting in case framework needs to reschedule
                    resolver.notifyChange(
                            android.provider.Settings.Secure.getUriFor(
                                    android.provider.Settings.Secure.AUTO_REBOOT_ENABLED),
                            null, false);
                    
                    // Send broadcast to AutoRebootReceiver
                    android.content.Intent intent = new android.content.Intent("com.epic.action.AUTO_REBOOT_CONFIG_CHANGED");
                    intent.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(intent);
                    
                    // Update the preference state immediately
                    if (preference instanceof ListPreference) {
                        ListPreference listPref = (ListPreference) preference;
                        listPref.setValue(valueStr);
                        listPref.setSummary(formatInterval(intervalMs));
                    }
                    
                    android.util.Log.d("AutoRebootIntervalController", 
                            "Auto reboot interval set to: " + formatInterval(intervalMs) + 
                            " (" + intervalMs + " ms)");
                } else {
                    android.util.Log.e("AutoRebootIntervalController", 
                            "Failed to save auto reboot interval");
                }
                return success;
            } catch (NumberFormatException e) {
                android.util.Log.e("AutoRebootIntervalController", 
                        "Invalid interval value: " + newValue, e);
                return false;
            } catch (Exception e) {
                android.util.Log.e("AutoRebootIntervalController", 
                        "Failed to set auto reboot interval", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Apply the pending interval change after PIN verification succeeds.
     */
    public boolean applyPendingIntervalChange() {
        if (mPendingIntervalMs < 0) {
            return false;
        }
        
        try {
            boolean success = PrivacySecurityHelper.setAutoRebootInterval(mContext, mPendingIntervalMs);
            if (success) {
                // Notify that the setting changed - this triggers framework to reschedule
                android.content.ContentResolver resolver = mContext.getContentResolver();
                resolver.notifyChange(
                        android.provider.Settings.Secure.getUriFor(
                                android.provider.Settings.Secure.AUTO_REBOOT_DELAY),
                        null, false);
                
                // Also notify the enabled setting in case framework needs to reschedule
                resolver.notifyChange(
                        android.provider.Settings.Secure.getUriFor(
                                android.provider.Settings.Secure.AUTO_REBOOT_ENABLED),
                        null, false);
                
                // Send broadcast to AutoRebootReceiver
                android.content.Intent intent = new android.content.Intent("com.epic.action.AUTO_REBOOT_CONFIG_CHANGED");
                intent.setPackage(mContext.getPackageName());
                mContext.sendBroadcast(intent);
                
                // Update the preference state immediately
                if (mPreference != null) {
                    String valueStr = String.valueOf(mPendingIntervalMs);
                    mPreference.setValue(valueStr);
                    mPreference.setSummary(formatInterval(mPendingIntervalMs));
                }
                
                android.util.Log.d("AutoRebootIntervalController", 
                        "Auto reboot interval set to: " + formatInterval(mPendingIntervalMs) + 
                        " (" + mPendingIntervalMs + " ms)");
                
                // Reset pending value
                mPendingIntervalMs = -1;
            } else {
                android.util.Log.e("AutoRebootIntervalController", 
                        "Failed to save auto reboot interval");
            }
            return success;
        } catch (Exception e) {
            android.util.Log.e("AutoRebootIntervalController", 
                    "Failed to apply pending interval change", e);
            mPendingIntervalMs = -1;
            return false;
        }
    }

    /**
     * Handle activity result from credential confirmation.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (AutoRebootPinHelper.isCredentialConfirmed(resultCode)) {
                return applyPendingIntervalChange();
            } else {
                android.util.Log.w("AutoRebootIntervalController", 
                        "Credential verification failed or cancelled");
                // Reset pending value and update UI to current state
                mPendingIntervalMs = -1;
                if (mPreference != null) {
                    updateState(mPreference);
                }
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
        } else if (hours >= 1) {
            if (hours == 1) {
                return mContext.getString(R.string.auto_reboot_interval_one_hour);
            } else {
                return mContext.getString(R.string.auto_reboot_interval_hours, hours);
            }
        } else {
            long minutes = intervalMs / (60 * 1000);
            return mContext.getString(R.string.auto_reboot_interval_minutes, minutes);
        }
    }
}

