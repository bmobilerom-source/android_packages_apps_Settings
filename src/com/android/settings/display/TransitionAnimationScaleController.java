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

public class TransitionAnimationScaleController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TRANSITION_ANIMATION_SCALE_KEY = "transition_animation_scale";
    private static final int TRANSITION_ANIMATION_SCALE_SELECTOR = 1;
    private static final float DEFAULT_VALUE = 1;

    private final IWindowManager mWindowManager;
    private final String[] mListValues;
    private final String[] mListSummaries;

    public TransitionAnimationScaleController(Context context, String key) {
        super(context, key);
        mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        mListValues = context.getResources().getStringArray(
                com.android.settingslib.R.array.transition_animation_scale_values);
        mListSummaries = context.getResources().getStringArray(
                com.android.settingslib.R.array.transition_animation_scale_entries);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return TRANSITION_ANIMATION_SCALE_KEY;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            try {
                final float scale = mWindowManager.getAnimationScale(TRANSITION_ANIMATION_SCALE_SELECTOR);
                int index = 0; // default
                for (int i = 0; i < mListValues.length; i++) {
                    float val = Float.parseFloat(mListValues[i]);
                    if (scale <= val) {
                        index = i;
                        break;
                    }
                }
                listPreference.setValue(mListValues[index]);
                listPreference.setSummary(mListSummaries[index]);
            } catch (RemoteException e) {
                // Ignore
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            float scale = Float.parseFloat((String) newValue);
            mWindowManager.setAnimationScale(TRANSITION_ANIMATION_SCALE_SELECTOR, scale);
            updateState(preference);
        } catch (RemoteException e) {
            // Ignore
        }
        return true;
    }
}