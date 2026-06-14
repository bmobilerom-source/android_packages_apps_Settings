/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.fragments;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class DeviceControlsSettingsFragment extends DashboardFragment {

    private static final String TAG = "DeviceControlsSettingsFragment";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AmbientDisplayConfiguration config = new AmbientDisplayConfiguration(context);
        use(DoubleTapScreenPreferenceController.class).setConfig(config);
        use(PickupGesturePreferenceController.class).setConfig(config);
    }

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
        return R.xml.lockscreen_device_controls_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new DoubleTapScreenPreferenceController(context,
                "ambient_display_double_tap"));
        controllers.add(new PickupGesturePreferenceController(context,
                "ambient_display_pick_up"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.lockscreen_device_controls_settings);
}
