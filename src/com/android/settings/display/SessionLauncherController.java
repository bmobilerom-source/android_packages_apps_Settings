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

import android.content.Context;
import android.content.Intent;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for launching Session messenger app
 */
public class SessionLauncherController extends BasePreferenceController {

    private static final String SESSION_PACKAGE = "network.loki.messenger";
    private static final String SESSION_ACTIVITY = "network.loki.messenger.RoutingActivity";

    public SessionLauncherController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        android.util.Log.d("SessionLauncherController", "handlePreferenceTreeClick called for key: " + preference.getKey() + ", controller key: " + getPreferenceKey());

        if (!preference.getKey().equals(getPreferenceKey())) {
            android.util.Log.d("SessionLauncherController", "Key mismatch, calling super");
            return super.handlePreferenceTreeClick(preference);
        }

        android.util.Log.d("SessionLauncherController", "Launching Session app");

        try {
            Intent intent = new Intent();
            intent.setClassName(SESSION_PACKAGE, SESSION_ACTIVITY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            android.util.Log.d("SessionLauncherController", "Session app launched successfully");
        } catch (Exception e) {
            android.util.Log.e("SessionLauncherController", "Failed to launch Session app", e);
            // App not installed or other error
            android.widget.Toast.makeText(mContext,
                "Session app not installed", android.widget.Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}