/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.bmobile.customization;

import android.content.Context;

import com.android.settings.core.TogglePreferenceController;
import com.bmobile.fragments.StatusBarLogoHelper;

public class StatusBarLogoController extends TogglePreferenceController {

    public StatusBarLogoController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return StatusBarLogoHelper.isLogoFeatureAvailable(mContext) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return StatusBarLogoHelper.isStatusBarLogoEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return StatusBarLogoHelper.setStatusBarLogoEnabled(mContext, isChecked);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
