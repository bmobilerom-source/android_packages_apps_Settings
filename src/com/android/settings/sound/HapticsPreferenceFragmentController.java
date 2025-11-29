/*
 * Copyright (C) 2023 the risingOS android project
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
package com.android.settings.sound;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for haptics settings preference
 */
public class HapticsPreferenceFragmentController extends BasePreferenceController {

    private static final String TAG = "HapticsPrefController";

    public HapticsPreferenceFragmentController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(androidx.preference.Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            final Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.sound.HapticsPreferenceFragment");
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }
}
