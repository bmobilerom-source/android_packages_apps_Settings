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

package com.android.settings.location;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings page for Mock Location App selection
 * Works independently of Developer Options
 */
@SearchIndexable
public class MockLocationsSettings extends SettingsPreferenceFragment {

    private static final String TAG = "MockLocationsSettings";
    private MockLocationAppPreferenceController mMockLocationController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mock_locations_settings);
        
        // Setup controller manually
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            mMockLocationController = new MockLocationAppPreferenceController(
                    getContext(), "mock_location_app", this);
            mMockLocationController.displayPreference(screen);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMockLocationController != null) {
            mMockLocationController.updateState(findPreference("mock_location_app"));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mMockLocationController != null) {
            mMockLocationController.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.mock_locations_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
