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
 * Controller for AOD Always On When Charging toggle
 */
public class AODAlwaysOnChargeController extends TogglePreferenceController {

    private static final String TAG = "AODAlwaysOnChargeController";
    private static final String KEY_DOZE_ALWAYS_ON_CHARGE = "doze_always_on_charge_mode";

    public AODAlwaysOnChargeController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.System.getInt(resolver, KEY_DOZE_ALWAYS_ON_CHARGE, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.System.putInt(resolver, KEY_DOZE_ALWAYS_ON_CHARGE, isChecked ? 1 : 0);
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
