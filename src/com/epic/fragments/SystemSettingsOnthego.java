/*
 * Copyright (C) 2025 the-jekts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. thanks  EpicROM-AOSP for base code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epic.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;

import com.android.settings.widget.SeekBarPreference;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.R;
import androidx.annotation.NonNull;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.power.OnTheGoPreferenceController;
import com.android.settings.power.OnTheGoAlphaPreferenceController;
import com.android.settings.power.OnTheGoCameraPreferenceController;
import com.android.settings.power.OnTheGoServiceRestartPreferenceController;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingsOnthego extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "SystemSettingsOnthego";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.system_settings_onthego);
        
        Log.e(TAG, "SystemSettingsOnthego onCreate - loading controllers");
        
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Context context = getActivity();
        
        // Initialize and update controllers
        OnTheGoPreferenceController mainController = new OnTheGoPreferenceController(context);
        if (mainController.isAvailable()) {
            Preference mainPref = prefScreen.findPreference("global_actions_onthego");
            if (mainPref != null) {
                mainController.updateState(mainPref);
                mainPref.setOnPreferenceChangeListener(mainController);
                Log.e(TAG, "✓ OnTheGoPreferenceController initialized");
            }
        }
        
        OnTheGoAlphaPreferenceController alphaController = new OnTheGoAlphaPreferenceController(context, "onthego_alpha");
        if (alphaController.getAvailabilityStatus() == BasePreferenceController.AVAILABLE) {
            SeekBarPreference alphaPref = (SeekBarPreference) prefScreen.findPreference("onthego_alpha");
            if (alphaPref != null) {
                alphaController.updateState(alphaPref);
                alphaPref.setOnSeekBarChangeListener(alphaController);
                Log.e(TAG, "✓ OnTheGoAlphaPreferenceController initialized");
            }
        }

        OnTheGoCameraPreferenceController cameraController = new OnTheGoCameraPreferenceController(context, "onthego_camera");
        if (cameraController.getAvailabilityStatus() == BasePreferenceController.AVAILABLE) {
            Preference cameraPref = prefScreen.findPreference("onthego_camera");
            if (cameraPref != null) {
                cameraController.updateState(cameraPref);
                cameraPref.setOnPreferenceChangeListener(cameraController);
                Log.e(TAG, "✓ OnTheGoCameraPreferenceController initialized");
            }
        }

        OnTheGoServiceRestartPreferenceController restartController = new OnTheGoServiceRestartPreferenceController(context, "onthego_service_restart");
        if (restartController.getAvailabilityStatus() == BasePreferenceController.AVAILABLE) {
            Preference restartPref = prefScreen.findPreference("onthego_service_restart");
            if (restartPref != null) {
                restartController.updateState(restartPref);
                restartPref.setOnPreferenceChangeListener(restartController);
                Log.e(TAG, "✓ OnTheGoServiceRestartPreferenceController initialized");
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.e(TAG, "onPreferenceChange called for: " + preference.getKey());
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
