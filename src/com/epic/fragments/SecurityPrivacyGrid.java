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

public class SecurityPrivacyGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.anatolia_settings_security_privacy_grid);
        } catch (Exception e) {
            android.util.Log.e("SecurityPrivacyGrid", "Error in onCreate", e);
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
            
            androidx.preference.Preference layoutPref = screen.findPreference("security_privacy_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.security_privacy_grid_recycler);
            if (rv == null) {
                return;
            }
            
            Context context = getContext();
            if (context == null) {
                return;
            }
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            java.util.List<SecurityPrivacyGridAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Grid layout matching image exactly:
            // Row 1: Monet Color (left), LockScreen (right - tall, spans 2 rows visually)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_MONET_COLOR,
                    R.string.security_privacy_grid_monet_color_title,
                    R.string.security_privacy_grid_monet_color_summary,
                    "com.android.settings.display.MonetColorSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_LOCKSCREEN,
                    R.string.security_privacy_grid_lockscreen_title,
                    R.string.security_privacy_grid_lockscreen_summary,
                    "com.epic.fragments.LockScreenSettings",
                    null));
            
            // Row 2: Wallpapers (left), LockScreen continues (right - already added, just tall)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_WALLPAPERS,
                    R.string.security_privacy_grid_wallpapers_title,
                    R.string.security_privacy_grid_wallpapers_summary,
                    "com.android.settings.display.WallpaperSettings",
                    null));
            
            // Row 3: Theme Packs (left), QS Panel (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.security_privacy_grid_theme_packs_title,
                    R.string.security_privacy_grid_theme_packs_summary,
                    "com.epic.fragments.ThemePacksSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_BUTTON,
                    R.string.security_privacy_grid_qs_panel_title,
                    R.string.security_privacy_grid_qs_panel_summary,
                    "com.epic.fragments.QuickSettings",
                    null));
            
            // Row 4: Statusbar (left), Time Display (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_BUTTON,
                    R.string.security_privacy_grid_statusbar_title,
                    R.string.security_privacy_grid_statusbar_summary,
                    "com.epic.fragments.StatusBarSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_TIME_DISPLAY,
                    R.string.security_privacy_grid_time_display_title,
                    R.string.security_privacy_grid_time_display_summary,
                    "com.android.settings.datetime.DateTimeSettings",
                    null));
            
            // Row 5: AOD Customizations (wide, spans 2 columns)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_WIDE,
                    R.string.security_privacy_grid_aod_title,
                    R.string.security_privacy_grid_aod_summary,
                    "com.android.settings.display.AmbientDisplaySettings",
                    null));
            
            // Row 6: Buttons (left), Powermenu (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_buttons_title,
                    R.string.security_privacy_grid_buttons_summary,
                    "com.epic.fragments.ButtonSettings",
                    R.drawable.ic_button));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_powermenu_title,
                    R.string.security_privacy_grid_powermenu_summary,
                    "com.epic.fragments.PowerMenuSettings",
                    R.drawable.ic_powermenu));
            
            // Row 7: Navigation (left), Miscellaneous (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_navigation_title,
                    R.string.security_privacy_grid_navigation_summary,
                    "com.epic.fragments.NavbarSettings",
                    R.drawable.ic_navigation));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_miscellaneous_title,
                    R.string.security_privacy_grid_miscellaneous_summary,
                    "com.epic.fragments.ExtraSettings",
                    R.drawable.ic_misc));
            
            // Row 8: Gestures (left), Notification (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_gestures_title,
                    R.string.security_privacy_grid_gestures_summary,
                    "com.epic.fragments.GestureSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_notification_title,
                    R.string.security_privacy_grid_notification_summary,
                    "com.epic.fragments.NotificationSettings",
                    R.drawable.ic_notification));
            
            // Row 9: About Us (left), Team (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_about_us_title,
                    R.string.security_privacy_grid_about_us_summary,
                    "com.epic.fragments.AboutUsSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.security_privacy_grid_team_title,
                    R.string.security_privacy_grid_team_summary,
                    "com.epic.fragments.AboutUsSettings",
                    R.drawable.ic_team));

            rv.setAdapter(new SecurityPrivacyGridAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("SecurityPrivacyGrid", "Error setting up SecurityPrivacyGrid", e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
            }
        } catch (Exception e) {
            android.util.Log.e("SecurityPrivacyGrid", "Error in onPreferenceChange", e);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
