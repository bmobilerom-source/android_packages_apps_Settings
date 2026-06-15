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

package com.bmobile.customization;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Window / transition / animator duration scale for user-facing display settings
 * (not gated on developer options).
 */
public class AnimationScalePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private final int mAnimationScaleSelector;
    private final String[] mListValues;
    private final String[] mListSummaries;
    private final IWindowManager mWindowManager;

    public AnimationScalePreferenceController(Context context, String preferenceKey,
            int animationScaleSelector, int entriesResId, int valuesResId) {
        super(context, preferenceKey);
        mAnimationScaleSelector = animationScaleSelector;
        mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        mListValues = context.getResources().getStringArray(valuesResId);
        mListSummaries = context.getResources().getStringArray(entriesResId);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        updateAnimationScaleValue(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            float scale = Float.parseFloat(String.valueOf(newValue));
            mWindowManager.setAnimationScale(mAnimationScaleSelector, scale);
            updateAnimationScaleValue(preference);
            return true;
        } catch (RemoteException | NumberFormatException e) {
            return false;
        }
    }

    private void updateAnimationScaleValue(Preference preference) {
        if (!(preference instanceof ListPreference)) {
            return;
        }
        try {
            final float scale = mWindowManager.getAnimationScale(mAnimationScaleSelector);
            int index = 0;
            for (int i = 0; i < mListValues.length; i++) {
                float val = Float.parseFloat(mListValues[i]);
                if (scale <= val) {
                    index = i;
                    break;
                }
            }
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setValue(mListValues[index]);
            listPreference.setSummary(mListSummaries[index]);
        } catch (RemoteException e) {
            // intentional no-op
        }
    }
}
