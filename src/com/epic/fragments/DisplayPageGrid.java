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
            
            // Row 1: Dark mode (Monet/Dark UI) and Wallpapers (tall with live wallpaper preview)
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
            
            // Row 2: Wallpaper Background removed, Text reading options
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.accessibility_text_reading_options_title,
                    R.string.accessibility_text_reading_options_summary,
                    "com.android.settings.accessibility.TextReadingPreferenceFragment",
                    null));
            
            // Row 3: Night display and Auto-rotate
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_STANDARD,
                    R.string.night_display_title,
                    R.string.night_display_text,
                    "com.android.settings.display.NightDisplaySettings",
                    null));
            // Auto-rotate: Try device state based first, fallback handled in adapter
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_STANDARD,
                    R.string.accelerometer_title,
                    R.string.auto_rotate_settings_primary_switch_title,
                    "auto_rotate", // Special key for auto rotate
                    null));
            
            // Row 4: AOD Customizations (wide) - use intent action for LineageOS
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_WIDE,
                    R.string.ambient_display_screen_title,
                    R.string.ambient_display_category_triggers,
                    "ambient_display", // Special key for ambient display
                    null));
            
            // Last 10 cards (ordered as specified):
            // 1. Display customizations (replaces adaptive brightness)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_customizations3_title,
                    R.string.display_customizations_summary,
                    "com.android.settings.awaken.fragments.DisplayCustomizations3",
                    null));
            // 2. Monet Color Settings (replaces extra dim)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.display_page_grid_monet_color_title,
                    R.string.display_page_grid_monet_color_summary,
                    "com.android.settings.display.MonetColorSettings",
                    null));
            // 3. Text and size
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.accessibility_text_reading_options_title,
                    R.string.accessibility_text_reading_options_summary,
                    "com.android.settings.accessibility.TextReadingPreferenceFragment",
                    null));
            // QS Header Image (from display_customizations5.xml)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.qs_header_title,
                    R.string.qs_header_summary,
                    "com.android.settings.awaken.fragments.QsHeader",
                    null));
            // Custom themes removed from ROM
            // 5. Screen saver
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.screensaver_settings_title,
                    R.string.screensaver_settings_when_to_dream,
                    "com.android.settings.dream.DreamSettings",
                    null));
            // 6. Color contrast
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.accessibility_color_contrast_title,
                    R.string.accessibility_color_contrast_summary,
                    "com.android.settings.display.ColorContrastFragment",
                    null));
            // 7. Colors (Color mode)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.color_mode_title,
                    R.string.color_mode_option_automatic,
                    "com.android.settings.display.ColorModePreferenceFragment",
                    null));
            // 8. Live display (dynamically injected - use LineageParts intent)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.color_mode_title, // Use color_mode_title as fallback if livedisplay_title doesn't exist
                    R.string.color_mode_option_natural,
                    "livedisplay", // Special key for live display - will use LineageParts intent
                    null));
            // 9. Custom Dashboard (replaces second Anatolia settings card at bottom)
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.dashboard_style_title,
                    R.string.dashboard_style_summary,
                    "com.epic.fragments.DashboardStyleSettings",
                    null));
            // 10. Color and motion
            items.add(new DisplayPageGridAdapter.CardItem(
                    DisplayPageGridAdapter.CARD_TYPE_SMALL,
                    R.string.accessibility_color_and_motion_title,
                    R.string.accessibility_color_and_motion_summary,
                    "com.android.settings.accessibility.ColorAndMotionFragment",
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

