/*
 * Copyright (C) 2025 BashaMobile
 * Copyright (C) 2025 LineageOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.settings.display;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for Custom AOD Image Enabled toggle
 */
public class CustomAODImageEnabledController extends TogglePreferenceController {

    private static final String TAG = "CustomAODImageEnabledController";
    private static final String KEY_CUSTOM_AOD_IMAGE_ENABLED = "custom_aod_image_enabled";

    public CustomAODImageEnabledController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.System.getInt(resolver, KEY_CUSTOM_AOD_IMAGE_ENABLED, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.System.putInt(resolver, KEY_CUSTOM_AOD_IMAGE_ENABLED, isChecked ? 1 : 0);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return com.android.settings.R.string.menu_key_display;
    }
}
