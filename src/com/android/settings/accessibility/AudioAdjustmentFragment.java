/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.app.settings.SettingsEnums;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/** Accessibility settings for audio adjustment. */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AudioAdjustmentFragment extends DashboardFragment {

    private static final String TAG = "AudioAdjustmentFragment";
    private static final String KEY_BALANCE_SEEKBAR = "seekbar_primary_balance";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.ACCESSIBILITY_AUDIO_ADJUSTMENT;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.accessibility_audio_adjustment;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreatePreferences(android.os.Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        
        // Override the layout for BalanceSeekBarPreference to use adaptive card
        // This must be done after super.onCreatePreferences so the preference screen is loaded
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            BalanceSeekBarPreference balancePref = screen.findPreference(KEY_BALANCE_SEEKBAR);
            if (balancePref != null) {
                balancePref.setLayoutResource(R.layout.adaptive_balance_seekbar_card_progress);
            }
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.accessibility_audio_adjustment);

}
