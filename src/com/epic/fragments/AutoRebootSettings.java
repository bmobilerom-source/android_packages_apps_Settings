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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

public class AutoRebootSettings extends DashboardFragment {

    private static final String TAG = "AutoRebootSettings";
    private static final String KEY_AUTO_REBOOT_INTERVAL = "auto_reboot_interval";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1001;
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL_INTERVAL = 1002;

    private AutoRebootMainSwitchController mMainSwitchController;
    private AutoRebootIntervalController mIntervalController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Refresh preference states when returning to the screen
        Preference intervalPref = findPreference(KEY_AUTO_REBOOT_INTERVAL);
        if (intervalPref != null) {
            AutoRebootIntervalController controller = 
                    (AutoRebootIntervalController) use(AutoRebootIntervalController.class);
            if (controller != null) {
                controller.updateState(intervalPref);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            // Handle main switch PIN verification
            AutoRebootMainSwitchController controller =
                    (AutoRebootMainSwitchController) use(AutoRebootMainSwitchController.class);
            if (controller != null) {
                controller.handleActivityResult(requestCode, resultCode, data);
            }
        }
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

