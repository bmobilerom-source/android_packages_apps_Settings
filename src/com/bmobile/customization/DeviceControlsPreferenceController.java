/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class DeviceControlsPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private static final Uri SHOW_CONTROLS_URI =
            Settings.Secure.getUriFor(Settings.Secure.LOCKSCREEN_SHOW_CONTROLS);
    private static final Uri USE_CONTROLS_URI =
            Settings.Secure.getUriFor(Settings.Secure.LOCKSCREEN_ALLOW_TRIVIAL_CONTROLS);
    private static final Uri DOZE_URI =
            Settings.Secure.getUriFor(Settings.Secure.DOZE_ENABLED);

    private Preference mPreference;
    private final ContentObserver mObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            if (mPreference != null) {
                refreshSummary(mPreference);
            }
        }
    };

    public DeviceControlsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            refreshSummary(mPreference);
        }
    }

    @Override
    protected void refreshSummary(Preference preference) {
        preference.setSummary(buildSummary());
    }

    @Override
    public void onStart() {
        mContext.getContentResolver().registerContentObserver(
                SHOW_CONTROLS_URI, false, mObserver);
        mContext.getContentResolver().registerContentObserver(
                USE_CONTROLS_URI, false, mObserver);
        mContext.getContentResolver().registerContentObserver(
                DOZE_URI, false, mObserver);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    private CharSequence buildSummary() {
        if (!DeviceControlsUtils.hasControlsFeature(mContext)) {
            return mContext.getString(R.string.device_controls_page_summary_no_feature);
        }

        final boolean showControls = Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.LOCKSCREEN_SHOW_CONTROLS, 0) != 0;
        final boolean useControls = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.LOCKSCREEN_ALLOW_TRIVIAL_CONTROLS, 0) != 0;
        final boolean wakeForNotifs = Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.DOZE_ENABLED, 0) != 0;

        if (!showControls && !wakeForNotifs) {
            return mContext.getString(R.string.display_customization_device_controls_off);
        }

        CharSequence summary;
        if (showControls && useControls) {
            summary = mContext.getText(R.string.lockscreen_trivial_controls_summary);
        } else if (showControls) {
            summary = mContext.getText(R.string.lockscreen_privacy_controls_summary);
        } else {
            summary = mContext.getString(R.string.device_controls_page_summary_wake_only);
        }

        if (wakeForNotifs && showControls) {
            summary = mContext.getString(
                    R.string.device_controls_page_summary_with_wake, summary);
        }
        return summary;
    }
}
