/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.power;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.SeekBar;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;

/**
 * Controller for OnTheGo transparency (alpha) control
 */
public class OnTheGoAlphaPreferenceController extends BasePreferenceController
        implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "OnTheGoAlphaPC";

    public OnTheGoAlphaPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(androidx.preference.Preference preference) {
        super.updateState(preference);
        if (preference instanceof SeekBarPreference) {
            SeekBarPreference seekBarPreference = (SeekBarPreference) preference;

            // Get current alpha value (default to 50%)
            float currentAlpha = Settings.System.getFloat(
                    mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_ALPHA,
                    0.5f);

            // Convert to percentage (0-75 range as per documentation)
            int progress = Math.round(currentAlpha * 100);
            progress = Math.max(10, Math.min(75, progress)); // Clamp to 10-75 range

            seekBarPreference.setProgress(progress);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // Convert percentage to alpha value (0.0-1.0)
            float alpha = progress / 100.0f;

            // Save to settings
            Settings.System.putFloat(mContext.getContentResolver(),
                    Settings.System.ON_THE_GO_ALPHA, alpha);

            // Send broadcast to update the service in real-time
            sendAlphaBroadcast(alpha);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // No additional action needed
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // No additional action needed
    }

    private void sendAlphaBroadcast(float alpha) {
        try {
            Intent alphaBroadcast = new Intent();
            alphaBroadcast.setAction("com.android.systemui.epic.onthego.OnTheGoService.ACTION_TOGGLE_ALPHA");
            alphaBroadcast.putExtra("com.android.systemui.epic.onthego.OnTheGoService.EXTRA_ALPHA", alpha);
            mContext.sendBroadcast(alphaBroadcast);
        } catch (Exception e) {
            // Ignore broadcast failures
        }
    }
}
