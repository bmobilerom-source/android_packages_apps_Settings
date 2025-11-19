/*
 * Copyright (C) 2025 BashaMobile
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

package com.android.settings.security;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for pocket lock preference in lock screen settings.
 */
public class PocketLockPreferenceController extends TogglePreferenceController {
    
    private static final String TAG = "PocketLockController";
    private static final String SETTINGS_KEY = "pocket_mode_enabled";
    
    public PocketLockPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    
    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                SETTINGS_KEY,
                0,
                UserHandle.USER_CURRENT) != 0;
    }
    
    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                SETTINGS_KEY,
                isChecked ? 1 : 0,
                UserHandle.USER_CURRENT);
        if (result) {
            Log.d(TAG, "Pocket lock " + (isChecked ? "enabled" : "disabled"));
        }
        return result;
    }
    
    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_security;
    }
}

