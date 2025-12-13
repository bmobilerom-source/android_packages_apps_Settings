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
import android.provider.Settings;
import com.android.settings.core.SliderPreferenceController;

public class QsSecondaryLabelScaleController extends SliderPreferenceController {

    private static final String QS_SECONDARY_LABEL_SCALE = "qs_secondary_label_scale";
    private static final int DEFAULT_VALUE = 0;

    public QsSecondaryLabelScaleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public int getSliderPosition() {
        return Settings.System.getInt(mContext.getContentResolver(),
                QS_SECONDARY_LABEL_SCALE, DEFAULT_VALUE);
    }

    @Override
    public boolean setSliderPosition(int position) {
        return Settings.System.putInt(mContext.getContentResolver(),
                QS_SECONDARY_LABEL_SCALE, position);
    }

    @Override
    public int getMax() {
        return 50;
    }

    @Override
    public int getMin() {
        return -50;
    }
}
