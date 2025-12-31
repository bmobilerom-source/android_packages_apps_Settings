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

import com.android.internal.logging.nano.MetricsProto;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Display Color Settings
 * Consolidates all color-related display settings in one place
 */
public class DisplayColorSettings extends SettingsPreferenceFragment {

    private static final String TAG = "DisplayColorSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_color_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DISPLAY;
    }
}


