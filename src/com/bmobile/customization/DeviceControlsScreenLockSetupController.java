/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import android.content.Context;
import android.os.UserHandle;

import androidx.preference.Preference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;

/** Prompts the user to set a screen lock before using lock screen privacy options. */
public class DeviceControlsScreenLockSetupController extends BasePreferenceController {

    public DeviceControlsScreenLockSetupController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isSecure() ? UNSUPPORTED_ON_DEVICE : AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(mContext.getText(R.string.lockscreen_privacy_not_secure));
    }

    private boolean isSecure() {
        final LockPatternUtils utils = FeatureFactory.getFeatureFactory()
                .getSecurityFeatureProvider()
                .getLockPatternUtils(mContext);
        return utils.isSecure(UserHandle.myUserId());
    }
}
