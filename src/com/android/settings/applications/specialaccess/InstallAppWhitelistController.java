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
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for toggling the install app whitelist restriction.
 * When enabled, only whitelisted apps (Aurora Store, Google Play) can install apps.
 * When disabled, all apps can install apps (default behavior).
 */
public class InstallAppWhitelistController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "install_app_whitelist_enabled";

    public InstallAppWhitelistController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Always available - can be shown in anatolia.xml or special_access.xml
        // The visibility is controlled by settings:isPreferenceVisible in XML
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY,
                isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_apps;
    }

    /**
     * Check if the whitelist restriction is enabled.
     * This is a static method that can be called from other classes.
     */
    public static boolean isWhitelistEnabled(Context context) {
        if (context == null) {
            return false;
        }
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }
}

