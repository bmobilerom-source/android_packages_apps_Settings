/*
 * Copyright (C) 2023 the risingOS android project
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
import android.util.Log;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import androidx.preference.ListPreference;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for haptics
 */
public class HapticsPreferenceFragment extends DashboardFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "HapticsPreferenceFragment";

    private static final String BACK_GESTURE_HAPTIC_INTENSITY = "back_gesture_haptic_intensity";

    private static final String KEY_BACK_GESTURE_HAPTICS_INTENSITY = "back_gesture_haptics_intensity";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTICS_INTENSITY = "brightness_slider_haptics_intensity";
    private static final String KEY_EDGE_SCROLLING_HAPTICS_INTENSITY = "edge_scrolling_haptics_intensity";
    private static final String KEY_QS_HAPTICS_INTENSITY = "qs_haptics_intensity";
    private static final String KEY_QS_TILE_HAPTICS_INTENSITY = "qs_tile_haptics_intensity";
    private static final String KEY_VOLUME_SLIDER_HAPTICS_INTENSITY = "volume_slider_haptics_intensity";

    private ListPreference mBackIntensity;
    private ListPreference mBrightnessIntensity;
    private ListPreference mVolumeSliderIntensity;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.haptics_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new HapticsPreferenceFragmentController(context, "haptics_settings"));
        return controllers;
    }

    @Override
    public void onCreatePreferences(android.os.Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mBackIntensity = (ListPreference) findPreference(KEY_BACK_GESTURE_HAPTICS_INTENSITY);
        mBrightnessIntensity = (ListPreference) findPreference(KEY_BRIGHTNESS_SLIDER_HAPTICS_INTENSITY);
        mVolumeSliderIntensity = (ListPreference) findPreference(KEY_VOLUME_SLIDER_HAPTICS_INTENSITY);

        // Ensure preferences don't persist to SharedPreferences since we handle Settings manually
        mBackIntensity.setPersistent(false);
        mBrightnessIntensity.setPersistent(false);
        mVolumeSliderIntensity.setPersistent(false);

        updateSettings();
    }

    private void updateSettings() {
        int backIntensity = Settings.Secure.getInt(getContext().getContentResolver(),
                BACK_GESTURE_HAPTIC_INTENSITY, 1);
        mBackIntensity.setValue(String.valueOf(backIntensity));
        int brightnessIntensity = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.QS_BRIGHTNESS_SLIDER_HAPTIC, 0);
        mBrightnessIntensity.setValue(String.valueOf(brightnessIntensity));
        int volumeSliderIntensity = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.VOLUME_SLIDER_HAPTICS_INTENSITY, 1);
        mVolumeSliderIntensity.setValue(String.valueOf(volumeSliderIntensity));

        mBackIntensity.setOnPreferenceChangeListener(this);
        mBrightnessIntensity.setOnPreferenceChangeListener(this);
        mVolumeSliderIntensity.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int intensity = Integer.parseInt((String) newValue);
            boolean isChanged = false;
            if (preference == mBackIntensity) {
                Settings.Secure.putInt(getContext().getContentResolver(),
                        BACK_GESTURE_HAPTIC_INTENSITY, intensity);
                mBackIntensity.setValue(String.valueOf(intensity));
                isChanged = true;
            } else if (preference == mBrightnessIntensity) {
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.QS_BRIGHTNESS_SLIDER_HAPTIC, intensity);
                mBrightnessIntensity.setValue(String.valueOf(intensity));
                isChanged = true;
            } else if (preference == mVolumeSliderIntensity) {
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.VOLUME_SLIDER_HAPTICS_INTENSITY, intensity);
                mVolumeSliderIntensity.setValue(String.valueOf(intensity));
                isChanged = true;
            }
            if (isChanged) {
                triggerVibration(getContext(), intensity);
                return true;
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid preference value: " + newValue, e);
        }

        return false;
    }

    private static void triggerVibration(Context context, int intensity) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                // Create vibration effect based on intensity
                VibrationEffect effect = VibrationEffect.createOneShot(
                        Math.max(50, intensity * 50), VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }
}
