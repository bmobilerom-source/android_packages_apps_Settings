/*
 * Copyright (C) 2022 EpicROM-AOSP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epic;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;
import android.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.specialaccess.InstallAppWhitelistController;

public class Anatolia extends SettingsPreferenceFragment {

    private InstallAppWhitelistController mInstallAppWhitelistController;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.anatolia);

        // Initialize the install app whitelist controller
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Context context = getActivity();
        if (context != null && prefScreen != null) {
            mInstallAppWhitelistController = new InstallAppWhitelistController(
                    context, "install_app_whitelist_toggle");
            if (mInstallAppWhitelistController.isAvailable()) {
                final SwitchPreferenceCompat togglePref = 
                        prefScreen.findPreference("install_app_whitelist_toggle");
                if (togglePref != null) {
                    mInstallAppWhitelistController.updateState(togglePref);
                    togglePref.setOnPreferenceChangeListener(mInstallAppWhitelistController);
                }
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
