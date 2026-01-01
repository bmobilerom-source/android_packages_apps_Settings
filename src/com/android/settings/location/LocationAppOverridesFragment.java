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
 * Fragment for location app overrides settings.
 * Allows users to set per-app location precision overrides.
 * 
 * NOTE: This feature requires framework support for per-app location precision,
 * which is not yet implemented. This is a placeholder fragment.
 */
public class LocationAppOverridesFragment extends DashboardFragment {

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.location_app_overrides;
    }

    @Override
    protected String getLogTag() {
        return "LocationAppOverrides";
    }

    @Override
    public int getMetricsCategory() {
        return 0; // TODO: Add proper metrics category
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.location_app_overrides_title);
    }
}

