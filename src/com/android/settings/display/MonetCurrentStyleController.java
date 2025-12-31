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

package com.android.settings.display;

import android.content.Context;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class MonetCurrentStyleController extends BasePreferenceController {

    public MonetCurrentStyleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            int currentStyle = android.provider.Settings.System.getInt(
                    mContext.getContentResolver(), "monet_color_style", 0);
            String styleName = getStyleName(currentStyle);
            preference.setSummary(styleName);
        }
    }

    private String getStyleName(int styleValue) {
        switch (styleValue) {
            case 0: return "Tonal Spot";
            case 1: return "Spritz";
            case 2: return "Vibrant";
            case 3: return "Expressive";
            case 4: return "Rainbow";
            case 5: return "Fruit Salad";
            default: return "Tonal Spot";
        }
    }
}





