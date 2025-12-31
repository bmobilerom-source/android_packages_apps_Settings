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

import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.DynamicsProcessing;
import android.provider.Settings;
import android.util.Log;

/**
 * Singleton manager for Android audio enhancement effects.
 * Manages Equalizer, BassBoost, Virtualizer, Reverb, and DynamicsProcessing effects.
 */
public class AudioEnhancementManager {
    private static final String TAG = "AudioEnhancementManager";
    private static AudioEnhancementManager sInstance;

    private static final int GLOBAL_AUDIO_SESSION = 0;

    // Internal state variables (settings are managed by controllers)
    private boolean mEqualizerEnabled = false;
    private int mEqualizerPreset = 0;
    private boolean mBassBoostEnabled = false;
    private int mBassBoostStrength = 0;
    private boolean mVirtualizerEnabled = false;
    private int mVirtualizerStrength = 0;
    private int mReverbPreset = android.media.audiofx.PresetReverb.PRESET_NONE;
    private boolean mDynamicsEnabled = false;

    private final Context mContext; // Used in syncStateWithSettings()

    // Audio effect instances
    private Equalizer mEqualizer;
    private BassBoost mBassBoost;
    private Virtualizer mVirtualizer;
    private PresetReverb mPresetReverb;
    private DynamicsProcessing mDynamicsProcessing;

    private boolean mEffectsInitialized = false;

