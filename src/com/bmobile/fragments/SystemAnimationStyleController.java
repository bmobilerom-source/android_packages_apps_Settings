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

package com.bmobile.fragments;

import android.content.Context;

import com.bmobile.customization.AbstractIntListPreferenceController;

public class SystemAnimationStyleController extends AbstractIntListPreferenceController {

    private static final String SYSTEM_ANIMATION_STYLE_KEY = "system_animation_style";

    public SystemAnimationStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    protected int getDefaultValue() {
        return 0;
    }

    @Override
    protected String getSettingKey() {
        return SYSTEM_ANIMATION_STYLE_KEY;
    }

    @Override
    protected boolean isSecure() {
        return false;
    }
}
