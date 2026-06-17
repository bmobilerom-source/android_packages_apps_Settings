/*
 * Copyright (C) 2025 BashaMobile
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
 */

package com.bmobile.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.ContentResolver;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SystemGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.anatolia_settings_system_grid);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }

        androidx.preference.Preference layoutPref = screen.findPreference("system_grid");
        if (!(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
            return;
        }

        com.android.settingslib.widget.LayoutPreference lp =
                (com.android.settingslib.widget.LayoutPreference) layoutPref;
        androidx.recyclerview.widget.RecyclerView rv =
                lp.findViewById(R.id.system_grid_recycler);
        if (rv == null) {
            return;
        }

        rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                getContext(), 2));

        java.util.List<SystemGridAdapter.CardItem> items = new java.util.ArrayList<>();
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.gesture_preference_title,
                R.string.system_grid_gesture_summary,
                "com.android.settings.gestures.GestureSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.system_navigation_title,
                R.string.system_grid_navigation_summary,
                "com.android.settings.gestures.SystemNavigationGestureSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.power_usage_summary_title,
                R.string.system_grid_power_usage_summary,
                "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.languages_settings,
                R.string.languages_setting_summary,
                "com.android.settings.language.LanguageSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.date_and_time,
                R.string.date_and_time_summary,
                "com.android.settings.datetime.DateTimeSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.category_buttons_title,
                R.string.button_summary,
                "org.lineageos.lineageparts.input.ButtonSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.statusbar_title,
                R.string.system_grid_statusbar_summary,
                "org.lineageos.lineageparts.statusbar.StatusBarSettings"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.keyboard_settings,
                R.string.keyboard_settings_summary,
                "com.android.settings.inputmethod.KeyboardSettings"));
        items.add(new SystemGridAdapter.CardItem(
                R.drawable.ic_settings_backup,
                R.string.backup_transport_title,
                R.string.system_grid_backup_transport_summary,
                "com.android.settings.backup.UserBackupSettingsActivity"));
        items.add(new SystemGridAdapter.CardItem(
                R.drawable.ic_emergency_gesture_24dp,
                R.string.emergency_settings_preference_title,
                R.string.system_grid_emergency_summary,
                "com.android.settings.emergency.EmergencyDashboardFragment"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.about_us_info,
                R.string.about_us_info_summary,
                "com.bmobile.fragments.SystemSettingsAboutus"));
        items.add(new SystemGridAdapter.CardItem(
                0,
                R.string.reset_dashboard_title,
                R.string.system_reset_summary,
                "com.android.settings.system.ResetDashboardFragment"));

        rv.setAdapter(new SystemGridAdapter(getContext(), items, getMetricsCategory()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
