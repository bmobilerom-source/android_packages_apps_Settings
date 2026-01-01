/*
 * Copyright (C) 2025 bmobile
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

import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

/**
 * Fragment for location sharing controls.
 * Allows users to control location sharing and background access.
 */
public class LocationSharingControlsFragment extends DashboardFragment {

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.location_sharing_controls;
    }

    @Override
    protected String getLogTag() {
        return "LocationSharingControls";
    }

    @Override
    public int getMetricsCategory() {
        return 0; // TODO: Add proper metrics category
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.location_sharing_controls_title);
    }

    @Override
    public void onAttach(android.content.Context context) {
        super.onAttach(context);
        
        // Initialize controllers
        use(LocationSharingController.class);
        use(LocationBackgroundAccessController.class);
    }
}