    private AudioEnhancementManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized AudioEnhancementManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AudioEnhancementManager(context);
        }
        return sInstance;
    }

    /**
     * Sync internal state with saved settings
     */
    private void syncStateWithSettings() {
        mEqualizerEnabled = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "equalizer_enabled", 0) == 1;
        mEqualizerPreset = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "equalizer_preset", 0);
        mBassBoostEnabled = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "bass_boost_enabled", 0) == 1;
        mBassBoostStrength = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "bass_boost_strength", 0);
        mVirtualizerEnabled = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "virtualizer_enabled", 0) == 1;
        mVirtualizerStrength = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "virtualizer_strength", 0);
        mReverbPreset = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "reverb_preset", android.media.audiofx.PresetReverb.PRESET_NONE);
        mDynamicsEnabled = android.provider.Settings.Secure.getInt(mContext.getContentResolver(),
                "dynamics_enabled", 0) == 1;
    }

    /**
     * Initialize all available audio effects
     * Uses GLOBAL_AUDIO_SESSION (0) which applies effects globally to all audio playback,
     * including music players, system sounds, and other audio apps.
     *
     * WARNING: Only call this when audio services are fully initialized!
     * Calling during system boot will cause boot loops.
     */
    public void initializeEffects() {
        // If already initialized, just ensure effects are enabled according to current settings
        if (mEffectsInitialized) {
            syncStateWithSettings();
            // Re-apply current settings to ensure effects are in correct state
            if (mEqualizer != null) mEqualizer.setEnabled(getEqualizerEnabled());
            if (mBassBoost != null) {
                mBassBoost.setEnabled(getBassBoostEnabled());
                mBassBoost.setStrength((short) getBassBoostStrength());
            }
            if (mVirtualizer != null) {
                mVirtualizer.setEnabled(getVirtualizerEnabled());
                mVirtualizer.setStrength((short) getVirtualizerStrength());
            }
            if (mPresetReverb != null) {
                mPresetReverb.setEnabled(getReverbPreset() != PresetReverb.PRESET_NONE);
                mPresetReverb.setPreset((short) getReverbPreset());
            }
            if (mDynamicsProcessing != null) mDynamicsProcessing.setEnabled(getDynamicsEnabled());
            return;
        }

        // First sync state with saved settings
        syncStateWithSettings();

        Log.d(TAG, "Initializing audio enhancement effects with GLOBAL_AUDIO_SESSION");

        try {
            // Initialize Equalizer
            try {
                mEqualizer = new Equalizer(0, GLOBAL_AUDIO_SESSION);
                mEqualizer.setEnabled(getEqualizerEnabled());
                loadEqualizerSettings();
                Log.d(TAG, "Equalizer initialized for global audio session");
            } catch (Exception e) {
                Log.w(TAG, "Equalizer not available on this device", e);
            }

            // Initialize BassBoost
            try {
                mBassBoost = new BassBoost(0, GLOBAL_AUDIO_SESSION);
                mBassBoost.setEnabled(getBassBoostEnabled());
                mBassBoost.setStrength((short) getBassBoostStrength());
                Log.d(TAG, "BassBoost initialized for global audio session");
            } catch (Exception e) {
                Log.w(TAG, "BassBoost not available on this device", e);
            }

            // Initialize Virtualizer
            try {
                mVirtualizer = new Virtualizer(0, GLOBAL_AUDIO_SESSION);
                mVirtualizer.setEnabled(getVirtualizerEnabled());
                mVirtualizer.setStrength((short) getVirtualizerStrength());
                Log.d(TAG, "Virtualizer initialized for global audio session");
            } catch (Exception e) {
                Log.w(TAG, "Virtualizer not available on this device", e);
            }

            // Initialize PresetReverb
            try {
                mPresetReverb = new PresetReverb(0, GLOBAL_AUDIO_SESSION);
                mPresetReverb.setEnabled(getReverbPreset() != PresetReverb.PRESET_NONE);
                mPresetReverb.setPreset((short) getReverbPreset());
                Log.d(TAG, "PresetReverb initialized for global audio session");
            } catch (Exception e) {
                Log.w(TAG, "PresetReverb not available on this device", e);
            }

            // Initialize DynamicsProcessing
            try {
                mDynamicsProcessing = new DynamicsProcessing(0, GLOBAL_AUDIO_SESSION);
                mDynamicsProcessing.setEnabled(getDynamicsEnabled());
                Log.d(TAG, "DynamicsProcessing initialized for global audio session");
            } catch (Exception e) {
                Log.w(TAG, "DynamicsProcessing not available on this device", e);
            }

            mEffectsInitialized = true;
            Log.d(TAG, "All audio effects initialized successfully - will apply to music players and all audio playback");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing audio effects", e);
        }
    }

    /**
     * Release all audio effects
     */
    public void releaseEffects() {
        if (!mEffectsInitialized) {
            return;
        }

        Log.d(TAG, "Releasing audio effects");

        if (mEqualizer != null) {
            mEqualizer.release();
            mEqualizer = null;
        }
        if (mBassBoost != null) {
            mBassBoost.release();
            mBassBoost = null;
        }
        if (mVirtualizer != null) {
            mVirtualizer.release();
            mVirtualizer = null;
        }
        if (mPresetReverb != null) {
            mPresetReverb.release();
            mPresetReverb = null;
        }
        if (mDynamicsProcessing != null) {
            mDynamicsProcessing.release();
            mDynamicsProcessing = null;
        }

        mEffectsInitialized = false;
    }

    // Equalizer methods
    public boolean isEqualizerAvailable() {
        return mEqualizer != null;
    }

    public boolean getEqualizerEnabled() {
        return mEqualizerEnabled;
    }

    public void setEqualizerEnabled(boolean enabled) {
        mEqualizerEnabled = enabled;
        if (mEqualizer != null) {
            mEqualizer.setEnabled(enabled);
        }
    }

    public int getEqualizerPreset() {
        return mEqualizerPreset;
    }

    public void setEqualizerPreset(int preset) {
        mEqualizerPreset = preset;
        if (mEqualizer != null) {
            mEqualizer.usePreset((short) preset);
        }
    }

    public int getEqualizerNumberOfBands() {
        return mEqualizer != null ? mEqualizer.getNumberOfBands() : 0;
    }

    public int getEqualizerCenterFreq(int band) {
        return mEqualizer != null ? mEqualizer.getCenterFreq((short) band) : 0;
    }

    public int[] getEqualizerBandLevelRange() {
        if (mEqualizer != null) {
            short[] range = mEqualizer.getBandLevelRange();
            return new int[]{range[0], range[1]};
        }
        return new int[]{-1500, 1500};
    }

    public int getEqualizerBandLevel(int band) {
        return mEqualizer != null ? mEqualizer.getBandLevel((short) band) : 0;
    }

    public void setEqualizerBandLevel(int band, int level) {
        if (mEqualizer != null) {
            mEqualizer.setBandLevel((short) band, (short) level);
        }
    }

    // BassBoost methods
    public boolean isBassBoostAvailable() {
        return mBassBoost != null;
    }

    public boolean getBassBoostEnabled() {
        return mBassBoostEnabled;
    }

    public void setBassBoostEnabled(boolean enabled) {
        mBassBoostEnabled = enabled;
        if (mBassBoost != null) {
            mBassBoost.setEnabled(enabled);
        }
    }

    public int getBassBoostStrength() {
        return mBassBoostStrength;
    }

    public void setBassBoostStrength(int strength) {
        mBassBoostStrength = strength;
        if (mBassBoost != null) {
            mBassBoost.setStrength((short) strength);
        }
    }

    // Virtualizer methods
    public boolean isVirtualizerAvailable() {
        return mVirtualizer != null;
    }

    public boolean getVirtualizerEnabled() {
        return mVirtualizerEnabled;
    }

    public void setVirtualizerEnabled(boolean enabled) {
        mVirtualizerEnabled = enabled;
        if (mVirtualizer != null) {
            mVirtualizer.setEnabled(enabled);
        }
    }

    public int getVirtualizerStrength() {
        return mVirtualizerStrength;
    }

    public void setVirtualizerStrength(int strength) {
        mVirtualizerStrength = strength;
        if (mVirtualizer != null) {
            mVirtualizer.setStrength((short) strength);
        }
    }

    // Reverb methods
    public boolean isReverbAvailable() {
        return mPresetReverb != null;
    }

    public int getReverbPreset() {
        return mReverbPreset;
    }

    public void setReverbPreset(int preset) {
        mReverbPreset = preset;
        if (mPresetReverb != null) {
            mPresetReverb.setPreset((short) preset);
            mPresetReverb.setEnabled(preset != PresetReverb.PRESET_NONE);
        }
    }

    // DynamicsProcessing methods
    public boolean isDynamicsAvailable() {
        return mDynamicsProcessing != null;
    }

    public boolean getDynamicsEnabled() {
        return mDynamicsEnabled;
    }

    public void setDynamicsEnabled(boolean enabled) {
        mDynamicsEnabled = enabled;
        if (mDynamicsProcessing != null) {
            mDynamicsProcessing.setEnabled(enabled);
        }
    }

    /**
     * Load equalizer settings from internal state
     */
    private void loadEqualizerSettings() {
        if (mEqualizer == null) return;

        int preset = getEqualizerPreset();
        if (preset >= 0 && preset < mEqualizer.getNumberOfPresets()) {
            mEqualizer.usePreset((short) preset);
        }
        // Custom band levels could be loaded here if implemented
    }
}
