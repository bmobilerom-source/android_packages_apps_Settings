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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.R;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;


import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.R;
import android.util.Log;
import com.epic.utils.CardNavigationHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class StatusBarSettings extends DashboardFragment {

    private CardNavigationHelper mCardHelper;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // DashboardFragment automatically loads preferences via getPreferenceScreenResId()
        // Do NOT call addPreferencesFromResource() here as it causes double-loading and crashes

        // Initialize card navigation helper
        mCardHelper = new CardNavigationHelper(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup click handling for navigation cards
        mCardHelper.setupCardClickHandling(view);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return "StatusBarSettings";
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.anatolia_settings_statusbar;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        // Controllers are automatically instantiated from XML via settings:controller attribute
        // Do NOT manually add them here as it causes double instantiation and crashes
        // If you need to add controllers programmatically, remove the settings:controller from XML
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.anatolia_settings_statusbar) {
                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context);
                }
            };
}
