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

package com.bmobile.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.ContentResolver;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

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

            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            androidx.recyclerview.widget.GridLayoutManager layoutManager =
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            java.util.List<DisplayPageGridAdapter.CardItem> items = new java.util.ArrayList<>();

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_MONET_COLOR,
                    R.string.dark_ui_mode,
                    R.string.dark_ui_mode_summary,
                    "com.android.settings.display.darkmode.DarkModeSettingsFragment",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_LOCKSCREEN,
                    R.string.wallpaper_settings_title,
                    R.string.wallpaper_dashboard_summary,
                    "com.android.settings.display.WallpaperSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.display_text_title,
                    R.string.accessibility_text_reading_options_suggestion_title,
                    "com.android.settings.accessibility.TextReadingPreferenceFragment",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_STANDARD,
                    R.string.night_display_title,
                    R.string.night_display_text,
                    "com.android.settings.display.NightDisplaySettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_WIDE,
                    R.string.dynamic_island_settings_title,
                    R.string.dynamic_island_settings_summary,
                    "dynamic_island",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.qs_header_title,
                    R.string.qs_header_summary,
                    "com.bmobile.fragments.QsHeader",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_customization_title,
                    R.string.display_customization_summary,
                    "com.bmobile.fragments.DisplayCustomizationsSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_grid_demo_settings_title,
                    R.string.accessibility_text_reading_options_suggestion_title,
                    "com.android.settings.display.MonetColorSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.dashboard_style_title,
                    R.string.dashboard_style_summary,
                    "com.bmobile.fragments.CustomDashboardSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.monet_color_settings_title,
                    R.string.monet_color_settings_summary,
                    "com.android.settings.display.MonetColorSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.screen_timeout,
                    R.string.screen_timeout_summary,
                    "com.android.settings.display.ScreenTimeoutSettings",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.smart_pixels_title,
                    R.string.smart_pixels_summary,
                    "com.bmobile.fragments.SmartPixels",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.image_toolbox_title,
                    R.string.image_toolbox_summary,
                    "image_toolbox",
                    null));

            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_grid_status_bar_title,
                    R.string.display_grid_status_bar_summary,
                    "com.android.settings.display.StatusBarSettings",
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
