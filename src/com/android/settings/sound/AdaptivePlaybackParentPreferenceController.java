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

package com.android.settings.sound;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

/**
 * Parent controller for Adaptive Playback in main Sound Settings
 */
public class AdaptivePlaybackParentPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnResume {

    private static final String TAG = "AdaptivePlaybackParent";

    public AdaptivePlaybackParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference == null) return;

        boolean enabled = Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED,
                0, UserHandle.USER_CURRENT) != 0;

        preference.setSummary(enabled ?
                R.string.adaptive_playback_enabled :
                R.string.adaptive_playback_disabled_summary);
    }

    @Override
    public void onResume() {
        updateState(mPreference);
    }
}
