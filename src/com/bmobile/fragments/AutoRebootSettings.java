/*
 * Copyright (C) 2025 BashaMobile
 *
 * Auto Reboot Settings - schedule automatic device reboots.
 */

package com.bmobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings fragment for Auto Reboot. Controllers are registered manually
 * (katheleya-quick pattern) for reliable binding.
 */
public class AutoRebootSettings extends DashboardFragment {

    private static final String TAG = "AutoRebootSettings";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AutoRebootMainSwitchController controller =
                (AutoRebootMainSwitchController) use(AutoRebootMainSwitchController.class);
        if (controller != null) {
            controller.handleActivityResult(requestCode, resultCode, data);
        }
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
        return R.xml.auto_reboot_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AutoRebootMainSwitchController(context, "auto_reboot_main_switch"));
        controllers.add(new AutoRebootIntervalController(context, "auto_reboot_interval"));
        return controllers;
    }
}
