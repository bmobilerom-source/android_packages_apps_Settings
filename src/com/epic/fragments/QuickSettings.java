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

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
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
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.anatolia_settings_quicksettings);
        }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PreferenceScreen screen = getPreferenceScreen();
        androidx.preference.Preference layoutPref = screen.findPreference("qs_grid");
        if (layoutPref instanceof com.android.settingslib.widget.LayoutPreference) {
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.qs_grid_recycler);
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                        getContext(), 2));

                java.util.List<QuickSettingsAdapter.CardItem> items = new java.util.ArrayList<>();
                // Base UI Theme
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_sb,
                        R.string.category_statusbar_title,
                        R.string.statusbar_summary,
                        "com.afterlife.afterlab.StatusBar"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_qs,
                        R.string.category_quicksettings_title,
                        R.string.quicksetting_summary,
                        "com.afterlife.afterlab.QuickSettings"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_battery,
                        R.string.category_battery_title,
                        R.string.battery_summary,
                        "com.afterlife.afterlab.Battery"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_ls,
                        R.string.category_lockscreen_title,
                        R.string.lockscreen_summary,
                        "com.afterlife.afterlab.LockScreen"));
                // System Features
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_notif,
                        R.string.category_notifications_title,
                        R.string.notification_summary,
                        "com.afterlife.afterlab.Notifications"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_buttons,
                        R.string.category_buttons_title,
                        R.string.button_summary,
                        "com.afterlife.afterlab.Buttons"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_gesture,
                        R.string.category_gestures_title,
                        R.string.gestures_summary,
                        "com.afterlife.afterlab.Gesture"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_power,
                        R.string.category_powermenu_title,
                        R.string.power_summary,
                        "com.afterlife.afterlab.PowerMenu"));
                // Misc and General
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_misc,
                        R.string.category_misc_title,
                        R.string.misc_summary,
                        "com.afterlife.afterlab.Misc"));
                items.add(new QuickSettingsAdapter.CardItem(
                        R.drawable.ic_interface_system,
                        R.string.category_system_title,
                        R.string.system_summary,
                        "com.afterlife.afterlab.System"));

                rv.setAdapter(new QuickSettingsAdapter(getContext(), items, getMetricsCategory()));
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
