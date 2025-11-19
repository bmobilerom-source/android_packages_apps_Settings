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
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class SystemOptimizationSettings extends DashboardFragment {

    private static final String TAG = "SystemOptimizationSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh all preference summaries to show current status
        // updateState() is called automatically by DashboardFragment, but we refresh here
        // to ensure real-time status is shown
        refreshPreferenceSummaries();
    }
    
    private void refreshPreferenceSummaries() {
        // Refresh all preference summaries to show real-time status
        // updatePreferenceStates() calls updateState() on all controllers automatically
        updatePreferenceStates();
    }

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
        return R.xml.system_optimization_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, com.android.settingslib.core.lifecycle.Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        // Only features that work out of the box without kernel/framework changes
        controllers.add(new BackgroundAppLimitsController(context, "background_app_limits"));
        controllers.add(new NetworkOptimizationController(context, "network_optimization"));
        controllers.add(new BatteryOptimizationController(context, "battery_optimization"));
        controllers.add(new StorageOptimizationController(context, "storage_optimization"));
        controllers.add(new ThermalThrottlingController(context, "thermal_throttling"));
        controllers.add(new OptimizationStatusController(context, "optimization_status"));
        // Removed controllers (require kernel/framework):
        // - MemoryOptimizationController
        // - CpuGovernorOptimizationController
        // - IoSchedulerOptimizationController
        // - ZramOptimizationController
        // - PerformanceModeController
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.system_optimization_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
