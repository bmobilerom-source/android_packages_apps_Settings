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

public class SecurityGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.security_grid);
        } catch (Exception e) {
            android.util.Log.e("SecurityGrid", "Error in onCreate", e);
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
            
            androidx.preference.Preference layoutPref = screen.findPreference("security_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.security_grid_recycler);
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
            
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            java.util.List<SecurityGridAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Row 1: Monet Color (wide) and LockScreen (tall)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_MONET_COLOR,
                    R.string.security_grid_monet_color_title,
                    R.string.security_grid_monet_color_summary,
                    "com.android.settings.display.ThemeSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_LOCKSCREEN,
                    R.string.security_grid_lockscreen_title,
                    R.string.security_grid_lockscreen_summary,
                    "com.epic.fragments.LockScreenSettings",
                    null));
            
            // Row 2: Wallpapers and Theme Packs
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.security_grid_wallpapers_title,
                    R.string.security_grid_wallpapers_summary,
                    "com.android.settings.display.WallpaperSettings",
                    R.drawable.ic_wallpaper));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.security_grid_theme_packs_title,
                    R.string.security_grid_theme_packs_summary,
                    "com.epic.fragments.ThemePacksSettings",
                    null));
            
            // Row 3: Statusbar and QS Panel
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.security_grid_statusbar_title,
                    R.string.security_grid_statusbar_summary,
                    "com.epic.fragments.StatusBarSettings",
                    R.drawable.ic_settings_statusbar));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.security_grid_qs_panel_title,
                    R.string.security_grid_qs_panel_summary,
                    "com.epic.fragments.QuickSettings",
                    R.drawable.ic_quick_settings));
            
            // Row 4: AOD Customizations (wide)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_WIDE,
                    R.string.security_grid_aod_title,
                    R.string.security_grid_aod_summary,
                    "com.android.settings.display.AmbientDisplaySettings",
                    R.drawable.ic_aod));
            
            // Row 5: Bottom row - 6 small cards
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_buttons_title,
                    R.string.security_grid_buttons_summary,
                    "com.epic.fragments.ButtonSettings",
                    R.drawable.ic_buttons));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_powermenu_title,
                    R.string.security_grid_powermenu_summary,
                    "com.epic.fragments.PowerMenuSettings",
                    R.drawable.ic_power_menu));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_notification_title,
                    R.string.security_grid_notification_summary,
                    "com.epic.fragments.NotificationSettings",
                    R.drawable.ic_notifications));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_navigation_title,
                    R.string.security_grid_navigation_summary,
                    "com.epic.fragments.NavbarSettings",
                    R.drawable.ic_navigation));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_miscellaneous_title,
                    R.string.security_grid_miscellaneous_summary,
                    "com.epic.fragments.ExtraSettings",
                    R.drawable.ic_miscellaneous));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_grid_team_title,
                    R.string.security_grid_team_summary,
                    "com.epic.fragments.AboutUsSettings",
                    R.drawable.ic_team));

            rv.setAdapter(new SecurityGridAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("SecurityGrid", "Error setting up SecurityGrid", e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
            }
        } catch (Exception e) {
            android.util.Log.e("SecurityGrid", "Error in onPreferenceChange", e);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

