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
            
            // Row 1: Dark mode (Monet/Dark UI) and Wallpapers (tall with live wallpaper preview)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_MONET_COLOR,
                    R.string.dark_ui_mode,
                    R.string.summary_placeholder,
                    "com.android.settings.display.darkmode.DarkModeSettingsFragment",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_LOCKSCREEN,
                    R.string.wallpaper_settings_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.WallpaperSettings",
                    null));
            
            // Row 2: Wallpapers and Text reading options
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.wallpaper_settings_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.WallpaperSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_THEME_PACKS,
                    R.string.accessibility_text_reading_options_title,
                    R.string.summary_placeholder,
                    "com.android.settings.accessibility.TextReadingPreferenceFragment",
                    null));
            
            // Row 3: Night display and Auto-rotate (device state based)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.night_display_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.NightDisplaySettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_STANDARD,
                    R.string.accelerometer_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.DeviceStateAutoRotateDetailsFragment",
                    null));
            
            // Row 4: AOD Customizations (wide)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_WIDE,
                    R.string.ambient_display_screen_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.AmbientDisplaySettings",
                    null));
            
            // Row 5: Bottom row - 6 small cards mapped to display settings
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.color_mode_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.ColorModePreferenceFragment",
                    null));
            // Additional Anatolia small cards (safe destinations, safe strings)
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.anatolia_settings_title,
                    R.string.summary_placeholder,
                    "com.epic.fragments.ButtonSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.anatolia_settings_title,
                    R.string.summary_placeholder,
                    "com.epic.fragments.PowerMenuSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.anatolia_settings_title,
                    R.string.summary_placeholder,
                    "com.epic.fragments.NavbarSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.anatolia_settings_title,
                    R.string.summary_placeholder,
                    "com.epic.fragments.ExtraSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.accessibility_color_contrast_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.ColorContrastFragment",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.auto_brightness_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.AutoBrightnessSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.screensaver_settings_title,
                    R.string.summary_placeholder,
                    "com.android.settings.dream.DreamSettings",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.even_dimmer_display_title,
                    R.string.even_dimmer_display_summary,
                    "com.android.settings.accessibility.ToggleReduceBrightColorsPreferenceFragment",
                    null));
            items.add(new SecurityGridAdapter.CardItem(
                    SecurityGridAdapter.CARD_TYPE_SMALL,
                    R.string.color_mode_title,
                    R.string.summary_placeholder,
                    "com.android.settings.display.ColorModePreferenceFragment",
                    null));

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

