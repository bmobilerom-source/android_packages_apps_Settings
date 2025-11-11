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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.widget.FooterPreference;

/**
 * Unified helper class for all adaptive preferences to ensure consistent styling,
 * positioning, and behavior across all preference types including:
 * - AdaptivePreference and variants
 * - FooterPreference
 * - RestrictedSwitchPreference
 * - SettingsLib preference switches
 */
public class AdaptivePreferenceHelper {

    /**
     * Checks if a layout resource is an adaptive preference card or compatible layout.
     * This includes all adaptive preference cards, system cards, footer, and restricted switch layouts.
     */
    public static boolean isAdaptiveOrCompatibleLayout(int layoutRes) {
        return layoutRes == R.layout.adaptive_preference_card ||
               layoutRes == R.layout.adaptive_preference_card_top ||
               layoutRes == R.layout.adaptive_preference_card_middle ||
               layoutRes == R.layout.adaptive_preference_card_bottom ||
               layoutRes == R.layout.adaptive_preference_card_top_switch ||
               layoutRes == R.layout.adaptive_preference_card_middle_switch ||
               layoutRes == R.layout.adaptive_preference_card_bottom_switch ||
               layoutRes == R.layout.system_warning_preference_card ||
               layoutRes == R.layout.system_features_preference_card ||
               layoutRes == R.layout.system_advice_preference_card ||
               // Footer preference layout (SettingsLib)
               isFooterLayout(layoutRes) ||
               // Restricted switch preference layout (SettingsLib)
               layoutRes == com.android.settingslib.R.layout.restricted_switch_preference;
    }

    /**
     * Checks if a preference instance is an adaptive or compatible preference type.
     */
    public static boolean isAdaptiveOrCompatiblePreference(Preference preference) {
        if (preference == null) {
            return false;
        }
        
        // Check by class type
        if (preference instanceof AdaptivePreference ||
            preference instanceof AdaptiveSwitchPreference ||
            preference instanceof AdaptiveRestrictedSwitchPreference ||
            preference instanceof AdaptiveRestrictedPreference ||
            preference instanceof AdaptivePrimarySwitchPreference ||
            preference instanceof AdaptiveListPreference ||
            preference instanceof AdaptiveDropDownPreference ||
            preference instanceof AdaptiveRingtonePreference ||
            preference instanceof AdaptiveVolumeSeekBarPreference) {
            return true;
        }
        
        // Check FooterPreference
        if (preference instanceof FooterPreference) {
            return true;
        }
        
        // Check RestrictedSwitchPreference (SettingsLib)
        if (preference instanceof RestrictedSwitchPreference) {
            return true;
        }
        
        // Check RestrictedPreference (SettingsLib)
        if (preference instanceof RestrictedPreference) {
            return true;
        }
        
        // Check by layout resource
        int layoutRes = preference.getLayoutResource();
        return isAdaptiveOrCompatibleLayout(layoutRes);
    }

    /**
     * Hides icon and icon frame for consistent positioning across all preferences.
     */
    public static void hideIcons(PreferenceViewHolder holder) {
        if (holder == null) {
            return;
        }
        
        View iconFrame = holder.findViewById(android.R.id.icon_frame);
        if (iconFrame != null) {
            iconFrame.setVisibility(View.GONE);
        }
        
        View icon = holder.findViewById(android.R.id.icon);
        if (icon != null) {
            icon.setVisibility(View.GONE);
        }
    }

    /**
     * Sets summary text size to be 10% smaller than title text size.
     */
    public static void adjustTextSizes(PreferenceViewHolder holder, Context context) {
        if (holder == null || context == null) {
            return;
        }
        
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        
        if (titleView != null && summaryView != null) {
            // Get title text size
            float titleSize = titleView.getTextSize();
            
            // Calculate summary size (10% smaller)
            float summarySize = titleSize * 0.9f;
            
            // Apply summary size
            summaryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, summarySize);
            
            // Ensure same text color attributes for consistency
            TypedArray a = context.obtainStyledAttributes(new int[]{
                android.R.attr.textColorPrimary,
                android.R.attr.textColorSecondary
            });
            
            if (titleView.getCurrentTextColor() == 0) {
                titleView.setTextColor(a.getColor(0, 0xFF000000));
            }
            
            if (summaryView.getCurrentTextColor() == 0) {
                summaryView.setTextColor(a.getColor(1, 0xFF808080));
            }
            
            a.recycle();
        }
    }

    /**
     * Applies unified styling to all preferences including hiding icons and adjusting text sizes.
     */
    public static void applyUnifiedStyling(PreferenceViewHolder holder, Context context) {
        hideIcons(holder);
        adjustTextSizes(holder, context);
    }

    /**
     * Finds the next visible adaptive or compatible preference in the parent group.
     */
    public static Preference findNextCompatiblePreference(PreferenceGroup parent, int startIndex) {
        if (parent == null || startIndex < 0) {
            return null;
        }
        
        int count = parent.getPreferenceCount();
        for (int i = startIndex + 1; i < count; i++) {
            Preference pref = parent.getPreference(i);
            if (pref != null && pref.isVisible() && isAdaptiveOrCompatiblePreference(pref)) {
                return pref;
            }
        }
        return null;
    }

    /**
     * Finds the previous visible adaptive or compatible preference in the parent group.
     */
    public static Preference findPreviousCompatiblePreference(PreferenceGroup parent, int startIndex) {
        if (parent == null || startIndex < 0) {
            return null;
        }
        
        for (int i = startIndex - 1; i >= 0; i--) {
            Preference pref = parent.getPreference(i);
            if (pref != null && pref.isVisible() && isAdaptiveOrCompatiblePreference(pref)) {
                return pref;
            }
        }
        return null;
    }

    /**
     * Gets the index of a preference in its parent group.
     */
    public static int getPreferenceIndex(PreferenceGroup parent, Preference preference) {
        if (parent == null || preference == null) {
            return -1;
        }
        
        int count = parent.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            if (parent.getPreference(i) == preference) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if there's a compatible preference before this one.
     */
    public static boolean hasCompatiblePreferenceBefore(PreferenceGroup parent, int index) {
        return findPreviousCompatiblePreference(parent, index) != null;
    }

    /**
     * Checks if there's a compatible preference after this one.
     */
    public static boolean hasCompatiblePreferenceAfter(PreferenceGroup parent, int index) {
        return findNextCompatiblePreference(parent, index) != null;
    }

    /**
     * Checks if a preference has a top layout (rounded top corners).
     */
    public static boolean hasTopLayout(int layoutRes) {
        return layoutRes == R.layout.adaptive_preference_card_top ||
               layoutRes == R.layout.adaptive_preference_card_top_switch ||
               layoutRes == R.layout.adaptive_preference_card ||
               layoutRes == R.layout.system_warning_preference_card ||
               layoutRes == R.layout.system_features_preference_card ||
               layoutRes == R.layout.system_advice_preference_card ||
               isFooterLayout(layoutRes) ||
               layoutRes == com.android.settingslib.R.layout.restricted_switch_preference;
    }

    /**
     * Checks if a layout resource is a footer preference layout.
     */
    private static boolean isFooterLayout(int layoutRes) {
        if (layoutRes == 0) {
            return false;
        }
        try {
            return layoutRes == com.android.settingslib.widget.preference.footer.R.layout.preference_footer;
        } catch (Exception e) {
            // Resource might not be available, return false
            return false;
        }
    }
}

