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

package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreference;

/**
 * Settings fragment for ColorBlendr-style Material You color customization
 * Provides fine-tuning controls for saturation, lightness, and pitch black theme
 */
public class MonetColorBlendrSettingsFragment extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, OnPreferenceClickListener {
    
    private static final String TAG = "ColorBlendrSettings";
    
    private static final String KEY_ACCENT_SATURATION = "accent_saturation";
    private static final String KEY_BACKGROUND_SATURATION = "background_saturation";
    private static final String KEY_BACKGROUND_LIGHTNESS = "background_lightness";
    private static final String KEY_PITCH_BLACK = "pitch_black";
    private static final String KEY_MANUAL_COLOR = "manual_color_override";
    
    private SeekBarPreference mAccentSaturationPref;
    private SeekBarPreference mBackgroundSaturationPref;
    private SeekBarPreference mBackgroundLightnessPref;
    private SwitchPreferenceCompat mPitchBlackPref;
    private Preference mManualColorPref;
    private ColorBlendrController mController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            addPreferencesFromResource(R.xml.monet_color_blendr_settings);
            
            Context context = getContext();
            if (context == null) {
                Log.e(TAG, "Context is null in onCreate");
                return;
            }
            
            mController = new ColorBlendrController(context);
            setupPreferences();
            updatePreferenceStates();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ColorBlendr settings", e);
        }
    }
    
    private void setupPreferences() {
        Preference accentPref = findPreference(KEY_ACCENT_SATURATION);
        Preference bgSatPref = findPreference(KEY_BACKGROUND_SATURATION);
        Preference bgLightPref = findPreference(KEY_BACKGROUND_LIGHTNESS);
        Preference pitchPref = findPreference(KEY_PITCH_BLACK);
        mManualColorPref = findPreference(KEY_MANUAL_COLOR);
        
        // Safely cast to SeekBarPreference with null checks
        if (accentPref instanceof SeekBarPreference) {
            mAccentSaturationPref = (SeekBarPreference) accentPref;
            mAccentSaturationPref.setOnPreferenceChangeListener(this);
            // Ensure layout is set correctly
            mAccentSaturationPref.setLayoutResource(R.layout.adaptive_preference_card_seekbar);
        } else {
            Log.w(TAG, "Accent saturation preference not found or wrong type");
        }
        
        if (bgSatPref instanceof SeekBarPreference) {
            mBackgroundSaturationPref = (SeekBarPreference) bgSatPref;
            mBackgroundSaturationPref.setOnPreferenceChangeListener(this);
            mBackgroundSaturationPref.setLayoutResource(R.layout.adaptive_preference_card_seekbar);
        } else {
            Log.w(TAG, "Background saturation preference not found or wrong type");
        }
        
        if (bgLightPref instanceof SeekBarPreference) {
            mBackgroundLightnessPref = (SeekBarPreference) bgLightPref;
            mBackgroundLightnessPref.setOnPreferenceChangeListener(this);
            mBackgroundLightnessPref.setLayoutResource(R.layout.adaptive_preference_card_seekbar);
        } else {
            Log.w(TAG, "Background lightness preference not found or wrong type");
        }
        
        if (pitchPref instanceof SwitchPreferenceCompat) {
            mPitchBlackPref = (SwitchPreferenceCompat) pitchPref;
            mPitchBlackPref.setOnPreferenceChangeListener(this);
        } else {
            Log.w(TAG, "Pitch black preference not found or wrong type");
        }
        
        if (mManualColorPref != null) {
            mManualColorPref.setOnPreferenceClickListener(this);
        }
    }
    
    private void updatePreferenceStates() {
        try {
            int accentSaturation = mController.getAccentSaturation();
            int backgroundSaturation = mController.getBackgroundSaturation();
            int backgroundLightness = mController.getBackgroundLightness();
            boolean pitchBlack = mController.isPitchBlackEnabled();
            
            // Clamp values to valid range (0-200)
            accentSaturation = Math.max(0, Math.min(200, accentSaturation));
            backgroundSaturation = Math.max(0, Math.min(200, backgroundSaturation));
            backgroundLightness = Math.max(0, Math.min(200, backgroundLightness));
            
            if (mAccentSaturationPref != null) {
                mAccentSaturationPref.setProgress(accentSaturation);
                mAccentSaturationPref.setSummary(getString(R.string.colorblendr_accent_saturation_summary, accentSaturation));
            }
            if (mBackgroundSaturationPref != null) {
                mBackgroundSaturationPref.setProgress(backgroundSaturation);
                mBackgroundSaturationPref.setSummary(getString(R.string.colorblendr_background_saturation_summary, backgroundSaturation));
            }
            if (mBackgroundLightnessPref != null) {
                mBackgroundLightnessPref.setProgress(backgroundLightness);
                mBackgroundLightnessPref.setSummary(getString(R.string.colorblendr_background_lightness_summary, backgroundLightness));
            }
            if (mPitchBlackPref != null) {
                mPitchBlackPref.setChecked(pitchBlack);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference states", e);
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (preference == mAccentSaturationPref && newValue instanceof Integer) {
                int saturation = (Integer) newValue;
                // Clamp to valid range
                saturation = Math.max(0, Math.min(200, saturation));
                mController.setAccentSaturation(saturation);
                mController.applyColorModifications();
                if (mAccentSaturationPref != null) {
                    mAccentSaturationPref.setSummary(getString(R.string.colorblendr_accent_saturation_summary, saturation));
                }
                return true;
            } else if (preference == mBackgroundSaturationPref && newValue instanceof Integer) {
                int saturation = (Integer) newValue;
                saturation = Math.max(0, Math.min(200, saturation));
                mController.setBackgroundSaturation(saturation);
                mController.applyColorModifications();
                if (mBackgroundSaturationPref != null) {
                    mBackgroundSaturationPref.setSummary(getString(R.string.colorblendr_background_saturation_summary, saturation));
                }
                return true;
            } else if (preference == mBackgroundLightnessPref && newValue instanceof Integer) {
                int lightness = (Integer) newValue;
                lightness = Math.max(0, Math.min(200, lightness));
                mController.setBackgroundLightness(lightness);
                mController.applyColorModifications();
                if (mBackgroundLightnessPref != null) {
                    mBackgroundLightnessPref.setSummary(getString(R.string.colorblendr_background_lightness_summary, lightness));
                }
                return true;
            } else if (preference == mPitchBlackPref && newValue instanceof Boolean) {
                boolean enabled = (Boolean) newValue;
                mController.setPitchBlackEnabled(enabled);
                mController.applyColorModifications();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling preference change", e);
        }
        return false;
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mManualColorPref) {
            showColorPickerDialog();
            return true;
        }
        return false;
    }
    
    private void showColorPickerDialog() {
        // TODO: Implement color picker dialog
        // For now, just log
        Log.d(TAG, "Manual color picker requested");
    }
    
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }
}

