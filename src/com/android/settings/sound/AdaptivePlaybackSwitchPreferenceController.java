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
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

/**
 * Controller for Adaptive Playback (Smart Pause) master switch
 */
public class AdaptivePlaybackSwitchPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnResume {

    private static final String TAG = "AdaptivePlaybackSwitch";

    public AdaptivePlaybackSwitchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        boolean enabled = Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED,
                0, UserHandle.USER_CURRENT) != 0;

        if (preference != null) {
            preference.setSummary(enabled ?
                    R.string.adaptive_playback_enabled :
                    R.string.adaptive_playback_disabled_summary);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            Intent intent = new Intent(mContext, AdaptivePlaybackSoundSettings.class);
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        updateState(mPreference);
    }
}
