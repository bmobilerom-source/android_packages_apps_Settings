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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

/**
 * Monet Color Presets settings fragment.
 * Provides predefined color themes for Material You.
 */
public class MonetColorPresets extends DashboardFragment {

    private static final String TAG = "MonetColorPresets";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.monet_color_presets;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Controller is defined in XML via settings:controller attribute
        // Add it here to ensure it's properly initialized
        controllers.add(new MonetColorPresetsController(context, "monet_color_preset"));

        return controllers;
    }

    public static final com.android.settings.search.BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new com.android.settings.search.BaseSearchIndexProvider(R.xml.monet_color_presets) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return false;
                }
            };
}
