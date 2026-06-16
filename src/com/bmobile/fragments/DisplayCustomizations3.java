/*
 * Copyright (C) 2021 Wave-OS
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

package com.bmobile.fragments;

import android.content.Context;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.bmobile.customization.AnimatorDurationScaleController;
import com.bmobile.customization.TransitionAnimationScaleController;
import com.bmobile.customization.WindowAnimationScaleController;

import java.util.ArrayList;
import java.util.List;

/**
 * Animation settings page — transition style and animation speed controls.
 * Opened from the Display grid Animation card.
 */
@SearchIndexable
public class DisplayCustomizations3 extends DashboardFragment {

    private static final String TAG = "DisplayCustomizations3";

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
        return R.xml.display_customizations3;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new SystemAnimationStyleController(context, "system_animation_style"));
        controllers.add(new WindowAnimationScaleController(context));
        controllers.add(new TransitionAnimationScaleController(context));
        controllers.add(new AnimatorDurationScaleController(context));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_customizations3);
}
