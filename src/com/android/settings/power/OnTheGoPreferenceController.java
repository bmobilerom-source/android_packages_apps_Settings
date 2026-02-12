/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.settings.power;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.BasePreferenceController;

import java.util.List;

/**
 * Controller for "On the Go mode" preference
 */
public class OnTheGoPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "OnTheGoPreferenceController";
    private static final String KEY_ON_THE_GO = "global_actions_onthego";
    
    // OnTheGo service component
    private static final ComponentName ONTHEGO_SERVICE = new ComponentName(
            "com.android.systemui",
            "com.android.systemui.epic.onthego.OnTheGoService");

    public OnTheGoPreferenceController(Context context) {
        super(context, KEY_ON_THE_GO);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ON_THE_GO;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
            boolean isEnabled = isOnTheGoServiceRunning();
            Log.d(TAG, "updateState called - service running: " + isEnabled);
            switchPreference.setChecked(isEnabled);
            
            // Also check Settings.System value
            int systemValue = android.provider.Settings.System.getInt(
                    mContext.getContentResolver(),
                    android.provider.Settings.System.ON_THE_GO_ENABLED, 0);
            Log.d(TAG, "Settings.System.ON_THE_GO_ENABLED value: " + systemValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SwitchPreferenceCompat) {
            boolean enabled = (Boolean) newValue;
            toggleOnTheGoService(enabled);
            return true;
        }
        return false;
    }

    /**
     * Check if OnTheGo service is currently running
     */
    private boolean isOnTheGoServiceRunning() {
        try {
            ActivityManager activityManager = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> services = activityManager
                    .getRunningServices(Integer.MAX_VALUE);

            if (services != null) {
                for (ActivityManager.RunningServiceInfo info : services) {
                    if (info.service != null && info.service.getClassName() != null) {
                        if (info.service.getClassName().equals(ONTHEGO_SERVICE.getClassName())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking OnTheGo service status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Start or stop the OnTheGo service
     * 
     * Uses EXACT same code as Power Menu and QS Tile (which both work).
     * Copied from GlobalActionsDialogLite.java:1649-1674
     */
    private void toggleOnTheGoService(boolean enabled) {
        Log.e(TAG, "========================================");
        Log.e(TAG, "Starting OnTheGo service (method identical to power menu)");
        Log.e(TAG, "========================================");
        
        // SAME CODE AS POWER MENU (GlobalActionsDialogLite line 1658-1663)
        ComponentName cn = new ComponentName("com.android.systemui",
                "com.android.systemui.epic.onthego.OnTheGoService");
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(cn);
        serviceIntent.setAction(enabled ? "start" : "stop");
        
        Log.e(TAG, "Calling mContext.startService()...");
        try {
            ComponentName result = mContext.startService(serviceIntent);
            if (result != null) {
                Log.e(TAG, "✓✓✓ SUCCESS! Service started: " + result);
            } else {
                Log.e(TAG, "⚠️ startService returned null (service might be starting)");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "✗✗✗ SECURITY EXCEPTION: " + e.getMessage());
            // Use broadcast fallback
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(enabled ? "com.android.systemui.epic.onthego.START" : "com.android.systemui.epic.onthego.STOP");
            broadcastIntent.setPackage("com.android.systemui");
            mContext.sendBroadcast(broadcastIntent);
            Log.e(TAG, "✓ Sent broadcast as fallback");
        }
    }
}
