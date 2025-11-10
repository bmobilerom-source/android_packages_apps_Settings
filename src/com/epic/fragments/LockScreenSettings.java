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

package com.epic.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.anatolia_settings_lockscreen);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

    }

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PreferenceScreen screen = getPreferenceScreen();
        androidx.preference.Preference layoutPref = screen.findPreference("quick_access_grid");
        if (layoutPref instanceof com.android.settingslib.widget.LayoutPreference) {
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.quick_access_grid_recycler);
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                        getContext(), 2));

                java.util.List<QuickAccessGridAdapter.CardItem> items = new java.util.ArrayList<>();
                
                // SecurityHub
                items.add(new QuickAccessGridAdapter.CardItem(
                        R.string.quick_access_security_title,
                        R.string.quick_access_security_summary,
                        R.drawable.ic_quick_access_security,
                        "com.android.settings.security.SecuritySettings"));
                
                // Location Settings
                items.add(new QuickAccessGridAdapter.CardItem(
                        R.string.quick_access_location_title,
                        R.string.quick_access_location_summary,
                        R.drawable.ic_quick_access_location,
                        "com.android.settings.location.LocationSettings"));
                
                // Connected Devices
                items.add(new QuickAccessGridAdapter.CardItem(
                        R.string.quick_access_connected_devices_title,
                        R.string.quick_access_connected_devices_summary,
                        R.drawable.ic_quick_access_connected_devices,
                        "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment"));
                
                // Notifications
                items.add(new QuickAccessGridAdapter.CardItem(
                        R.string.quick_access_notifications_title,
                        R.string.quick_access_notifications_summary,
                        R.drawable.ic_quick_access_notifications,
                        "com.epic.fragments.NotificationSettings"));

                rv.setAdapter(new QuickAccessGridAdapter(getContext(), items, getMetricsCategory()));
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
