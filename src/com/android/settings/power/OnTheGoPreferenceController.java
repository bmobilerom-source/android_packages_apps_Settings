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
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.List;

/**
 * Controller for "On the Go mode" preference
 */
public class OnTheGoPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "OnTheGoPreferenceController";
    private static final String KEY_ON_THE_GO = "global_actions_onthego";
    
    // OnTheGo service component
    private static final ComponentName ONTHEGO_SERVICE = new ComponentName(
            "com.android.systemui",
            "com.android.systemui.epic.onthego.OnTheGoService");

    public OnTheGoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ON_THE_GO;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreferenceCompat) {
            SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
            boolean isEnabled = isOnTheGoServiceRunning();
            switchPreference.setChecked(isEnabled);
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
     */
    private void toggleOnTheGoService(boolean enabled) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(ONTHEGO_SERVICE);
            
            if (enabled) {
                serviceIntent.setAction("start");
                Log.d(TAG, "Starting OnTheGo service");
            } else {
                serviceIntent.setAction("stop");
                Log.d(TAG, "Stopping OnTheGo service");
            }
            
            mContext.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error toggling OnTheGo service: " + e.getMessage());
        }
    }
}
