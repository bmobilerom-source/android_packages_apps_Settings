/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.bmobile.fragments;

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

public class KidsSafeBasicDefaults extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.kidssafe_basic_defaults);
        } catch (Exception e) {
            android.util.Log.e("SystemBasicDefaults", "Error in onCreate", e);
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
            
            androidx.preference.Preference layoutPref = screen.findPreference("system_basic_defaults");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.system_basic_defaults_recycler);
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
            // - Large left card spans full width (3 columns)
            // - Default Apps card spans full width below it
            // - Circular buttons take 1 column each -> 3 per row
            androidx.recyclerview.widget.GridLayoutManager layoutManager =
                    new androidx.recyclerview.widget.GridLayoutManager(context, 3);
            rv.setLayoutManager(layoutManager);

            java.util.List<SystemBasicDefaultsAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Large left card - Aurora Store (biggest card)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_LARGE_LEFT,
                    R.string.system_basic_defaults_aurora_store_title,
                    R.string.system_basic_defaults_aurora_store_summary,
                    null,
                    "aurora_store", // Special key for intent launch
                    "Aurora Store"));
            
            // Second card - Default Apps
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_ABOUT_US,
                    R.string.system_basic_defaults_default_apps_title,
                    R.string.system_basic_defaults_default_apps_summary,
                    R.drawable.ic_system_basic_defaults_default_apps,
                    "default_apps")); // Special key for intent launch
            
            // Circular buttons (6 buttons)
            // 1. Gesture Navigation - Now first button
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.system_basic_defaults_gesture_navigation_title,
                    R.string.system_basic_defaults_gesture_navigation_summary,
                    R.drawable.ic_system_basic_defaults_gesture,
                    "com.android.settings.gestures.SystemNavigationGestureSettings"));

            // 2. WiFi Hotspot (from tether_prefs.xml 34-39)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.wifi_hotspot_checkbox_text,
                    R.string.wifi_hotspot_off_subtext,
                    R.drawable.ic_system_basic_defaults_one_handed,
                    "com.android.settings.wifi.tether.WifiTetherSettings"));

            // 3. Media Controls (from sound_settings.xml 162-170)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.media_controls_title,
                    R.string.keywords_media_controls,
                    R.drawable.ic_system_basic_defaults_brightness,
                    "com.android.settings.sound.MediaControlsSettings"));

            // 4. Dark Theme
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.dark_ui_mode,
                    R.string.dark_ui_mode_summary,
                    R.drawable.ic_system_basic_defaults_dark_theme,
                    "com.android.settings.display.darkmode.DarkModeSettingsFragment"));

            // 5. PhotoWidget App (package: com.fibelatti.photowidget, class: com.fibelatti.photowidget.home.HomeActivity)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.system_basic_defaults_photowidget_title,
                    R.string.system_basic_defaults_photowidget_summary,
                    R.drawable.ic_system_basic_defaults_anatolia,
                    "photowidget_app")); // Special key for PhotoWidget intent

            // 6. TapTap Settings (from gesture_navigation_settings.xml 40-46)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.taptap_title,
                    R.string.taptap_summary,
                    R.drawable.ic_system_basic_defaults_more,
                    "com.android.settings.gestures.TapScreenGestureSettings"));

            rv.setAdapter(new SystemBasicDefaultsAdapter(activity, items, getMetricsCategory()));
        } catch (Exception e) {
            android.util.Log.e("SystemBasicDefaults", "Error setting up SystemBasicDefaults", e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
            }
        } catch (Exception e) {
            android.util.Log.e("SystemBasicDefaults", "Error in onPreferenceChange", e);
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

