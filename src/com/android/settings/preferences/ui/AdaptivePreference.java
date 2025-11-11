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
import android.content.res.TypedArray;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;

import com.android.settings.network.SubscriptionUtil;

public class AdaptivePreference extends Preference {

    public AdaptivePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if ("device_model".equals(getKey()) && !SubscriptionUtil.isSimHardwareVisible(context)) {
            setLayoutResource(R.layout.adaptive_preference_card_top);
            return;
        }
        
        setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, attrs));
    }

    public AdaptivePreference(Context context) {
        super(context);
        // Default to solo position for programmatically created preferences
        setLayoutResource(R.layout.adaptive_preference_card);
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
     * Middle preferences that don't have a top preference above should have rounded corners.
     */
    private void updateLayoutForFirstMiddlePreference() {
        try {
            PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            // Check if this preference uses middle layout
            int currentLayout = getLayoutResource();
            if (currentLayout != R.layout.adaptive_preference_card_middle) {
                return; // Not a middle preference
            }
            
            // Find this preference's index in parent
            int thisIndex = AdaptivePreferenceHelper.getPreferenceIndex(parent, this);
            
            if (thisIndex < 0) {
                return; // Not found
            }
            
            // Check if there's a visible top preference or compatible preference before this one
            boolean hasTopBefore = false;
            Preference prevPref = AdaptivePreferenceHelper.findPreviousCompatiblePreference(parent, thisIndex);
            if (prevPref != null) {
                int prevLayout = prevPref.getLayoutResource();
                hasTopBefore = AdaptivePreferenceHelper.hasTopLayout(prevLayout);
            }
            
            // If no top before and this is middle, use top layout for rounded corners
            if (!hasTopBefore) {
                setLayoutResource(R.layout.adaptive_preference_card_top);
            }
        } catch (Exception e) {
            // Ignore errors - fallback to original layout
        }
    }
    
    /**
     * Updates layout for solo preferences.
     * If a solo preference is truly alone (no adaptive preferences before or after),
     * it should have rounded top AND bottom corners.
     * If there's an adaptive preference after, it should become top to merge.
     * If there's an adaptive preference before, it should become bottom to merge.
     */
    private void updateLayoutForSoloPreference() {
        try {
            PreferenceGroup parent = getParent();
            if (parent == null) {
                return;
            }
            
            int currentLayout = getLayoutResource();
            if (currentLayout != R.layout.adaptive_preference_card) {
                return; // Not a solo preference
            }
            
            // Find this preference's index in parent
            int thisIndex = AdaptivePreferenceHelper.getPreferenceIndex(parent, this);
            
            if (thisIndex < 0) {
                return; // Not found
            }
            
            // Check if there's a visible compatible preference before this one
            boolean hasAdaptiveBefore = AdaptivePreferenceHelper.hasCompatiblePreferenceBefore(parent, thisIndex);
            
            // Check if there's a visible compatible preference after this one
            boolean hasAdaptiveAfter = AdaptivePreferenceHelper.hasCompatiblePreferenceAfter(parent, thisIndex);
            
            // If truly alone (no adaptive before or after), keep as solo (has all rounded corners)
            // If there's an adaptive after, become top to merge
            // If there's an adaptive before but no after, become bottom to merge
            if (hasAdaptiveAfter) {
                setLayoutResource(R.layout.adaptive_preference_card_top);
            } else if (hasAdaptiveBefore) {
                setLayoutResource(R.layout.adaptive_preference_card_bottom);
            }
            // Otherwise, keep as solo (already has all rounded corners - top and bottom)
        } catch (Exception e) {
            // Ignore errors - fallback to original layout
        }
    }
    
    /**
     * Updates layout to ensure preferences merge properly when adjacent.
     * Top preferences should merge with middle/bottom preferences below them.
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
    
    /**
     * Checks if a layout resource is an adaptive preference card or compatible layout.
     * Uses unified helper to check all compatible preference types.
     */
    private boolean isAdaptiveOrCardLayout(int layoutRes) {
        return AdaptivePreferenceHelper.isAdaptiveOrCompatibleLayout(layoutRes);
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // Apply unified styling: hide icons and adjust text sizes
        AdaptivePreferenceHelper.applyUnifiedStyling(holder, getContext());
    }
}

