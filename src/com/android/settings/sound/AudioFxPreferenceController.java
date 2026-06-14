/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound;

import android.content.Context;
import android.content.pm.PackageManager;

import com.android.settings.core.BasePreferenceController;

/** Shows AudioFX entry only when org.lineageos.audiofx is installed on the device. */
public class AudioFxPreferenceController extends BasePreferenceController {

    static final String PACKAGE_AUDIOFX = "org.lineageos.audiofx";

    public AudioFxPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        try {
            mContext.getPackageManager().getPackageInfo(PACKAGE_AUDIOFX, 0);
            return AVAILABLE;
        } catch (PackageManager.NameNotFoundException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
    }
}
