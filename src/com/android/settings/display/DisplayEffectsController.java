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
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for Display Effects toggle preference
 * Controls Settings.System.SETTINGS_DISPLAY_EFFECTS_ENABLED
 */
public class DisplayEffectsController extends TogglePreferenceController {

    private static final String TAG = "DisplayEffectsController";
    private static final String KEY_DISPLAY_EFFECTS = "display_effects";
    private static final String SETTING_KEY = Settings.System.SETTINGS_DISPLAY_EFFECTS_ENABLED;

    public DisplayEffectsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getIntForUser(
                mContext.getContentResolver(),
                SETTING_KEY,
                0,
                UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.System.putIntForUser(
                mContext.getContentResolver(),
                SETTING_KEY,
                isChecked ? 1 : 0,
                UserHandle.USER_CURRENT);
        
        if (result) {
            // Notify system of change
            ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(
                    Settings.System.getUriFor(SETTING_KEY),
                    null);
            Log.d(TAG, "Display effects " + (isChecked ? "enabled" : "disabled"));
        }
        
        return result;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return com.android.settings.R.string.menu_key_display;
    }
}

