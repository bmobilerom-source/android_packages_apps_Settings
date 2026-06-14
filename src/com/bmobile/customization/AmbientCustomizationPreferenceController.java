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

public class AmbientCustomizationPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private static final Uri AMBIENT_TEXT_URI =
            Settings.System.getUriFor(AmbientCustomizationsHelper.SETTING_AMBIENT_TEXT);
    private static final Uri AMBIENT_IMAGE_URI =
            Settings.System.getUriFor(AmbientCustomizationsHelper.SETTING_AMBIENT_IMAGE);

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

    public AmbientCustomizationPreferenceController(Context context, String preferenceKey) {
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
        mContext.getContentResolver().registerContentObserver(AMBIENT_TEXT_URI, false, mObserver);
        mContext.getContentResolver().registerContentObserver(AMBIENT_IMAGE_URI, false, mObserver);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    private CharSequence buildSummary() {
        if (AmbientCustomizationsHelper.isAmbientDisplayEnabled(mContext)) {
            return mContext.getString(R.string.ambient_master_title);
        }
        if (!AmbientCustomizationsHelper.isAmbientCustomizationEnabled(mContext)) {
            return mContext.getString(R.string.ambient_customization_summary_off);
        }
        final StringBuilder summary = new StringBuilder();
        if (AmbientCustomizationsHelper.isAmbientTextEnabled(mContext)) {
            summary.append(mContext.getString(R.string.ambient_text_enable_title));
        }
        if (AmbientCustomizationsHelper.isAmbientImageEnabled(mContext)) {
            if (summary.length() > 0) {
                summary.append(" · ");
            }
            summary.append(mContext.getString(R.string.ambient_image_enable_title));
        }
        return summary.length() > 0 ? summary
                : mContext.getString(R.string.ambient_master_summary);
    }
}
