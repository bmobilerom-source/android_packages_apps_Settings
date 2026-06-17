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

public class SecurityPrivacyGrid extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    /**
     * BMobile Security Grid — portable card dashboard.
     * Launch: Settings → Security, or
     * {@code adb shell am start -a com.android.settings.SECURITY_GRID_SETTINGS}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            addPreferencesFromResource(R.xml.securitygrid_settings);
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
            
            androidx.preference.Preference layoutPref = screen.findPreference("securitygrid_recycler_pref");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            androidx.recyclerview.widget.RecyclerView rv =
                    lp.findViewById(R.id.securitygrid_recycler);
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
            
            // Row 1: Security Header (wide, spans 2 columns, non-clickable)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SECURITY_HEADER,
                    R.string.securitygrid_header_android_version, // Title not used, but required
                    R.string.securitygrid_header_android_version_summary, // Summary not used, but required
                    null, // No destination - this is just a display card
                    null));
            
            // Row 2: Fingerprint (left), empty space (right - was LockScreen)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.securitygrid_fingerprint_title,
                    R.string.securitygrid_fingerprint_summary,
                    "com.android.settings.biometrics.fingerprint.FingerprintSettings$FingerprintSettingsFragment",
                    null));
            
            // Row 3: Pocket Mode (left), Special Access (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.securitygrid_pocket_mode_title,
                    R.string.securitygrid_pocket_mode_summary,
                    "com.bmobile.fragments.PocketModeSettings",
                    null));
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_BUTTON,
                    R.string.securitygrid_special_access_title,
                    R.string.securitygrid_special_access_summary,
                    "com.android.settings.applications.specialaccess.SpecialAccessSettings",
                    null));
            
            // Row 4: Disable QS (left, toggle), empty space (right - Time Display removed)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_BUTTON,
                    R.string.securitygrid_disable_qs_title,
                    R.string.securitygrid_disable_qs_summary,
                    null, // No destination fragment for toggle
                    null, // No icon
                    true, // Is toggle
                    "secure_lockscreen_qs_disabled")); // Toggle key
            
            // Row 5: Lockscreen (wide, spans 2 columns)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_WIDE,
                    R.string.securitygrid_lockscreen_title,
                    R.string.securitygrid_lockscreen_summary,
                    "com.android.settings.security.LockscreenDashboardFragment",
                    null));
            
            // Row 6: Timeout (left), Disable SAF (right, toggle)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.securitygrid_timeout_title,
                    R.string.securitygrid_timeout_summary,
                    "com.android.settings.display.ScreenTimeoutSettings",
                    null)); // No icon
            
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.securitygrid_disable_saf_title,
                    R.string.securitygrid_disable_saf_summary,
                    null, // No destination fragment for toggle
                    null, // No icon
                    true, // Is toggle
                    "no_storage_restrict")); // Toggle key
            
            // Row 7: Window Ignore Secure (left, toggle), Miscellaneous (right)
            items.add(new SecurityPrivacyGridAdapter.CardItem(
                    SecurityPrivacyGridAdapter.CARD_TYPE_SMALL,
                    R.string.securitygrid_window_ignore_secure_title,
                    R.string.securitygrid_window_ignore_secure_summary,
                    null, // No destination fragment for toggle
                    null, // No icon
                    true, // Is toggle
                    "window_ignore_secure")); // Toggle key
            

            // REMOVED: Cell Security card

            SecurityPrivacyGridAdapter adapter = new SecurityPrivacyGridAdapter(activity, items, getMetricsCategory());
            rv.setAdapter(adapter);
            
            // Initialize SecurityInfoHeaderController for the security header card
            // Wait for the RecyclerView to layout, then find and initialize the header
            rv.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Find the security header view in the RecyclerView (first item, position 0)
                        androidx.recyclerview.widget.RecyclerView.ViewHolder vh = 
                            rv.findViewHolderForAdapterPosition(0);
                        if (vh != null && vh.itemView != null) {
                            // The included layout should be directly in the itemView
                            android.view.View headerView = vh.itemView.findViewById(R.id.securitygrid_header_content);
                            if (headerView == null) {
                                // Try finding the container from xd_about_phone_header
                                headerView = vh.itemView.findViewById(R.id.securitygrid_header_container);
                            }
                            if (headerView != null) {
                                // Initialize SecurityInfoHeaderController to populate the header
                                com.bmobile.fragments.SecurityInfoHeaderController headerController = 
                                    new com.bmobile.fragments.SecurityInfoHeaderController(getContext());
                                // The controller will populate the TextViews in the header layout
                                headerController.updateHeaderView(headerView);
                            } else {
                                android.util.Log.w("SecurityPrivacyGrid", "Security header view not found in RecyclerView");
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SecurityPrivacyGrid", "Error initializing security header", e);
                    }
                }
            });
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
