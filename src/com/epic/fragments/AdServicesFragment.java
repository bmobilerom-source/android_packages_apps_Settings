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

import android.content.Intent;
import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Fragment for Ad Services settings.
 * Provides access to Ad Services controls and information.
 */
public class AdServicesFragment extends SettingsPreferenceFragment {

    @Override
    public int getMetricsCategory() {
        return com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.ad_services_settings);
        updateAdServicesAvailability();
    }

    private void updateAdServicesAvailability() {
        // Try to launch Ad Services directly when the fragment opens
        try {
            android.content.Intent intent = new android.content.Intent("android.intent.action.MAIN");
            intent.setClassName("com.android.adservices.api", "com.android.adservices.api.ui.settings.AdServicesSettingsActivity");
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            // If we get here, close this fragment since we launched the real settings
            getActivity().onBackPressed();
            return;
        } catch (Exception e) {
            // Ad Services activity not available, try fallback
            tryFallbackAdServices();
        }
    }

    private void tryFallbackAdServices() {
        // Try to open Ad Services through Settings
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_PRIVACY_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            getActivity().onBackPressed(); // Close this fragment
            return;
        } catch (Exception e) {
            // Show a message that Ad Services isn't available
            android.widget.Toast.makeText(getContext(),
                "Ad Services settings are not available on this device", android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.bmobile_ad_services_title);
    }
}
