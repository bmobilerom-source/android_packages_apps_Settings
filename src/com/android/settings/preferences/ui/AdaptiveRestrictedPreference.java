/*
 * Copyright (C) 2023 The risingOS Android Project
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
import android.os.Process;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceGroup;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.view.View;

import com.android.settings.R;
import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

import com.android.settingslib.RestrictedPreference;

public class AdaptiveRestrictedPreference extends RestrictedPreference {

    public AdaptiveRestrictedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, attrs));
    }
    
    @Override
    public void onAttached() {
        super.onAttached();
        // Check if this is a middle preference that's first (no top above) and update layout
        updateLayoutForFirstMiddlePreference();
        // Check if this is a solo preference that needs bottom rounded corners
        updateLayoutForSoloPreference();
        // Check if this preference should merge with adjacent preferences
        updateLayoutForAdjacentMerging();
    }
    
    /**
     * Updates layout for middle preferences that are first (no top above).
     */
    private void updateLayoutForFirstMiddlePreference() {
        try {
            PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            int currentLayout = getLayoutResource();
            if (currentLayout != R.layout.adaptive_preference_card_middle) {
                return;
            }
            
            int thisIndex = AdaptivePreferenceHelper.getPreferenceIndex(parent, this);
            
            if (thisIndex < 0) {
                return;
            }
            
            // Check if there's a visible top preference or compatible preference before this one
            boolean hasTopBefore = false;
            Preference prevPref = AdaptivePreferenceHelper.findPreviousCompatiblePreference(parent, thisIndex);
            if (prevPref != null) {
                int prevLayout = prevPref.getLayoutResource();
                hasTopBefore = AdaptivePreferenceHelper.hasTopLayout(prevLayout);
            }
            
            if (!hasTopBefore) {
                setLayoutResource(R.layout.adaptive_preference_card_top);
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }
    
    /**
     * Updates layout for solo preferences.
     */
    private void updateLayoutForSoloPreference() {
        try {
            PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            int currentLayout = getLayoutResource();
            if (currentLayout != R.layout.adaptive_preference_card) {
                return;
            }
            
            int thisIndex = AdaptivePreferenceHelper.getPreferenceIndex(parent, this);
            
            if (thisIndex < 0) {
                return;
            }
            
            // Check if there's a visible compatible preference before this one
            boolean hasAdaptiveBefore = AdaptivePreferenceHelper.hasCompatiblePreferenceBefore(parent, thisIndex);
            
            // Check if there's a visible compatible preference after this one
            boolean hasAdaptiveAfter = AdaptivePreferenceHelper.hasCompatiblePreferenceAfter(parent, thisIndex);
            
            if (hasAdaptiveAfter) {
                setLayoutResource(R.layout.adaptive_preference_card_top);
            } else if (hasAdaptiveBefore) {
                setLayoutResource(R.layout.adaptive_preference_card_bottom);
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }
    
    /**
     * Updates layout to ensure preferences merge properly when adjacent.
     */
    private void updateLayoutForAdjacentMerging() {
        try {
            PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            int currentLayout = getLayoutResource();
            int thisIndex = AdaptivePreferenceHelper.getPreferenceIndex(parent, this);
            
            if (thisIndex < 0) {
                return; // Not found
            }
            
            // Find adjacent compatible preferences
            Preference prevPref = AdaptivePreferenceHelper.findPreviousCompatiblePreference(parent, thisIndex);
            Preference nextPref = AdaptivePreferenceHelper.findNextCompatiblePreference(parent, thisIndex);
            
            boolean hasCompatibleBefore = prevPref != null;
            boolean hasCompatibleAfter = nextPref != null;
            
            // Determine correct layout based on adjacent preferences
            if (hasCompatibleBefore && hasCompatibleAfter) {
                // Middle preference - has both before and after
                if (currentLayout == R.layout.adaptive_preference_card_top ||
                    currentLayout == R.layout.adaptive_preference_card_bottom ||
                    currentLayout == R.layout.adaptive_preference_card) {
                    setLayoutResource(R.layout.adaptive_preference_card_middle);
                }
            } else if (hasCompatibleBefore && !hasCompatibleAfter) {
                // Bottom preference - has before but no after
                if (currentLayout == R.layout.adaptive_preference_card_top ||
                    currentLayout == R.layout.adaptive_preference_card_middle ||
                    currentLayout == R.layout.adaptive_preference_card) {
                    setLayoutResource(R.layout.adaptive_preference_card_bottom);
                }
            } else if (!hasCompatibleBefore && hasCompatibleAfter) {
                // Top preference - has after but no before
                if (currentLayout == R.layout.adaptive_preference_card_middle ||
                    currentLayout == R.layout.adaptive_preference_card_bottom ||
                    currentLayout == R.layout.adaptive_preference_card) {
                    setLayoutResource(R.layout.adaptive_preference_card_top);
                }
            } else {
                // Solo preference - no adjacent compatible preferences
                if (currentLayout != R.layout.adaptive_preference_card) {
                    setLayoutResource(R.layout.adaptive_preference_card);
                }
            }
        } catch (Exception e) {
            // Ignore errors - fallback to original layout
        }
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // Hide icon and icon frame to ensure all preferences start at the same place
        View iconFrame = holder.findViewById(android.R.id.icon_frame);
        if (iconFrame != null) {
            iconFrame.setVisibility(View.GONE);
        }
        View icon = holder.findViewById(android.R.id.icon);
        if (icon != null) {
            icon.setVisibility(View.GONE);
        }
    }
}
