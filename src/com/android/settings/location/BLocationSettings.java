/*
 * Copyright (C) 2024 The Android Open Source Project
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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Fragment for enhanced location privacy and status settings.
 * Provides advanced location controls beyond basic location toggle.
 */
@SearchIndexable
public class BLocationSettings extends DashboardFragment {
    private static final String TAG = "BLocationSettings";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.LOCATION;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.blocation;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Initialize controllers
        use(LocationPrivacyIndicatorsController.class);
        use(LocationPrecisionController.class);
        use(LocationTimeRestrictionsController.class);
        use(LocationAppOverridesController.class);
        use(LocationSharingControlsController.class);
        use(LocationServiceStatusController.class);
        use(LocationAccuracyIndicatorController.class);
        use(LocationPrivacyFooterController.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.location_privacy_dashboard_title);
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blocation);
}
