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

import android.app.UiModeManager;
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
 * Controller for Color Temperature toggle preference
 * Controls night display color temperature feature
 */
public class ColorTemperatureController extends TogglePreferenceController {

    private static final String TAG = "ColorTemperatureController";
    private static final String KEY_COLOR_TEMPERATURE = "color_temperature";
    private final UiModeManager mUiModeManager;

    public ColorTemperatureController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mUiModeManager = context.getSystemService(UiModeManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        // Only available if night display is supported
        if (mUiModeManager == null) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        // Check if night display is enabled
        return Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.NIGHT_DISPLAY_ACTIVATED,
                0,
                UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.NIGHT_DISPLAY_ACTIVATED,
                isChecked ? 1 : 0,
                UserHandle.USER_CURRENT);
        
        if (result) {
            // Notify system of change
            ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(
                    Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_ACTIVATED),
                    null);
            Log.d(TAG, "Color temperature (night display) " + (isChecked ? "enabled" : "disabled"));
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

