/*
 * Copyright (C) 2024 LineageOS
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
package com.android.settings.preferences.ui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.PreferenceViewHolder;
import com.android.settingslib.widget.FooterPreference;

/**
 * Wrapper for FooterPreference that applies unified adaptive styling.
 * This ensures FooterPreference works seamlessly with other adaptive preferences.
 */
public class AdaptiveFooterPreference extends FooterPreference {

    public AdaptiveFooterPreference(Context context) {
        super(context);
    }

    public AdaptiveFooterPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // Apply unified styling: hide icons and adjust text sizes
        AdaptivePreferenceHelper.applyUnifiedStyling(holder, getContext());
    }
}


