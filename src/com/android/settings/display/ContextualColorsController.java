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
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.TogglePreferenceController;

public class ContextualColorsController extends TogglePreferenceController {

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
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_contextual_enabled", 0) == 1;
            preference.setSummary(isEnabled ?
                "Colors change based on notifications (enabled)" :
                "Colors change based on notifications (disabled)");
        }
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                "monet_contextual_enabled", 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            // Clear any existing contextual data when enabled
            Settings.System.putString(mContext.getContentResolver(),
                    "monet_contextual_colors", null);
            Settings.System.putString(mContext.getContentResolver(),
                    "monet_last_notification", null);
        }

        return Settings.System.putInt(mContext.getContentResolver(),
                "monet_contextual_enabled", isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
