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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for volume slider haptics intensity.
 * Controls the haptics intensity for the system volume dialog.
 */
public class VolumeSliderHapticsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String VOLUME_SLIDER_HAPTICS_INTENSITY = "volume_slider_haptics_intensity";
    private Vibrator mVibrator;

    public VolumeSliderHapticsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int getAvailabilityStatus() {
        return mVibrator != null && mVibrator.hasVibrator() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            // Get current intensity value
            int currentIntensity = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.VOLUME_SLIDER_HAPTICS_INTENSITY, 1);

            // Set the current value
            listPreference.setValue(String.valueOf(currentIntensity));

            // Set up change listener
            listPreference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int intensity = Integer.parseInt((String) newValue);

            // Save the new intensity
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.VOLUME_SLIDER_HAPTICS_INTENSITY, intensity);

            // Provide haptic feedback to demonstrate the intensity
            triggerHapticFeedback(intensity);

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void triggerHapticFeedback(int intensity) {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }

        try {
            VibrationEffect effect;
            switch (intensity) {
                case 0:
                    // No vibration for "Off"
                    return;
                case 1:
                    // Light - use subtle tick
                    effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TEXTURE_TICK);
                    break;
                case 2:
                    // Medium - use tick
                    effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                    break;
                case 3:
                    // Strong - use click
                    effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
                    break;
                default:
                    effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                    break;
            }

            mVibrator.vibrate(effect);
        } catch (Exception e) {
            // Fallback to simple vibration
            try {
                mVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } catch (Exception e2) {
                // Ignore vibration errors
            }
        }
    }
}
