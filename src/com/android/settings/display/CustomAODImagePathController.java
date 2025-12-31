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

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Custom AOD Image Path preference
 */
public class CustomAODImagePathController extends BasePreferenceController {

    private static final String TAG = "CustomAODImagePathController";
    private static final String KEY_CUSTOM_AOD_IMAGE_PATH = "custom_aod_image_path";

    public CustomAODImagePathController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        ContentResolver resolver = mContext.getContentResolver();
        String path = Settings.System.getString(resolver, KEY_CUSTOM_AOD_IMAGE_PATH);
        return path != null && !path.isEmpty() ? path : "No image selected";
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CUSTOM_AOD_IMAGE_PATH;
    }
}


