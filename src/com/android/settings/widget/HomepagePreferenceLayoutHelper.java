/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.settings.widget;

import android.content.Context;
import android.provider.Settings;
import android.os.UserHandle;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.RelativeLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.flags.Flags;

/** Helper for homepage preference to manage layout. */
public class HomepagePreferenceLayoutHelper {

    private View mIcon;
    private View mText;
    private TextView mTitle;
    private TextView mSummary;
    private boolean mIconVisible = true;
    private int mIconPaddingStart = -1;
    private int mTextPaddingStart = -1;

    /** The interface for managing preference layouts on homepage */
    public interface HomepagePreferenceLayout {
        /** Returns a {@link HomepagePreferenceLayoutHelper}  */
        HomepagePreferenceLayoutHelper getHelper();
    }

    public HomepagePreferenceLayoutHelper(Preference preference) {
        preference.setLayoutResource(
                Flags.homepageRevamp()
                        ? R.layout.homepage_preference_v2
                        : R.layout.homepage_preference);
    }

    /** Sets whether the icon should be visible */
    public void setIconVisible(boolean visible) {
        mIconVisible = visible;
        if (mIcon != null) {
            mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /** Sets the icon padding start */
    public void setIconPaddingStart(int paddingStart) {
        mIconPaddingStart = paddingStart;
        if (mIcon != null && paddingStart >= 0) {
            mIcon.setPaddingRelative(paddingStart, mIcon.getPaddingTop(), mIcon.getPaddingEnd(),
                    mIcon.getPaddingBottom());
        }
    }

    /** Sets the text padding start */
    public void setTextPaddingStart(int paddingStart) {
        mTextPaddingStart = paddingStart;
        if (mText != null && paddingStart >= 0) {
            mText.setPaddingRelative(paddingStart, mText.getPaddingTop(), mText.getPaddingEnd(),
                    mText.getPaddingBottom());
        }
    }

    void onBindViewHolder(PreferenceViewHolder holder) {
        mIcon = holder.findViewById(R.id.icon_frame);
        mText = holder.findViewById(R.id.text_frame);
        mTitle = (TextView) holder.findViewById(android.R.id.title);
        mSummary = (TextView) holder.findViewById(android.R.id.summary);
        
        setIconVisible(mIconVisible);
        setIconPaddingStart(mIconPaddingStart);
        setTextPaddingStart(mTextPaddingStart);
        
        // For epic style, ensure text is left-aligned (not centered)
        if (isEpicStyle()) {
            // Text should be left-aligned, not centered
            if (mText != null && mText instanceof RelativeLayout) {
                RelativeLayout textFrame = (RelativeLayout) mText;
                textFrame.setGravity(Gravity.START);
                
                if (mTitle != null) {
                    RelativeLayout.LayoutParams titleParams = 
                        (RelativeLayout.LayoutParams) mTitle.getLayoutParams();
                    if (titleParams != null) {
                        titleParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        titleParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                        mTitle.setLayoutParams(titleParams);
                        mTitle.setGravity(Gravity.START);
                        mTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    }
                }
                
                if (mSummary != null) {
                    RelativeLayout.LayoutParams summaryParams = 
                        (RelativeLayout.LayoutParams) mSummary.getLayoutParams();
                    if (summaryParams != null) {
                        summaryParams.addRule(RelativeLayout.ALIGN_START);
                        summaryParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                        mSummary.setLayoutParams(summaryParams);
                        mSummary.setGravity(Gravity.START);
                        mSummary.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if epic style dashboard is enabled.
     */
    private boolean isEpicStyle() {
        try {
            Context context = null;
            if (mText != null) {
                context = mText.getContext();
            }
            if (context == null) {
                return false;
            }
            
            int dashboardStyle = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    "settings_dashboard_style",
                    2, // Default to V2
                    UserHandle.USER_CURRENT);
            
            return dashboardStyle == 1; // Epic style = 1
        } catch (Exception e) {
            return false;
        }
    }
    
}
