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

public class DisplayGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.display_grid);
        } catch (Exception e) {
            android.util.Log.e("DisplayGrid", "Error in onCreate", e);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            
            androidx.preference.Preference layoutPref = screen.findPreference("display_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.display_grid_recycler);
            if (rv == null) {
                return;
            }
            
            Context context = getContext();
            if (context == null) {
                return;
            }
            
            // Ensure we have an Activity context for SubSettingLauncher
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            // Use 3 columns:
            // - Large left card spans 2 columns (visually 2 rows tall via its own height)
            // - Right-side cards each take 1 column stacked vertically
            // - Circular buttons take 1 column each -> 3 per row as in the desired design
            androidx.recyclerview.widget.GridLayoutManager layoutManager =
                    new androidx.recyclerview.widget.GridLayoutManager(context, 3);
            rv.setLayoutManager(layoutManager);

            java.util.List<DisplayGridAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Large left card with Quick Settings button
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_LARGE_LEFT,
                    R.string.display_grid_quick_settings_title,
                    R.string.display_grid_quick_settings_summary,
                    null,
                    "com.epic.fragments.QuickSettings",
                    "Quick settings"));
            
            // About Us card
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_ABOUT_US,
                    R.string.display_grid_about_us_title,
                    R.string.display_grid_about_us_summary,
                    R.drawable.ic_display_grid_about_us,
                    "com.epic.fragments.AboutUsSettings"));
            
            // Circular buttons (6 buttons)
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_lock_title,
                    R.string.display_grid_lock_summary,
                    R.drawable.ic_display_grid_lock,
                    "com.android.settings.security.SecuritySettings"));
            
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_messages_title,
                    R.string.display_grid_messages_summary,
                    R.drawable.ic_display_grid_messages,
                    "com.android.settings.notification.ConfigureNotificationSettings"));
            
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_connection_title,
                    R.string.display_grid_connection_summary,
                    R.drawable.ic_display_grid_connection,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment"));
            
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_edit_title,
                    R.string.display_grid_edit_summary,
                    R.drawable.ic_display_grid_edit,
                    "com.android.settings.display.DisplaySettings"));
            
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_location_title,
                    R.string.display_grid_location_summary,
                    R.drawable.ic_display_grid_location,
                    "com.android.settings.location.LocationSettings"));
            
            items.add(new DisplayGridAdapter.CardItem(
                    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.display_grid_team_title,
                    R.string.display_grid_team_summary,
                    R.drawable.ic_display_grid_team,
                    "com.epic.fragments.AboutUsSettings"));

            rv.setAdapter(new DisplayGridAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("DisplayGrid", "Error setting up DisplayGrid", e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
            }
        } catch (Exception e) {
            android.util.Log.e("DisplayGrid", "Error in onPreferenceChange", e);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

