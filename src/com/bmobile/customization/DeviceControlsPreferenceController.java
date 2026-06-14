/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import static org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_DEVICECONTROLS;

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

import lineageos.app.LineageGlobalActions;

public class DeviceControlsPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private static final Uri SHOW_CONTROLS_URI =
            Settings.Secure.getUriFor(Settings.Secure.LOCKSCREEN_SHOW_CONTROLS);
    private static final Uri USE_CONTROLS_URI =
            Settings.Secure.getUriFor(Settings.Secure.LOCKSCREEN_ALLOW_TRIVIAL_CONTROLS);

    private final LineageGlobalActions mLineageGlobalActions;
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
        mLineageGlobalActions = LineageGlobalActions.getInstance(context);
    }

    @Override
    public int getAvailabilityStatus() {
        return DeviceControlsUtils.hasControlsFeature(mContext) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
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
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    private CharSequence buildSummary() {
        final boolean showControls = Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.LOCKSCREEN_SHOW_CONTROLS, 0) != 0;
        final boolean useControls = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.LOCKSCREEN_ALLOW_TRIVIAL_CONTROLS, 0) != 0;
        final boolean powerMenu = mLineageGlobalActions.userConfigContains(
                GLOBAL_ACTION_KEY_DEVICECONTROLS);

        CharSequence summary;
        if (!showControls) {
            summary = mContext.getString(R.string.display_customization_device_controls_off);
        } else if (useControls) {
            summary = mContext.getText(R.string.lockscreen_trivial_controls_summary);
        } else {
            summary = mContext.getText(R.string.lockscreen_privacy_controls_summary);
        }

        if (powerMenu) {
            summary = mContext.getString(
                    R.string.display_customization_device_controls_with_power_menu, summary);
        }
        return summary;
    }
}
