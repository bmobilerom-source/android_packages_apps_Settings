/*
 * Copyright (C) 2025 BashaMobile
 *
 * Preference controller for BShare in Connected Devices
 */

package com.android.settings.bshare;

import android.content.Context;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for BShare preference in Connected Devices
 */
public class BSharePreferenceController extends BasePreferenceController {
    private static final String KEY = "bshare_settings";
    
    public BSharePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    
    @Override
    public int getAvailabilityStatus() {
        // Available on all devices (uses native Android APIs, no GMS required)
        return AVAILABLE;
    }
    
    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(mContext.getString(R.string.bshare_settings_summary));
        }
    }
}

