/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.TogglePreferenceController;

public class ContextualColorsController extends TogglePreferenceController {

    private static final String TAG = "ContextualColorsController";
    private static final String KEY_MONET_CONTEXTUAL_ENABLED = "monet_contextual_enabled";

    public ContextualColorsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            boolean isEnabled = isChecked();
            if (isEnabled && !isNotificationListenerEnabled()) {
                preference.setSummary("Tap to grant notification access permission");
            } else {
                preference.setSummary(isEnabled ?
                    mContext.getString(com.android.settings.R.string.monet_contextual_colors_enable_summary) :
                    mContext.getString(com.android.settings.R.string.monet_contextual_colors_enable_summary));
            }
        }
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
                KEY_MONET_CONTEXTUAL_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        ContentResolver resolver = mContext.getContentResolver();

        // Set the main toggle
        boolean success = Settings.Secure.putIntForUser(resolver,
                KEY_MONET_CONTEXTUAL_ENABLED, isChecked ? 1 : 0, UserHandle.USER_CURRENT);

        if (success) {
            if (isChecked) {
                // Check if we have notification listener permission
                if (!isNotificationListenerEnabled()) {
                    // Request permission by opening notification listener settings
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    // Don't start service yet - wait for permission
                    Log.d(TAG, "Requesting notification listener permission");
                    return true; // Return true to show toggle as enabled, but don't start service
                } else {
                    // Start the contextual colors service
                    startContextualColorsService();
                }

                // Clear any existing contextual data when enabled
                Settings.System.putStringForUser(resolver,
                        "monet_contextual_colors", null, UserHandle.USER_CURRENT);
                Settings.System.putStringForUser(resolver,
                        "monet_last_notification", null, UserHandle.USER_CURRENT);
            } else {
                // Stop the contextual colors service
                stopContextualColorsService();
            }

            // Notify SystemUI of the change
            resolver.notifyChange(
                    Settings.Secure.getUriFor(KEY_MONET_CONTEXTUAL_ENABLED),
                    null);

            Log.d(TAG, "Contextual colors " + (isChecked ? "enabled" : "disabled"));
        }

        return success;
    }

    private boolean isNotificationListenerEnabled() {
        String packageName = mContext.getPackageName();
        String flat = Settings.Secure.getString(mContext.getContentResolver(),
                "enabled_notification_listeners");
        if (flat != null && !flat.isEmpty()) {
            String[] names = flat.split(":");
            for (String name : names) {
                android.content.ComponentName cn = android.content.ComponentName.unflattenFromString(name);
                if (cn != null && cn.getPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startContextualColorsService() {
        Intent serviceIntent = new Intent(mContext, ContextualColorsService.class);
        mContext.startForegroundService(serviceIntent);
    }

    private void stopContextualColorsService() {
        Intent serviceIntent = new Intent(mContext, ContextualColorsService.class);
        mContext.stopService(serviceIntent);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return com.android.settings.R.string.menu_key_display;
    }
}
