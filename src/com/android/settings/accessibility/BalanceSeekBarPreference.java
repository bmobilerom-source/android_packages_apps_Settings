/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.widget.SeekBarPreference;

/** A slider preference that directly controls audio balance **/
public class BalanceSeekBarPreference extends SeekBarPreference {
    private static final int BALANCE_CENTER_VALUE = 100;
    private static final int BALANCE_MAX_VALUE = 200;

    private final Context mContext;
    private BalanceSeekBar mSeekBar;
    private ImageView mIconView;

    public BalanceSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context,
                com.android.settingslib.R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
        mContext = context;
        
        // Check if layout is specified in XML attributes (android:layout)
        TypedArray a = context.obtainStyledAttributes(attrs,
                new int[]{android.R.attr.layout});
        int xmlLayoutResId = a.getResourceId(0, 0);
        a.recycle();
        
        // Check what layout the parent class set (it may have read from XML or used default)
        int currentLayout = getLayoutResource();
        int parentDefaultLayout = com.android.internal.R.layout.preference_widget_seekbar;
        
        // Only set our default layout if:
        // 1. No layout was specified in XML (xmlLayoutResId == 0), AND
        // 2. The parent class is using its default layout (meaning XML didn't override it)
        if (xmlLayoutResId == 0 && currentLayout == parentDefaultLayout) {
            setLayoutResource(R.layout.preference_balance_slider);
        }
        // If XML specified a layout, it will already be set by parent class, so don't override
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mSeekBar = (BalanceSeekBar) view.findViewById(com.android.internal.R.id.seekbar);
        mIconView = (ImageView) view.findViewById(com.android.internal.R.id.icon);
        if (mSeekBar != null) {
            init();
        }
    }

    private void init() {
        if (mSeekBar == null) {
            return;
        }
        final float balance = Settings.System.getFloatForUser(
                mContext.getContentResolver(), Settings.System.MASTER_BALANCE,
                0.f /* default */, UserHandle.USER_CURRENT);
        // Rescale balance to range 0-BALANCE_MAX_VALUE centered at BALANCE_MAX_VALUE / 2.
        mSeekBar.setMax(BALANCE_MAX_VALUE);
        mSeekBar.setProgress((int) (balance * 100.f) + BALANCE_CENTER_VALUE);
        mSeekBar.setEnabled(isEnabled());
    }
}
