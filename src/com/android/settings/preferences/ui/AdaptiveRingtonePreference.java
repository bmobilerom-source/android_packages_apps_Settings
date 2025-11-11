/*
 * Copyright (C) 2025 BashaMobile
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
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.android.settings.DefaultRingtonePreference;
import com.android.settings.R;

/**
 * Adaptive ringtone preference that applies adaptive UI styling
 */
public class AdaptiveRingtonePreference extends DefaultRingtonePreference {

    public AdaptiveRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, attrs));
    }
    
    @Override
    public void onAttached() {
        super.onAttached();
        // Check if this is a middle preference that's first (no top above) and update layout
        updateLayoutForFirstMiddlePreference();
    }
    
    /**
     * Updates layout for middle preferences that are first (no top above).
     * Middle preferences that don't have a top preference above should have rounded corners.
     */
    private void updateLayoutForFirstMiddlePreference() {
        try {
            androidx.preference.PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            // Check if this preference uses middle layout
            int currentLayout = getLayoutResource();
            if (currentLayout != R.layout.adaptive_preference_card_middle) {
                return; // Not a middle preference
            }
            
            // Find this preference's index in parent
            int preferenceCount = parent.getPreferenceCount();
            int thisIndex = -1;
            for (int i = 0; i < preferenceCount; i++) {
                if (parent.getPreference(i) == this) {
                    thisIndex = i;
                    break;
                }
            }
            
            if (thisIndex < 0) {
                return; // Not found
            }
            
            // Check if there's a visible top preference before this one
            boolean hasTopBefore = false;
            for (int i = 0; i < thisIndex; i++) {
                androidx.preference.Preference pref = parent.getPreference(i);
                if (pref != null && pref.isVisible()) {
                    // Check if previous preference uses top layout or solo layout (both have rounded top)
                    int prevLayout = pref.getLayoutResource();
                    if (prevLayout == R.layout.adaptive_preference_card_top || 
                        prevLayout == R.layout.adaptive_preference_card) {
                        hasTopBefore = true;
                        break;
                    }
                }
            }
            
            // If no top before and this is middle, use top layout for rounded corners
            if (!hasTopBefore) {
                setLayoutResource(R.layout.adaptive_preference_card_top);
            }
        } catch (Exception e) {
            // Ignore errors - fallback to original layout
        }
    }
}

