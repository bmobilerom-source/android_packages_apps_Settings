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
 * Controller for Adaptive Playback preference in Sound Settings
 */
public class AdaptivePlaybackSoundPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnResume {

    private static final String TAG = "AdaptivePlaybackSound";

    public AdaptivePlaybackSoundPreferenceController(Context context, String preferenceKey) {
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

        int timeout = Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT,
                30, UserHandle.USER_CURRENT);

        String summary;
        if (enabled) {
            switch (timeout) {
                case 0:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_none_summary);
                    break;
                case 30:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_30_secs_summary);
                    break;
                case 60:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_1_min_summary);
                    break;
                case 120:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_2_min_summary);
                    break;
                case 300:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_5_min_summary);
                    break;
                case 600:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_10_min_summary);
                    break;
                default:
                    summary = mContext.getString(R.string.adaptive_playback_timeout_30_secs_summary);
                    break;
            }
        } else {
            summary = mContext.getString(R.string.adaptive_playback_disabled_summary);
        }

        preference.setSummary(summary);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            // Launch the detailed settings
            return false; // Let the system handle the intent
        }
        return false;
    }

    @Override
    public void onResume() {
        updateState(mPreference);
    }
}
