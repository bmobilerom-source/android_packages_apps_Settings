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
        addPreferencesFromResource(R.xml.security_grid);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PreferenceScreen screen = getPreferenceScreen();
        androidx.preference.Preference layoutPref = screen.findPreference("security_grid");
        if (layoutPref instanceof com.android.settingslib.widget.LayoutPreference) {
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.security_grid_recycler);
            if (rv != null) {
                rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                        getContext(), 2));

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
                        "com.android.settings.security.LockScreenSettings",
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
                        "com.android.settings.display.ThemePacksSettings",
                        null));
                
                // Row 3: Statusbar and QS Panel
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_STANDARD,
                        R.string.security_grid_statusbar_title,
                        R.string.security_grid_statusbar_summary,
                        "org.lineageos.lineageparts.statusbar.StatusBarSettings",
                        R.drawable.ic_settings_statusbar));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_STANDARD,
                        R.string.security_grid_qs_panel_title,
                        R.string.security_grid_qs_panel_summary,
                        "org.lineageos.lineageparts.quicksettings.QuickSettingsSettings",
                        R.drawable.ic_interface_qs));
                
                // Row 4: AOD Customizations (wide)
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_WIDE,
                        R.string.security_grid_aod_title,
                        R.string.security_grid_aod_summary,
                        "com.android.settings.display.AODSettings",
                        null));
                
                // Row 5: Bottom row - 6 small cards
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_buttons_title,
                        R.string.security_grid_buttons_summary,
                        "org.lineageos.lineageparts.input.ButtonSettings",
                        R.drawable.ic_anatolia_buttons));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_powermenu_title,
                        R.string.security_grid_powermenu_summary,
                        "org.lineageos.lineageparts.powermenu.PowerMenuSettings",
                        R.drawable.ic_anatolia_powermenu));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_notification_title,
                        R.string.security_grid_notification_summary,
                        "org.lineageos.lineageparts.notifications.NotificationSettings",
                        R.drawable.ic_anatolia_notifications));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_navigation_title,
                        R.string.security_grid_navigation_summary,
                        "org.lineageos.lineageparts.navigation.NavigationSettings",
                        R.drawable.ic_anatolia_navbar));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_miscellaneous_title,
                        R.string.security_grid_miscellaneous_summary,
                        "com.epic.fragments.MiscellaneousSettings",
                        R.drawable.ic_anatolia_extras));
                items.add(new SecurityGridAdapter.CardItem(
                        SecurityGridAdapter.CARD_TYPE_SMALL,
                        R.string.security_grid_team_title,
                        R.string.security_grid_team_summary,
                        "com.epic.fragments.TeamSettings",
                        R.drawable.ic_team));

                rv.setAdapter(new SecurityGridAdapter(getContext(), items, getMetricsCategory()));
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

