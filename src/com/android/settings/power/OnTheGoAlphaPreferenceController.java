/*
 * Copyright (C) 2025 The Android Open Source Project
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
import android.util.Log;

import com.android.settings.core.SliderPreferenceController;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Controller for OnTheGo transparency preference
 */
public class OnTheGoAlphaPreferenceController extends SliderPreferenceController {

    private static final String TAG = "OnTheGoAlphaController";
    private static final String KEY_ONTHEGO_ALPHA = "onthego_alpha";

    public OnTheGoAlphaPreferenceController(Context context) {
        super(context, KEY_ONTHEGO_ALPHA);
    }

    @Override
    public int getAvailabilityStatus() {
        return AbstractPreferenceController.AVAILABLE;
    }

    @Override
    public int getSliderPosition() {
        float alpha = Settings.System.getFloat(mContext.getContentResolver(),
                Settings.System.ON_THE_GO_ALPHA, 0.5f);
        return (int) (alpha * 100);
    }

    @Override
    public boolean setSliderPosition(int position) {
        float alpha = position / 100.0f;
        Settings.System.putFloat(mContext.getContentResolver(),
                Settings.System.ON_THE_GO_ALPHA, alpha);
        
        // Send broadcast to update the service
        sendAlphaBroadcast(alpha);
        return true;
    }

    @Override
    public int getMax() {
        return 100;
    }

    @Override
    public int getMin() {
        return 0;
    }

    private void sendAlphaBroadcast(float alpha) {
        try {
            Intent alphaBroadcast = new Intent();
            alphaBroadcast.setAction("com.android.systemui.epic.onthego.OnTheGoService.ACTION_TOGGLE_ALPHA");
            alphaBroadcast.putExtra("com.android.systemui.epic.onthego.OnTheGoService.EXTRA_ALPHA", alpha);
            mContext.sendBroadcast(alphaBroadcast);
            Log.d(TAG, "Sent alpha broadcast: " + alpha);
        } catch (Exception e) {
            Log.e(TAG, "Error sending alpha broadcast: " + e.getMessage());
        }
    }
}
