/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
public class PowerTweaksSettings extends DashboardFragment {

    private static final String TAG = "PowerTweaksSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return R.xml.power_tweaks_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, com.android.settingslib.core.lifecycle.Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        // crDroid features
        controllers.add(new FastChargingController(context, "fast_charging"));
        controllers.add(new ChargingLedController(context, "charging_led"));
        controllers.add(new BatterySaverAutoController(context, "battery_saver_auto"));
        controllers.add(new WakeOnChargeController(context, "wake_on_charge"));
        controllers.add(new ChargingSoundController(context, "charging_sound"));
        // Axion A16 features
        controllers.add(new SmartChargingController(context, "smart_charging"));
        controllers.add(new BatteryCalibrationController(context, "battery_calibration"));
        controllers.add(new PowerEfficientModeController(context, "power_efficient_mode"));
        controllers.add(new ScreenOffOptimizationController(context, "screen_off_optimization"));
        controllers.add(new ChargingAnimationController(context, "charging_animation"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.anatolia_settings_power) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}

