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
            boolean isEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.MONET_CONTEXTUAL_ENABLED, 0) == 1;
            preference.setSummary(isEnabled ?
                "Colors change based on notifications (enabled)" :
                "Colors change based on notifications (disabled)");
        }
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.MONET_CONTEXTUAL_ENABLED, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            Settings.Secure.putString(mContext.getContentResolver(),
                    Settings.Secure.MONET_CONTEXTUAL_COLORS, null);
            Settings.Secure.putString(mContext.getContentResolver(),
                    Settings.Secure.MONET_LAST_NOTIFICATION_PACKAGE, null);
        }

        return Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.MONET_CONTEXTUAL_ENABLED, isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
