/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import android.content.Context;
import android.content.Intent;

import com.android.settings.core.BasePreferenceController;

public class AddDeviceControlsPreferenceController extends BasePreferenceController {

    public AddDeviceControlsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!DeviceControlsUtils.hasControlsFeature(mContext)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return DeviceControlsUtils.hasControlsProvider(mContext) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean handlePreferenceTreeClick(androidx.preference.Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        Intent intent = DeviceControlsUtils.newProviderSelectorIntent();
        if (DeviceControlsUtils.canLaunch(mContext, intent)) {
            mContext.startActivity(intent);
        }
        return true;
    }
}
