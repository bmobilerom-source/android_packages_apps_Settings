/*
 * Copyright (C) 2025 LineageOS
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

package com.epic.fragments;

import android.content.Context;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for dedicated WiFi tile in Quick Settings
 */
public class DedicatedWifiTileController extends TogglePreferenceController {

    private static final String SYSTEM_PROP_DEDICATED_WIFI_TILE = "persist.sys.qs_dedicated_wifi_tile";

    public DedicatedWifiTileController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return SystemProperties.getBoolean(SYSTEM_PROP_DEDICATED_WIFI_TILE, true);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        SystemProperties.set(SYSTEM_PROP_DEDICATED_WIFI_TILE, isChecked ? "true" : "false");
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
