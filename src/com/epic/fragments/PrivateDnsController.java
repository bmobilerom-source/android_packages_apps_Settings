/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;

import android.content.Context;
import android.text.TextUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.R;

public class PrivateDnsController extends BasePreferenceController {
    
    public PrivateDnsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            // Navigate to Network & Internet settings where Private DNS is configured
            new SubSettingLauncher(mContext)
                .setDestination("com.android.settings.network.NetworkProviderSettings")
                .setTitleRes(R.string.network_and_internet_preferences_title)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
            return true;
        }
        return false;
    }

    @Override
    public CharSequence getSummary() {
        String mode = PrivacySecurityHelper.getPrivateDnsMode(mContext);
        String specifier = PrivacySecurityHelper.getPrivateDnsSpecifier(mContext);
        if (mode == null || mode.equals("off")) {
            return "Off";
        } else if (mode.equals("opportunistic")) {
            return "Automatic";
        } else if (mode.equals("hostname") && specifier != null && !specifier.isEmpty()) {
            return specifier;
        }
        return "Off";
    }
}
