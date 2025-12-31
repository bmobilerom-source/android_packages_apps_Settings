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

package com.android.settings.theme;

import android.app.settings.SettingsEnums;
import android.content.Context;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.epic.fragments.CustomThemeEngineController;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for custom theme engine (advanced theming)
 */
public class CustomThemeEngineSettings extends DashboardFragment {

    private static final String TAG = "CustomThemeEngineSettings";

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.custom_theme_engine_settings;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    public int getHelpResource() {
        return 0;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Add custom theme engine controllers if they exist
        // Note: Controller is optional - if preference key doesn't exist in XML, controller won't be used
        try {
            if (context != null) {
                controllers.add(new com.epic.fragments.CustomThemeEngineController(context, "theme_engine_enabled"));
            }
        } catch (Exception e) {
            // Controller not available, continue without it
            android.util.Log.w("CustomThemeEngineSettings", "CustomThemeEngineController not available", e);
        }

        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.custom_theme_engine_settings);
}
