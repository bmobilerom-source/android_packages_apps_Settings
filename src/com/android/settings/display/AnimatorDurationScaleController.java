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

package com.android.settings.display;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;

/**
 * Standalone controller for Animator Duration Scale
 * Works independently of Developer Options
 */
public class AnimatorDurationScaleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final int ANIMATOR_DURATION_SCALE_SELECTOR = 2;
    private static final float DEFAULT_VALUE = 1.0f;

    private final IWindowManager mWindowManager;
    private final String[] mListValues;
    private final String[] mListSummaries;

    public AnimatorDurationScaleController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        mListValues = context.getResources()
                .getStringArray(com.android.settingslib.R.array.animator_duration_scale_values);
        mListSummaries = context.getResources().getStringArray(
                com.android.settingslib.R.array.animator_duration_scale_entries);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        updateAnimationScaleValue(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeAnimationScaleOption(newValue);
        updateAnimationScaleValue(preference);
        return true;
    }

    private void writeAnimationScaleOption(Object newValue) {
        try {
            if (mWindowManager != null) {
                float scale = newValue != null ? Float.parseFloat(newValue.toString()) : DEFAULT_VALUE;
                mWindowManager.setAnimationScale(ANIMATOR_DURATION_SCALE_SELECTOR, scale);
            }
        } catch (RemoteException e) {
            // intentional no-op
        }
    }

    private void updateAnimationScaleValue(Preference preference) {
        if (!(preference instanceof ListPreference) || mWindowManager == null) {
            return;
        }
        try {
            final float scale = mWindowManager.getAnimationScale(ANIMATOR_DURATION_SCALE_SELECTOR);
            int index = 0; // default
            for (int i = 0; i < mListValues.length; i++) {
                float val = Float.parseFloat(mListValues[i]);
                if (scale <= val) {
                    index = i;
                    break;
                }
            }
            final ListPreference listPreference = (ListPreference) preference;
            listPreference.setValue(mListValues[index]);
            listPreference.setSummary(mListSummaries[index]);
        } catch (RemoteException e) {
            // intentional no-op
        }
    }
}

