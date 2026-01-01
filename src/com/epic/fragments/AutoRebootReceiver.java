/*
 * Copyright (C) 2025 BashaMobile
 *
 * BroadcastReceiver for Auto Reboot feature
 * Receives alarm and triggers device reboot
 */

package com.epic.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

/**
 * BroadcastReceiver that handles scheduled reboot alarms
 * When alarm fires, this receiver triggers the device reboot
 */
public class AutoRebootReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoRebootReceiver";
    private static final String PREF_NAME = "auto_reboot_prefs";
    private static final String KEY_AUTO_REBOOT_ENABLED = "auto_reboot_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Auto reboot alarm received");
        
        // Check if auto reboot is still enabled
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_AUTO_REBOOT_ENABLED, false);
        
        if (!enabled) {
            Log.d(TAG, "Auto reboot is disabled, cancelling");
            return;
        }
        
        // Perform reboot
        performReboot(context);
    }
    
    /**
     * Perform device reboot using PowerManager
     */
    private void performReboot(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                Log.d(TAG, "Rebooting device...");
                pm.reboot("auto_reboot");
            } else {
                Log.e(TAG, "PowerManager is null, cannot reboot");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to reboot device", e);
        }
    }
}

