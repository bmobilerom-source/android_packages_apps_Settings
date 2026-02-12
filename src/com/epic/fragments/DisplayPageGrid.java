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
import android.view.ViewGroup;

public class DisplayPageGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.display_page_grid);
        } catch (Exception e) {
            android.util.Log.e("DisplayPageGrid", "Error in onCreate", e);
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
            
            androidx.preference.Preference layoutPref = screen.findPreference("display_page_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.display_page_grid_recycler);
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

            java.util.List<DisplayPageGridAdapter.CardItem> items = new java.util.ArrayList<>();

            // Reordered according to user's specification:

            // 1. Dark mode
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_MONET_COLOR,
                    R.string.dark_ui_mode,
                    R.string.dark_ui_mode_summary,
                    "com.android.settings.display.darkmode.DarkModeSettingsFragment",
                    null));

            // 2. Wallpaper
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_LOCKSCREEN,
                    R.string.wallpaper_settings_title,
                    R.string.wallpaper_dashboard_summary,
                    "com.android.settings.display.WallpaperSettings",
                    null));

            // 3. Display text
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.display_text_title,
                    R.string.accessibility_text_reading_options_summary,
                    "com.android.settings.accessibility.TextReadingPreferenceFragment",
                    null));

            // 4. Night light
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_STANDARD,
                    R.string.night_display_title,
                    R.string.night_display_text,
                    "com.android.settings.display.NightDisplaySettings",
                    null));

            // 5. Auto rotate
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_STANDARD,
                    R.string.accelerometer_title,
                    R.string.auto_rotate_settings_primary_switch_title,
                    "auto_rotate", // Special key for auto rotate
                    null));

            // 6. Lockscreen (AOD Customizations)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_WIDE,
                    R.string.ambient_display_screen_title,
                    R.string.ambient_display_category_triggers,
                    "ambient_display", // Special key for ambient display
                    null));

            // 7. Rename QS Header
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.qs_header_title, // Renamed QS Header
                    R.string.qs_header_summary,
                    "com.android.settings.awaken.fragments.QsHeader",
                    null));

            // 8. Animation
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_color_settings_title, // Animation
                    R.string.display_customizations_summary,
                    "com.android.settings.awaken.fragments.DisplayCustomizations3",
                    null));

            // 9. B color (Brightness color)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.accessibility_text_reading_options_title, // B color / Text reading options
                    R.string.accessibility_text_reading_options_summary,
                    "com.android.settings.display.MonetColorSettings",
                    null));

            // 10. Rename to dashboard
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.dashboard_style_title, // Dashboard Style
                    R.string.dashboard_style_summary,
                    "com.epic.fragments.DashboardStyleSettings",
                    null));

            // 11. Colors
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.monet_color_settings_title,
                    R.string.monet_color_settings_summary,
                    "com.android.settings.display.MonetColorSettings",
                    null));

            // 12. Rename to timeout and launch screen timeout settings
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.screen_timeout, // Screen timeout
                    R.string.screen_timeout_summary,
                    "com.android.settings.display.ScreenTimeoutSettings",
                    null));

            // 13. Smart pixels
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.smart_pixels_title,
                    R.string.smart_pixels_summary,
                    "com.android.settings.awaken.fragments.SmartPixels",
                    null));

            // 14. Rename to image edit
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.image_toolbox_title, // Image Toolbox
                    R.string.image_toolbox_summary,
                    "image_toolbox",
                    null));

            // 15. Statusbar
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_grid_status_bar_title, // Status Bar
                    R.string.display_grid_status_bar_summary,
                    "com.epic.fragments.StatusBarSettings",
                    null));

            rv.setAdapter(new DisplayPageGridAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("DisplayPageGrid", "Error setting up DisplayPageGrid", e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
            }
        } catch (Exception e) {
            android.util.Log.e("DisplayPageGrid", "Error in onPreferenceChange", e);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

