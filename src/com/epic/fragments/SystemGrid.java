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
import android.view.ViewGroup;

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
        
        // Ensure custom theme backgrounds are applied
        ensureThemeBackgrounds();
        
        PreferenceScreen screen = getPreferenceScreen();
        androidx.preference.Preference layoutPref = screen.findPreference("system_grid");
        if (layoutPref instanceof com.android.settingslib.widget.LayoutPreference) {
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.system_grid_recycler);
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                        getContext(), 2));

                java.util.List<SystemGridAdapter.CardItem> items = new java.util.ArrayList<>();
                // Base UI Theme
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_sb,
                        R.string.gesture_preference_title,
                        R.string.system_grid_gesture_summary,
                        "com.android.settings.gestures.GestureSettings"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_qs,
                        R.string.system_navigation_title,
                        R.string.system_grid_navigation_summary,
                        "com.android.settings.gestures.SystemNavigationGestureSettings"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_battery,
                        R.string.power_usage_summary_title,
                        R.string.system_grid_power_usage_summary,
                        "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_ls,
                        R.string.languages_settings,
                        R.string.languages_setting_summary,
                        "com.android.settings.language.LanguageSettings"));
                // System Features
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_misc,
                        R.string.date_and_time,
                        R.string.date_and_time_summary,
                        "com.android.settings.datetime.DateTimeSettings"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_buttons,
                        R.string.category_buttons_title,
                        R.string.button_summary,
                        "org.lineageos.lineageparts.input.ButtonSettings"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_settings_statusbar,
                        R.string.statusbar_title,
                        R.string.system_grid_statusbar_summary,
                        "org.lineageos.lineageparts.statusbar.StatusBarSettings"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_power,
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
                // Misc and General
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_misc,
                        R.string.aboutus_title,
                        R.string.aboutus_summary,
                        "com.epic.fragments.SystemSettingsAboutus"));
                items.add(new SystemGridAdapter.CardItem(
                        R.drawable.ic_interface_system,
                        R.string.reset_dashboard_title,
                        R.string.system_reset_summary,
                        "com.android.settings.system.ResetDashboardFragment"));

                rv.setAdapter(new SystemGridAdapter(getContext(), items, getMetricsCategory()));
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    /**
     * Ensures custom theme backgrounds are applied to this fragment.
     */
    private void ensureThemeBackgrounds() {
        try {
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            View rootView = activity.findViewById(android.R.id.content);
            if (rootView instanceof ViewGroup) {
                ViewGroup rootGroup = (ViewGroup) rootView;
                if (rootGroup.findViewById(R.id.theme_background) != null) {
                    return;
                }
                
                com.android.settings.preferences.ui.AdaptiveThemeBackgroundView themeView =
                        new com.android.settings.preferences.ui.AdaptiveThemeBackgroundView(activity);
                themeView.setId(R.id.theme_background);
                themeView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                int insertIndex = (rootGroup.findViewById(R.id.wallpaper_background) != null) ? 1 : 0;
                rootGroup.addView(themeView, insertIndex, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } catch (Exception e) {
            android.util.Log.e("SystemGrid", "Error adding theme background", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

