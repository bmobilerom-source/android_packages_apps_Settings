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

package com.android.settings.sound;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

/**
 * Audio Enhancement Settings main fragment.
 * Provides access to equalizer, bass boost, virtualizer, reverb, and dynamics processing.
 */
public class AudioEnhancementSettings extends DashboardFragment {
    private static final String TAG = "AudioEnhancementSettings";

    private AudioEnhancementManager mAudioManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAudioManager = AudioEnhancementManager.getInstance(context);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error getting AudioEnhancementManager instance", e);
            mAudioManager = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: Audio effects are NOT initialized here to prevent boot loops
        // Effects will only be initialized when user explicitly enables them
        // via the master toggle controller
        android.util.Log.d(TAG, "AudioEnhancementSettings created - effects will initialize on demand only");
    }

    @Override
    public void onDestroy() {
        // Don't release effects here - they should persist
        // Only release when Settings app closes completely
        super.onDestroy();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.audio_enhancement_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        final Lifecycle lifecycle = getSettingsLifecycle();

        try {
            // Ensure AudioManager is initialized
            if (mAudioManager == null) {
                mAudioManager = AudioEnhancementManager.getInstance(context);
            }
            
            // Create controllers that actually control the audio effects
            if (mAudioManager != null) {
                controllers.add(new AudioEnhancementMasterController(context, mAudioManager));
                controllers.add(new EqualizerPreferenceController(context, mAudioManager));
                controllers.add(new BassBoostPreferenceController(context, mAudioManager));
                controllers.add(new VirtualizerPreferenceController(context, mAudioManager));
                controllers.add(new ReverbPreferenceController(context, mAudioManager));
                controllers.add(new DynamicsProcessingPreferenceController(context, mAudioManager));
            } else {
                android.util.Log.e(TAG, "Cannot create controllers - AudioEnhancementManager is null");
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error creating preference controllers", e);
        }

        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SOUND;
    }

    /**
     * Master controller for audio enhancement enable/disable
     */
    public static class AudioEnhancementMasterController extends com.android.settings.core.BasePreferenceController
            implements androidx.preference.Preference.OnPreferenceChangeListener {

        private final AudioEnhancementManager mAudioManager;

        public AudioEnhancementMasterController(Context context, AudioEnhancementManager audioManager) {
            super(context, "audio_enhancement_enabled");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            return AVAILABLE;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (mAudioManager == null) {
                android.util.Log.w("AudioEnhancementMasterController", "AudioManager is null");
                return;
            }
            if (preference instanceof androidx.preference.SwitchPreferenceCompat) {
                androidx.preference.SwitchPreferenceCompat switchPref = (androidx.preference.SwitchPreferenceCompat) preference;
                // Master toggle is enabled if any effect is enabled
                boolean anyEnabled = mAudioManager.getEqualizerEnabled() ||
                                   mAudioManager.getBassBoostEnabled() ||
                                   mAudioManager.getVirtualizerEnabled() ||
                                   mAudioManager.getDynamicsEnabled() ||
                                   mAudioManager.getReverbPreset() != android.media.audiofx.PresetReverb.PRESET_NONE;
                switchPref.setChecked(anyEnabled);
            }
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            if (mAudioManager == null) {
                return false;
            }
            boolean enabled = (Boolean) newValue;
            if (enabled) {
                // Enable effects that were previously enabled
                mAudioManager.initializeEffects();
            } else {
                // Disable all effects
                mAudioManager.setEqualizerEnabled(false);
                mAudioManager.setBassBoostEnabled(false);
                mAudioManager.setVirtualizerEnabled(false);
                mAudioManager.setDynamicsEnabled(false);
                mAudioManager.setReverbPreset(android.media.audiofx.PresetReverb.PRESET_NONE);
            }
            return true;
        }
    }

    /**
     * Equalizer controller
     */
    public static class EqualizerPreferenceController extends com.android.settings.core.BasePreferenceController {
        private final AudioEnhancementManager mAudioManager;

        public EqualizerPreferenceController(Context context, AudioEnhancementManager audioManager) {
            super(context, "equalizer");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            if (mAudioManager == null) {
                return UNSUPPORTED_ON_DEVICE;
            }
            return mAudioManager.isEqualizerAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            // Update equalizer state if needed
        }
    }

    /**
     * Bass Boost controller
     */
    public static class BassBoostPreferenceController extends com.android.settings.core.BasePreferenceController
            implements androidx.preference.Preference.OnPreferenceChangeListener {

        private final AudioEnhancementManager mAudioManager;

        public BassBoostPreferenceController(Context context, AudioEnhancementManager audioManager) {
            super(context, "bass_boost");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            if (mAudioManager == null) {
                return UNSUPPORTED_ON_DEVICE;
            }
            return mAudioManager.isBassBoostAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            if (mAudioManager == null) {
                return false;
            }
            int strength = (Integer) newValue;
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "bass_boost_strength", strength);
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "bass_boost_enabled", strength > 0 ? 1 : 0);

            mAudioManager.setBassBoostStrength(strength);
            mAudioManager.setBassBoostEnabled(strength > 0);
            return true;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (mAudioManager == null) {
                return;
            }
            if (preference instanceof com.android.settings.widget.SeekBarPreference) {
                com.android.settings.widget.SeekBarPreference seekBar = (com.android.settings.widget.SeekBarPreference) preference;
                int strength = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                        "bass_boost_strength", 0);
                seekBar.setProgress(strength);
                preference.setSummary(strength + "% boost");
            }
        }
    }

    /**
     * Virtualizer controller
     */
    public static class VirtualizerPreferenceController extends com.android.settings.core.BasePreferenceController
            implements androidx.preference.Preference.OnPreferenceChangeListener {

        private final AudioEnhancementManager mAudioManager;

        public VirtualizerPreferenceController(Context context, AudioEnhancementManager audioManager) {
            super(context, "virtualizer");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            if (mAudioManager == null) {
                return UNSUPPORTED_ON_DEVICE;
            }
            return mAudioManager.isVirtualizerAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            if (mAudioManager == null) {
                return false;
            }
            int strength = (Integer) newValue;
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "virtualizer_strength", strength);
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "virtualizer_enabled", strength > 0 ? 1 : 0);

            mAudioManager.setVirtualizerStrength(strength);
            mAudioManager.setVirtualizerEnabled(strength > 0);
            return true;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (mAudioManager == null) {
                return;
            }
            if (preference instanceof com.android.settings.widget.SeekBarPreference) {
                com.android.settings.widget.SeekBarPreference seekBar = (com.android.settings.widget.SeekBarPreference) preference;
                int strength = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                        "virtualizer_strength", 0);
                seekBar.setProgress(strength);
                preference.setSummary(strength + "% virtualization");
            }
        }
    }

    /**
     * Reverb controller
     */
    public static class ReverbPreferenceController extends com.android.settings.core.BasePreferenceController
            implements androidx.preference.Preference.OnPreferenceChangeListener {

        private final AudioEnhancementManager mAudioManager;

        public ReverbPreferenceController(Context context, AudioEnhancementManager audioManager) {
            super(context, "reverb");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            if (mAudioManager == null) {
                return UNSUPPORTED_ON_DEVICE;
            }
            return mAudioManager.isReverbAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            if (mAudioManager == null) {
                return false;
            }
            int preset = Integer.parseInt((String) newValue);
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "reverb_preset", preset);

            mAudioManager.setReverbPreset(preset);
            return true;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (mAudioManager == null) {
                return;
            }
            if (preference instanceof androidx.preference.ListPreference) {
                androidx.preference.ListPreference listPref = (androidx.preference.ListPreference) preference;
                int preset = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                        "reverb_preset", android.media.audiofx.PresetReverb.PRESET_NONE);
                listPref.setValue(String.valueOf(preset));
            }
        }
    }

    /**
     * Dynamics Processing controller
     */
    public static class DynamicsProcessingPreferenceController extends com.android.settings.core.BasePreferenceController
            implements androidx.preference.Preference.OnPreferenceChangeListener {

        private final AudioEnhancementManager mAudioManager;

        public DynamicsProcessingPreferenceController(Context context, AudioEnhancementManager audioManager) {
            super(context, "dynamics_processing");
            mAudioManager = audioManager;
        }

        @Override
        public int getAvailabilityStatus() {
            if (mAudioManager == null) {
                return UNSUPPORTED_ON_DEVICE;
            }
            return mAudioManager.isDynamicsAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }

        @Override
        public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
            if (mAudioManager == null) {
                return false;
            }
            boolean enabled = (Boolean) newValue;
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                    "dynamics_enabled", enabled ? 1 : 0);

            mAudioManager.setDynamicsEnabled(enabled);
            return true;
        }

        @Override
        public void updateState(androidx.preference.Preference preference) {
            super.updateState(preference);
            if (mAudioManager == null) {
                return;
            }
            if (preference instanceof androidx.preference.SwitchPreferenceCompat) {
                androidx.preference.SwitchPreferenceCompat switchPref = (androidx.preference.SwitchPreferenceCompat) preference;
                boolean enabled = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                        "dynamics_enabled", 0) == 1;
                switchPref.setChecked(enabled);
            }
        }
    }
}
