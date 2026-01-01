/*
 * Copyright (C) 2025 bmobile
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

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

/**
 * Controller for blocking access to SystemOptimizationSettings.
 * When enabled, users cannot access the System Optimization settings page.
 */
public class BlockSystemOptimizationController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "block_system_optimization_enabled";
    private static final String TAG = "BlockSystemOptimization";

    public BlockSystemOptimizationController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }

    /**
     * Check if SystemOptimizationSettings access is blocked.
     * This is a static method that can be called from other classes.
     */
    public static boolean isBlocked(Context context) {
        if (context == null) {
            return false;
        }
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }
}

