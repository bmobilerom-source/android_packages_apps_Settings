/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import lineageos.providers.LineageSettings;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarSettings extends DashboardFragment {
    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_LOGO = "status_bar_logo";
    private static final String KEY_STATUS_BAR_LOGO_POSITION = "status_bar_logo_position";
    private static final String KEY_STATUS_BAR_LOGO_STYLE = "status_bar_logo_style";

    private androidx.preference.SwitchPreference mStatusBarLogo;
    private androidx.preference.ListPreference mStatusBarLogoPosition;
    private androidx.preference.ListPreference mStatusBarLogoStyle;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.status_bar_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getContext().getContentResolver();

        mStatusBarLogo = findPreference(KEY_STATUS_BAR_LOGO);
        mStatusBarLogoPosition = findPreference(KEY_STATUS_BAR_LOGO_POSITION);
        mStatusBarLogoStyle = findPreference(KEY_STATUS_BAR_LOGO_STYLE);

        if (mStatusBarLogo != null) {
            int enabled = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_LOGO, 0);
            mStatusBarLogo.setChecked(enabled != 0);
            mStatusBarLogo.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_LOGO, value ? 1 : 0);
                return true;
            });
        }

        if (mStatusBarLogoPosition != null) {
            int pos = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_LOGO_POSITION, 0);
            mStatusBarLogoPosition.setValue(String.valueOf(pos));
            mStatusBarLogoPosition.setSummary(mStatusBarLogoPosition.getEntry());
            mStatusBarLogoPosition.setOnPreferenceChangeListener((preference, newValue) -> {
                int val = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_LOGO_POSITION, val);
                mStatusBarLogoPosition.setValue((String) newValue);
                mStatusBarLogoPosition.setSummary(mStatusBarLogoPosition.getEntries()
                        [mStatusBarLogoPosition.findIndexOfValue((String) newValue)]);
                return true;
            });
        }

        if (mStatusBarLogoStyle != null) {
            int style = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_LOGO_STYLE, 0);
            mStatusBarLogoStyle.setValue(String.valueOf(style));
            int idx = mStatusBarLogoStyle.findIndexOfValue(String.valueOf(style));
            if (idx >= 0) {
                mStatusBarLogoStyle.setSummary(mStatusBarStyleSummary(idx));
            }
            mStatusBarLogoStyle.setOnPreferenceChangeListener((preference, newValue) -> {
                int val = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_LOGO_STYLE, val);
                mStatusBarLogoStyle.setValue((String) newValue);
                int index = mStatusBarLogoStyle.findIndexOfValue((String) newValue);
                mStatusBarLogoStyle.setSummary(mStatusBarStyleSummary(index));
                return true;
            });
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        return controllers;
    }

    private CharSequence mStatusBarStyleSummary(int index) {
        CharSequence[] entries =
                getContext().getResources().getTextArray(R.array.status_bar_logo_style_entries);
        if (index >= 0 && index < entries.length) {
            return entries[index];
        }
        return "";
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.status_bar_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "status_bar_settings";
    }
}
