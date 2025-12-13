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
import android.content.SharedPreferences;

import com.android.settings.core.TogglePreferenceController;

public class ContextualColorsController extends TogglePreferenceController {

    private static final String PREF_FILE = "monet_prefs";
    private static final String KEY_CONTEXTUAL_ENABLED = "monet_contextual_enabled";
    private static final String KEY_CONTEXTUAL_COLORS = "monet_contextual_colors";
    private static final String KEY_LAST_NOTIFICATION = "monet_last_notification";

    public ContextualColorsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_CONTEXTUAL_ENABLED, false);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (isChecked) {
            // Clear any existing contextual data
            editor.putString(KEY_CONTEXTUAL_COLORS, null);
            editor.putString(KEY_LAST_NOTIFICATION, null);
        }

        return editor.putBoolean(KEY_CONTEXTUAL_ENABLED, isChecked).commit();
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
