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

package com.android.settings.location;

import android.content.Context;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings fragment for Mock Location settings.
 * Allows users to enable/disable mock location providers.
 */
public class MockLocationsSettings extends DashboardFragment {

    private static final String TAG = "MockLocationsSettings";

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.mock_locations_settings;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            MockLocationsSettings fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new MockLocationController(context, "mock_location_enabled"));
        controllers.add(new MockLocationWarningController(context, "mock_location_warning"));
        controllers.add(new MockLocationAppPreferenceController(context, "mock_location_app", fragment));
        return controllers;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = buildPreferenceControllers(context, this);
        // Add lifecycle observers for controllers that need it
        final Lifecycle lifecycle = getSettingsLifecycle();
        for (AbstractPreferenceController controller : controllers) {
            if (controller instanceof com.android.settingslib.core.lifecycle.LifecycleObserver) {
                lifecycle.addObserver((com.android.settingslib.core.lifecycle.LifecycleObserver) controller);
            }
        }
        return controllers;
    }
}


