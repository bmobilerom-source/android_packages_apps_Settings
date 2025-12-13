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

package com.epic.fragments;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;

public class StatusBarHeightFactorController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private SeekBarPreference mPreference;
    private static final String KEY_STATUS_BAR_HEIGHT_FACTOR = "status_bar_height_factor";

    public StatusBarHeightFactorController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            mPreference.setOnPreferenceChangeListener(this);
            int currentValue = getStatusBarHeightFactor();
            mPreference.setProgress(currentValue);
            updateSummary(currentValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = (Integer) newValue;
        boolean result = Settings.System.putInt(mContext.getContentResolver(),
                KEY_STATUS_BAR_HEIGHT_FACTOR, value);
        if (result && mPreference != null) {
            updateSummary(value);
        }
        return result;
    }

    private void updateSummary(int value) {
        if (mPreference != null) {
            String summary = mContext.getString(com.android.settings.R.string.status_bar_height_factor_summary);
            mPreference.setSummary(summary + " (" + value + "%)");
        }
    }

    private int getStatusBarHeightFactor() {
        return Settings.System.getInt(mContext.getContentResolver(),
                KEY_STATUS_BAR_HEIGHT_FACTOR, 100);
    }
}
