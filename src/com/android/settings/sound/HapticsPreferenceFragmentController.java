/*
 * Copyright (C) 2023 the risingOS android project
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
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settingslib.core.AbstractPreferenceController;

import com.android.settings.widget.SeekBarPreference;

import com.android.internal.util.android.VibrationUtils;

public class HapticsPreferenceFragmentController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY = "haptics_settings";
    private static final String KEY_BACK_GESTURE_HAPTIC_INTENSITY = "back_gesture_haptic_intensity";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTICS_INTENSITY = "qs_brightness_slider_haptic";
    private static final String KEY_EDGE_SCROLLING_HAPTICS_INTENSITY = "edge_scrolling_haptics_intensity";
    private static final String KEY_QS_HAPTICS_INTENSITY = "qs_haptics_intensity";
    private static final String KEY_QS_TILE_HAPTICS_INTENSITY = "qs_panel_tile_haptic";
    private static final String KEY_VOLUME_SLIDER_HAPTICS_INTENSITY = "volume_slider_haptics_intensity";

    private SeekBarPreference mBackIntensity;
    private SeekBarPreference mBrightnessIntensity;
    private SeekBarPreference mEdgeScrollingIntensity;
    private SeekBarPreference mQsIntensity;
    private SeekBarPreference mQsTileIntensity;
    private SeekBarPreference mVolumeSliderIntensity;
    
    private Context mContext;
    private Vibrator mVibrator;

    public HapticsPreferenceFragmentController(Context context) {
        super(context);
        mContext = context;
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean isAvailable() {
        return mVibrator != null && mVibrator.hasVibrator();
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }
    
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mBackIntensity = (SeekBarPreference) screen.findPreference(KEY_BACK_GESTURE_HAPTIC_INTENSITY);
        mBrightnessIntensity = (SeekBarPreference) screen.findPreference(KEY_BRIGHTNESS_SLIDER_HAPTICS_INTENSITY);
        mEdgeScrollingIntensity = (SeekBarPreference) screen.findPreference(KEY_EDGE_SCROLLING_HAPTICS_INTENSITY);
        mQsIntensity = (SeekBarPreference) screen.findPreference(KEY_QS_HAPTICS_INTENSITY);
        mQsTileIntensity = (SeekBarPreference) screen.findPreference(KEY_QS_TILE_HAPTICS_INTENSITY);
        mVolumeSliderIntensity = (SeekBarPreference) screen.findPreference(KEY_VOLUME_SLIDER_HAPTICS_INTENSITY);
        updateSettings();
    }

   private void updateSettings() {
        // Use available haptic settings or defaults
        int backIntensity = Settings.System.getInt(mContext.getContentResolver(),
                "back_gesture_haptic_intensity", 1);
        mBackIntensity.setProgress(backIntensity);

        int brightnessIntensity = Settings.System.getInt(mContext.getContentResolver(),
                "qs_brightness_slider_haptic", 0);
        mBrightnessIntensity.setProgress(brightnessIntensity);

        int edgeScrollingIntensity = Settings.System.getInt(mContext.getContentResolver(),
                "edge_scrolling_haptics_intensity", 1);
        mEdgeScrollingIntensity.setProgress(edgeScrollingIntensity);

        int volumeSliderIntensity = Settings.System.getInt(mContext.getContentResolver(),
                "volume_slider_haptics_intensity", 1);
        mVolumeSliderIntensity.setProgress(volumeSliderIntensity);

        int qsTileHapticsIntensity = Settings.System.getInt(mContext.getContentResolver(),
                "qs_panel_tile_haptic", 0);
        mQsTileIntensity.setProgress(qsTileHapticsIntensity);

        int qsHapticsIntensity = Settings.System.getInt(mContext.getContentResolver(),
               "qs_haptics_intensity", 1);
        mQsIntensity.setProgress(qsHapticsIntensity);

        mBackIntensity.setOnPreferenceChangeListener(this);
        mBrightnessIntensity.setOnPreferenceChangeListener(this);
        mEdgeScrollingIntensity.setOnPreferenceChangeListener(this);
        mQsIntensity.setOnPreferenceChangeListener(this);
        mQsTileIntensity.setOnPreferenceChangeListener(this);
        mVolumeSliderIntensity.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int intensity = (Integer) newValue;
        boolean isChanged = false;
        if (preference == mBackIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "back_gesture_haptic_intensity", intensity);
            mBackIntensity.setProgress(intensity);
            isChanged = true;
        } else if (preference == mBrightnessIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "qs_brightness_slider_haptic", intensity);
            mBrightnessIntensity.setProgress(intensity);
            isChanged = true;
        } else if (preference == mEdgeScrollingIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "edge_scrolling_haptics_intensity", intensity);
            mEdgeScrollingIntensity.setProgress(intensity);
            isChanged = true;
        } else if (preference == mQsIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "qs_haptics_intensity", intensity);
            mQsIntensity.setProgress(intensity);
            isChanged = true;
        } else if (preference == mQsTileIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "qs_panel_tile_haptic", intensity);
            mQsTileIntensity.setProgress(intensity);
            isChanged = true;
        } else if (preference == mVolumeSliderIntensity) {
            Settings.System.putInt(mContext.getContentResolver(),
                    "volume_slider_haptics_intensity", intensity);
            mVolumeSliderIntensity.setProgress(intensity);
            isChanged = true;
        }
        if (isChanged) {
            VibrationUtils.triggerVibration(mContext, intensity);
            return true;
        }

        return false;
    }

}
