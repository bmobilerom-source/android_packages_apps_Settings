/*
 * Copyright (C) 2025 BashaMobile
 *
 * Auto Reboot Settings - Schedule automatic device reboots
 * Inspired by GrapheneOS auto-reboot feature for security
 */

package com.epic.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Calendar;

/**
 * Settings fragment for Auto Reboot feature
 * Allows users to schedule automatic device reboots for security/maintenance
 */
public class AutoRebootSettings extends SettingsPreferenceFragment {
    private static final String TAG = "AutoRebootSettings";
    private static final String KEY_AUTO_REBOOT_ENABLED = "auto_reboot_enabled";
    private static final String KEY_AUTO_REBOOT_INTERVAL = "auto_reboot_interval";
    private static final String KEY_AUTO_REBOOT_TIME = "auto_reboot_time";
    private static final String PREF_NAME = "auto_reboot_prefs";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1001;
    
    private SwitchPreference mAutoRebootEnabled;
    private ListPreference mAutoRebootInterval;
    private Preference mAutoRebootTime;
    private boolean mIsAuthenticated = false;
    
    // Reboot intervals in hours
    private static final String[] INTERVAL_VALUES = {
        "24",   // Daily
        "48",   // Every 2 days
        "72",   // Every 3 days
        "168"   // Weekly
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.auto_reboot_settings);
        
        if (savedInstanceState != null) {
            mIsAuthenticated = savedInstanceState.getBoolean("is_authenticated", false);
        }
        
        setupPreferences();
        updatePreferenceStates();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_authenticated", mIsAuthenticated);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!mIsAuthenticated) {
            checkAndRequestAuthentication();
            return;
        }
        updatePreferenceStates();
    }
    
    private void setupPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        
        mAutoRebootEnabled = screen.findPreference(KEY_AUTO_REBOOT_ENABLED);
        mAutoRebootInterval = screen.findPreference(KEY_AUTO_REBOOT_INTERVAL);
        mAutoRebootTime = screen.findPreference(KEY_AUTO_REBOOT_TIME);
        
        if (mAutoRebootEnabled != null) {
            mAutoRebootEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                if (enabled && !mIsAuthenticated) {
                    checkAndRequestAuthentication();
                    return false; // Don't enable until authenticated
                }
                setAutoRebootEnabled(enabled);
                updatePreferenceStates();
                return true;
            });
        }
        
        if (mAutoRebootInterval != null) {
            mAutoRebootInterval.setOnPreferenceChangeListener((preference, newValue) -> {
                String interval = (String) newValue;
                setAutoRebootInterval(interval);
                updatePreferenceStates();
                scheduleReboot();
                return true;
            });
        }
        
        if (mAutoRebootTime != null) {
            mAutoRebootTime.setOnPreferenceClickListener(preference -> {
                showTimePicker();
                return true;
            });
        }
    }
    
    private void updatePreferenceStates() {
        boolean enabled = isAutoRebootEnabled();
        
        if (mAutoRebootEnabled != null) {
            mAutoRebootEnabled.setChecked(enabled);
        }
        
        if (mAutoRebootInterval != null) {
            mAutoRebootInterval.setEnabled(enabled);
            String interval = getAutoRebootInterval();
            mAutoRebootInterval.setValue(interval);
            mAutoRebootInterval.setSummary(getIntervalSummary(interval));
        }
        
        if (mAutoRebootTime != null) {
            mAutoRebootTime.setEnabled(enabled);
            String time = getAutoRebootTime();
            mAutoRebootTime.setSummary(time != null ? time : getString(R.string.auto_reboot_time_not_set));
        }
    }
    
    private void checkAndRequestAuthentication() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        
        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null || !km.isKeyguardSecure()) {
            mIsAuthenticated = true;
            return;
        }
        
        AutoRebootPinHelper.launchCredentialConfirmation(getActivity(), REQUEST_CODE_CONFIRM_CREDENTIAL);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (resultCode == Activity.RESULT_OK) {
                mIsAuthenticated = true;
                updatePreferenceStates();
            } else {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        String savedTime = getAutoRebootTime();
        
        if (savedTime != null) {
            String[] parts = savedTime.split(":");
            if (parts.length == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            }
        }
        
        TimePickerDialog timePicker = new TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                setAutoRebootTime(time);
                updatePreferenceStates();
                scheduleReboot();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(getContext())
        );
        
        timePicker.setTitle(getString(R.string.auto_reboot_time_picker_title));
        timePicker.show();
    }
    
    private void setAutoRebootEnabled(boolean enabled) {
        getSharedPreferences().edit()
            .putBoolean(KEY_AUTO_REBOOT_ENABLED, enabled)
            .apply();
        
        if (enabled) {
            scheduleReboot();
        } else {
            cancelReboot();
        }
    }
    
    private boolean isAutoRebootEnabled() {
        return getSharedPreferences().getBoolean(KEY_AUTO_REBOOT_ENABLED, false);
    }
    
    private void setAutoRebootInterval(String interval) {
        getSharedPreferences().edit()
            .putString(KEY_AUTO_REBOOT_INTERVAL, interval)
            .apply();
    }
    
    private String getAutoRebootInterval() {
        return getSharedPreferences().getString(KEY_AUTO_REBOOT_INTERVAL, "24");
    }
    
    private void setAutoRebootTime(String time) {
        getSharedPreferences().edit()
            .putString(KEY_AUTO_REBOOT_TIME, time)
            .apply();
    }
    
    private String getAutoRebootTime() {
        return getSharedPreferences().getString(KEY_AUTO_REBOOT_TIME, null);
    }
    
    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    private String getIntervalSummary(String interval) {
        int hours = Integer.parseInt(interval);
        if (hours == 24) {
            return getString(R.string.auto_reboot_interval_daily);
        } else if (hours == 48) {
            return getString(R.string.auto_reboot_interval_every_2_days);
        } else if (hours == 72) {
            return getString(R.string.auto_reboot_interval_every_3_days);
        } else if (hours == 168) {
            return getString(R.string.auto_reboot_interval_weekly);
        }
        return getString(R.string.auto_reboot_interval_custom, hours);
    }
    
    /**
     * Schedule automatic reboot using AlarmManager
     */
    private void scheduleReboot() {
        if (!isAutoRebootEnabled()) {
            return;
        }
        
        Context context = getContext();
        if (context == null) {
            return;
        }
        
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }
            
            // Calculate next reboot time
            Calendar calendar = Calendar.getInstance();
            String time = getAutoRebootTime();
            
            if (time != null) {
                String[] parts = time.split(":");
                if (parts.length == 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    
                    // If time has passed today, schedule for tomorrow
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                }
            } else {
                // Default: schedule for 24 hours from now
                calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(getAutoRebootInterval()));
            }
            
            // Create PendingIntent for reboot
            Intent rebootIntent = new Intent("com.android.settings.AUTO_REBOOT");
            rebootIntent.setClass(context, AutoRebootReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, rebootIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            // Schedule alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
            
            Log.d(TAG, "Auto reboot scheduled for: " + calendar.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule reboot", e);
        }
    }
    
    /**
     * Cancel scheduled reboot
     */
    private void cancelReboot() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return;
            }
            
            Intent rebootIntent = new Intent("com.android.settings.AUTO_REBOOT");
            rebootIntent.setClass(context, AutoRebootReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, rebootIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Auto reboot cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel reboot", e);
        }
    }
}

