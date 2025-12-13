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
 * AOSPMods Settings - Advanced SystemUI customizations
 */
public class AOSPModsSettings extends DashboardFragment {

    private static final String TAG = "AOSPModsSettings";

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
        return R.xml.aospmods_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Status Bar controllers
        controllers.add(new StatusBarHeightFactorController(context, "status_bar_height_factor"));
        controllers.add(new NotificationIconLimitController(context, "notification_icon_limit"));
        controllers.add(new CombinedSignalIconsController(context, "combined_signal_icons"));
        controllers.add(new HideRoamingStateController(context, "hide_roaming_state"));
        controllers.add(new VolteIconController(context, "volte_icon_enabled"));
        controllers.add(new VowifiIconController(context, "vowifi_icon_enabled"));
        controllers.add(new HidePrivacyChipController(context, "hide_privacy_chip"));
        controllers.add(new SystemIconsMultiRowController(context, "system_icons_multi_row"));
        controllers.add(new NotificationAreaMultiRowController(context, "notification_area_multi_row"));
        controllers.add(new NetworkOnSBController(context, "network_on_sb_enabled"));

        // Note: Quick Settings features are already implemented in LineageOS Quick Settings
        // Only unique Status Bar features are included here

        return controllers;
    }
}
