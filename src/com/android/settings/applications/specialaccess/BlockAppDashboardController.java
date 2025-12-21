/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.settings.applications.specialaccess;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for blocking access to AppDashboardFragment.
 * When enabled, users cannot access the Apps settings page.
 */
public class BlockAppDashboardController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "block_app_dashboard_enabled";
    private static final String TAG = "BlockAppDashboard";

    public BlockAppDashboardController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        // Default to disabled (0) - allow access by default
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY,
                isChecked ? 1 : 0);
        if (result) {
            Log.d(TAG, "App Dashboard access " + (isChecked ? "blocked" : "allowed"));
        }
        return result;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPref = (SwitchPreference) preference;
            // Ensure preference is enabled but not selectable
            switchPref.setEnabled(true);
            switchPref.setSelectable(false);
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_apps;
    }

    /**
     * Check if AppDashboardFragment access is blocked.
     * This is a static method that can be called from other classes.
     */
    public static boolean isBlocked(Context context) {
        if (context == null) {
            return false;
        }
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }
}

