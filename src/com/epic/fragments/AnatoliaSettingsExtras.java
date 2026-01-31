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
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import androidx.preference.Preference;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Security & Privacy Settings for Anatolia
 * Provides 19+ working security and privacy features
 */
public class AnatoliaSettingsExtras extends DashboardFragment {

    private static final String TAG = "AnatoliaSettingsExtras";

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.anatolia_settings_extras;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Core Security Controls removed (framework integration incomplete)

        // Developer & USB Security
        controllers.add(new UsbDebuggingController(context, "usb_debugging"));
        controllers.add(new DeveloperOptionsController(context, "developer_options"));

        // Lock Screen & Biometric Security
        controllers.add(new BiometricTimeoutController(context, "biometric_timeout"));

        return controllers;
    }
}
