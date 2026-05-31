/*
 * Copyright (C) 2025 BashaMobile
 *
 * Auto Reboot Settings - schedule automatic device reboots.
 */

package com.bmobile.fragments;

import android.content.Intent;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

/**
 * Settings fragment for Auto Reboot. Controllers are wired from
 * {@link R.xml#auto_reboot_settings}.
 */
public class AutoRebootSettings extends DashboardFragment {

    private static final String TAG = "AutoRebootSettings";

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AutoRebootMainSwitchController controller = use(AutoRebootMainSwitchController.class);
        if (controller != null) {
            controller.handleActivityResult(requestCode, resultCode, data);
        }
    }
}
