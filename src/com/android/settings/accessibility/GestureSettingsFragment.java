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

package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import com.android.settingslib.core.AbstractPreferenceController;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * GestureSettingsFragment provides the UI for configuring GestureFlow motion gestures.
 * Allows users to enable/disable gesture control, adjust sensitivity, and view instructions.
 */
public class GestureSettingsFragment extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "GestureSettingsFragment";

    // Preference keys
    private static final String KEY_ENABLE_GESTURES = "gestureflow_enable";
    private static final String KEY_SENSITIVITY = "gestureflow_sensitivity";

    // Settings keys
    private static final String GESTUREFLOW_ENABLED = "gestureflow_enabled";
    private static final String GESTUREFLOW_SENSITIVITY = "gestureflow_sensitivity";

    // Preferences
    private SwitchPreferenceCompat mEnableGesturesPreference;
    private SeekBarPreference mSensitivityPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gestureflow_settings);
        initializePreferences();
    }


    private void initializePreferences() {
        mEnableGesturesPreference = findPreference(KEY_ENABLE_GESTURES);
        mSensitivityPreference = findPreference(KEY_SENSITIVITY);

        if (mEnableGesturesPreference != null) {
            mEnableGesturesPreference.setOnPreferenceChangeListener(this);
            updateEnablePreferenceState();
        }

        if (mSensitivityPreference != null) {
            // Set layout resource to ensure TextView exists for value display
            mSensitivityPreference.setLayoutResource(com.android.settings.R.layout.adaptive_preference_card_seekbar);
            mSensitivityPreference.setOnPreferenceChangeListener(this);
            updateSensitivityPreferenceState();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        final ContentResolver resolver = getContentResolver();

        if (KEY_ENABLE_GESTURES.equals(key)) {
            boolean enabled = (Boolean) newValue;
            Settings.System.putInt(resolver, GESTUREFLOW_ENABLED, enabled ? 1 : 0);
            updateEnablePreferenceState();
            return true;

        } else if (KEY_SENSITIVITY.equals(key)) {
            int sensitivity = (Integer) newValue;
            Settings.System.putInt(resolver, GESTUREFLOW_SENSITIVITY, sensitivity);
            return true;
        }

        return false;
    }

    private void updateEnablePreferenceState() {
        if (mEnableGesturesPreference == null) return;

        try {
            boolean enabled = Settings.System.getInt(getContentResolver(),
                    GESTUREFLOW_ENABLED, 0) == 1;
            mEnableGesturesPreference.setChecked(enabled);
        } catch (Exception e) {
            mEnableGesturesPreference.setChecked(false);
        }
    }

    private void updateSensitivityPreferenceState() {
        if (mSensitivityPreference == null) return;

        try {
            int sensitivity = Settings.System.getInt(getContentResolver(),
                    GESTUREFLOW_SENSITIVITY, 10); // Default: 10
            mSensitivityPreference.setValue(sensitivity);
        } catch (Exception e) {
            mSensitivityPreference.setValue(10);
        }
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ACCESSIBILITY;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh preference states when returning to the fragment
        updateEnablePreferenceState();
        updateSensitivityPreferenceState();
    }

    // Preference Controllers
    public static class GestureEnableController extends AbstractPreferenceController
            implements OnPreferenceChangeListener {

        public GestureEnableController(Context context) {
            super(context);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String getPreferenceKey() {
            return "gestureflow_enable";
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            boolean enabled = (Boolean) newValue;
            Settings.System.putInt(mContext.getContentResolver(),
                    "gestureflow_enabled", enabled ? 1 : 0);
            return true;
        }
    }

    public static class GestureSensitivityController extends AbstractPreferenceController
            implements OnPreferenceChangeListener {

        public GestureSensitivityController(Context context) {
            super(context);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String getPreferenceKey() {
            return "gestureflow_sensitivity";
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            int sensitivity = (Integer) newValue;
            Settings.System.putInt(mContext.getContentResolver(),
                    "gestureflow_sensitivity", sensitivity);
            return true;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (preference instanceof SeekBarPreference) {
                SeekBarPreference seekBar = (SeekBarPreference) preference;
                try {
                    int sensitivity = Settings.System.getInt(mContext.getContentResolver(),
                            "gestureflow_sensitivity", 10);
                    seekBar.setValue(sensitivity);
                } catch (Exception e) {
                    seekBar.setValue(10);
                }
            }
        }
    }
}
