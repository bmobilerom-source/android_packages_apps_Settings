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

import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.widget.SelectorWithWidgetPreference;

/**
 * Settings for Adaptive Playback (Smart Pause) feature
 */
public class AdaptivePlaybackSoundSettings extends InstrumentedPreferenceFragment
        implements SelectorWithWidgetPreference.OnClickListener {

    private static final String TAG = "AdaptivePlaybackSoundSettings";

    // Settings keys
    private static final String KEY_NO_TIMEOUT = "adaptive_playback_timeout_none";
    private static final String KEY_30_SECS = "adaptive_playback_timeout_30_secs";
    private static final String KEY_1_MIN = "adaptive_playback_timeout_1_min";
    private static final String KEY_2_MIN = "adaptive_playback_timeout_2_min";
    private static final String KEY_5_MIN = "adaptive_playback_timeout_5_min";
    private static final String KEY_10_MIN = "adaptive_playback_timeout_10_min";

    // Settings values
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_NONE = 0;
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS = 30;
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN = 60;
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN = 120;
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN = 300;
    private static final int ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN = 600;

    private Context mContext;
    private PreferenceCategory mPreferenceCategory;

    // Radio button preferences
    private SelectorWithWidgetPreference mTimeoutNonePref;
    private SelectorWithWidgetPreference mTimeout30SecPref;
    private SelectorWithWidgetPreference mTimeout1MinPref;
    private SelectorWithWidgetPreference mTimeout2MinPref;
    private SelectorWithWidgetPreference mTimeout5MinPref;
    private SelectorWithWidgetPreference mTimeout10MinPref;

    // Current settings
    private boolean mAdaptivePlaybackEnabled;
    private int mAdaptivePlaybackTimeout;

    private final SettingObserver mSettingObserver = new SettingObserver(new Handler());

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SOUND;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        // Load current settings
        final ContentResolver cr = mContext.getContentResolver();
        mAdaptivePlaybackEnabled = Settings.System.getIntForUser(
                cr, Settings.System.ADAPTIVE_PLAYBACK_ENABLED, 0,
                UserHandle.USER_CURRENT) != 0;
        mAdaptivePlaybackTimeout = Settings.System.getIntForUser(
                cr, Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT,
                ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS, UserHandle.USER_CURRENT);

        // Add preferences from XML
        addPreferencesFromResource(R.xml.adaptive_playback_sound_settings);

        // Get preference category
        mPreferenceCategory = (PreferenceCategory) findPreference("adaptive_playback_category");

        if (mPreferenceCategory != null) {
            // Create radio button preferences
            mTimeoutNonePref = makeRadioPreference(KEY_NO_TIMEOUT, R.string.adaptive_playback_timeout_none);
            mTimeout30SecPref = makeRadioPreference(KEY_30_SECS, R.string.adaptive_playback_timeout_30_secs);
            mTimeout1MinPref = makeRadioPreference(KEY_1_MIN, R.string.adaptive_playback_timeout_1_min);
            mTimeout2MinPref = makeRadioPreference(KEY_2_MIN, R.string.adaptive_playback_timeout_2_min);
            mTimeout5MinPref = makeRadioPreference(KEY_5_MIN, R.string.adaptive_playback_timeout_5_min);
            mTimeout10MinPref = makeRadioPreference(KEY_10_MIN, R.string.adaptive_playback_timeout_10_min);
        }

        updateState(null);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        final int value = keyToSetting(preference.getKey());
        Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT, value,
                UserHandle.USER_CURRENT);
        mAdaptivePlaybackTimeout = value;
        updateState(preference.getKey());
    }

    private void updateState(String key) {
        if (mPreferenceCategory == null) return;

        if (mAdaptivePlaybackEnabled) {
            mPreferenceCategory.setEnabled(true);
            mTimeoutNonePref.setEnabled(true);
            mTimeout30SecPref.setEnabled(true);
            mTimeout1MinPref.setEnabled(true);
            mTimeout2MinPref.setEnabled(true);
            mTimeout5MinPref.setEnabled(true);
            mTimeout10MinPref.setEnabled(true);
        } else {
            mPreferenceCategory.setEnabled(false);
            mTimeoutNonePref.setEnabled(false);
            mTimeout30SecPref.setEnabled(false);
            mTimeout1MinPref.setEnabled(false);
            mTimeout2MinPref.setEnabled(false);
            mTimeout5MinPref.setEnabled(false);
            mTimeout10MinPref.setEnabled(false);
        }

        // Update radio button states
        mTimeoutNonePref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_NONE);
        mTimeout30SecPref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS);
        mTimeout1MinPref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN);
        mTimeout2MinPref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN);
        mTimeout5MinPref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN);
        mTimeout10MinPref.setChecked(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN);

        // Update summaries
        updateSummaries();
    }

    private void updateSummaries() {
        mTimeoutNonePref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_NONE ?
                R.string.adaptive_playback_timeout_none_summary : R.string.adaptive_playback_disabled_summary);
        mTimeout30SecPref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS ?
                R.string.adaptive_playback_timeout_30_secs_summary : R.string.adaptive_playback_disabled_summary);
        mTimeout1MinPref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN ?
                R.string.adaptive_playback_timeout_1_min_summary : R.string.adaptive_playback_disabled_summary);
        mTimeout2MinPref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN ?
                R.string.adaptive_playback_timeout_2_min_summary : R.string.adaptive_playback_disabled_summary);
        mTimeout5MinPref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN ?
                R.string.adaptive_playback_timeout_5_min_summary : R.string.adaptive_playback_disabled_summary);
        mTimeout10MinPref.setSummary(mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN ?
                R.string.adaptive_playback_timeout_10_min_summary : R.string.adaptive_playback_disabled_summary);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettingObserver.observe();
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.getContentResolver().unregisterContentObserver(mSettingObserver);
    }

    private static int keyToSetting(String key) {
        switch (key) {
            case KEY_NO_TIMEOUT:
                return ADAPTIVE_PLAYBACK_TIMEOUT_NONE;
            case KEY_30_SECS:
                return ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
            case KEY_1_MIN:
                return ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
            case KEY_2_MIN:
                return ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
            case KEY_5_MIN:
                return ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
            case KEY_10_MIN:
                return ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
            default:
                return ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
        }
    }

    private SelectorWithWidgetPreference makeRadioPreference(String key, int titleId) {
        SelectorWithWidgetPreference pref = new SelectorWithWidgetPreference(mPreferenceCategory.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnClickListener(this);
        mPreferenceCategory.addPreference(pref);
        return pref;
    }

    private final class SettingObserver extends ContentObserver {
        private final Uri ADAPTIVE_PLAYBACK = Settings.System.getUriFor(
                Settings.System.ADAPTIVE_PLAYBACK_ENABLED);
        private final Uri ADAPTIVE_PLAYBACK_TIMEOUT = Settings.System.getUriFor(
                Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT);

        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(ADAPTIVE_PLAYBACK, false, this, UserHandle.USER_ALL);
            cr.registerContentObserver(ADAPTIVE_PLAYBACK_TIMEOUT, false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (ADAPTIVE_PLAYBACK.equals(uri) || ADAPTIVE_PLAYBACK_TIMEOUT.equals(uri)) {
                mAdaptivePlaybackEnabled = Settings.System.getIntForUser(
                        mContext.getContentResolver(), Settings.System.ADAPTIVE_PLAYBACK_ENABLED, 0,
                        UserHandle.USER_CURRENT) != 0;
                mAdaptivePlaybackTimeout = Settings.System.getIntForUser(
                        mContext.getContentResolver(), Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT,
                        ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS, UserHandle.USER_CURRENT);
                updateState(null);
            }
        }
    }
}
