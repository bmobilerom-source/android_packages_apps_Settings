/*
 * Copyright (C) 2025 EpicROM-AOSP
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

package com.epic.services;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

/**
 * Receiver to handle Auto Reboot logic.
 * Monitors boot and unlock events to schedule a reboot if the device is not unlocked for a set duration.
 * Implements the backend logic for Settings.Secure.AUTO_REBOOT_ENABLED/DELAY.
 */
public class AutoRebootReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoRebootReceiver";
    private static final String PREF_NAME = "auto_reboot_prefs";
    private static final String KEY_LAST_UNLOCK = "last_unlock_time";
    private static final String ACTION_AUTO_REBOOT = "com.epic.action.AUTO_REBOOT_TRIGGER";
    private static final String ACTION_CONFIG_CHANGED = "com.epic.action.AUTO_REBOOT_CONFIG_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            Intent.ACTION_USER_PRESENT.equals(action) ||
            ACTION_CONFIG_CHANGED.equals(action)) {
            
            // Update last unlock time (Boot is considered an "unlock" point for safety initially, 
            // but strictly User Present is the unlock)
            // We reset the timer on boot to avoid loops, and on unlock.
            if (!ACTION_CONFIG_CHANGED.equals(action)) {
                updateLastUnlockTime(context);
            }
            scheduleAutoReboot(context);
            
        } else if (ACTION_AUTO_REBOOT.equals(action)) {
            performAutoReboot(context);
        }
    }

    private void updateLastUnlockTime(Context context) {
        long now = SystemClock.elapsedRealtime();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
               .edit()
               .putLong(KEY_LAST_UNLOCK, now)
               .apply();
        Log.d(TAG, "Updated last unlock time: " + now);
    }

    private void scheduleAutoReboot(Context context) {
        boolean enabled = Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_ENABLED, 0) == 1;
        
        if (!enabled) {
            cancelAutoReboot(context);
            return;
        }

        long delayMs = Settings.Secure.getLong(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_DELAY, 3600000L); // Default 1 hour

        long lastUnlock = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                                 .getLong(KEY_LAST_UNLOCK, SystemClock.elapsedRealtime());

        long triggerTime = lastUnlock + delayMs;
        long now = SystemClock.elapsedRealtime();

        if (triggerTime <= now) {
             // Should have already rebooted, or delay changed. Schedule immediately? 
             // Better give a grace period if we just booted/updated.
             triggerTime = now + 60000; // 1 minute grace
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoRebootReceiver.class);
        intent.setAction(ACTION_AUTO_REBOOT);
        
        // Use FLAG_IMMUTABLE for Android 12+ compatibility
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use setWindow or setExactAndAllowWhileIdle for reliability
        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        
        Log.d(TAG, "Scheduled auto reboot for: " + triggerTime + " (in " + (triggerTime - now) + "ms)");
    }

    private void cancelAutoReboot(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoRebootReceiver.class);
        intent.setAction(ACTION_AUTO_REBOOT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        Log.d(TAG, "Cancelled auto reboot alarm");
    }

    private void performAutoReboot(Context context) {
        Log.d(TAG, "Attempting auto reboot...");
        
        // Double check enabled state
        boolean enabled = Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.AUTO_REBOOT_ENABLED, 0) == 1;
        if (!enabled) {
            Log.w(TAG, "Auto reboot disabled, aborting.");
            return;
        }

        // Check if device is actually locked
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null && !km.isKeyguardLocked()) {
             Log.w(TAG, "Device is unlocked, aborting auto reboot.");
             // Reschedule since it's unlocked (User Present will handle it, but safety net)
             updateLastUnlockTime(context);
             scheduleAutoReboot(context);
             return;
        }
        
        // Check time requirement
        long delayMs = Settings.Secure.getLong(context.getContentResolver(),
                Settings.Secure.AUTO_REBOOT_DELAY, 3600000L);
        long lastUnlock = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                                 .getLong(KEY_LAST_UNLOCK, 0);
        long now = SystemClock.elapsedRealtime();
        
        if ((now - lastUnlock) < delayMs) {
            Log.w(TAG, "Time requirement not met (maybe clock changed?), rescheduling.");
            scheduleAutoReboot(context);
            return;
        }

        // Perform Reboot
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                Log.i(TAG, "Rebooting device due to inactivity...");
                pm.reboot("auto_reboot");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to reboot", e);
        }
    }
}

