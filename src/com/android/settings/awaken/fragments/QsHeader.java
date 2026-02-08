/*
 * Copyright (C) 2025 LineageOS
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
package com.android.settings.awaken.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QsHeader extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QsHeader";

    private static final String KEY_QS_HEADER_PROVIDER = "qs_header_provider";
    private static final String KEY_QS_HEADER_IMAGE_GALLERY = "qs_header_image_gallery";
    private static final String KEY_QS_HEADER_DAYLIGHT_PACK = "qs_header_daylight_pack";

    private ListPreference mHeaderProvider;
    private Preference mHeaderImageGallery;
    private ListPreference mDaylightPack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_header);

        final ContentResolver resolver = requireActivity().getContentResolver();

        mHeaderProvider = findPreference(KEY_QS_HEADER_PROVIDER);
        if (mHeaderProvider != null) {
            mHeaderProvider.setOnPreferenceChangeListener(this);
            String provider = Settings.System.getStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, UserHandle.USER_CURRENT);
            if (provider == null || provider.isEmpty()) {
                provider = "static";
            }
            mHeaderProvider.setValue(provider);
            updateProviderSummary(provider);
        }

        mHeaderImageGallery = findPreference(KEY_QS_HEADER_IMAGE_GALLERY);
        if (mHeaderImageGallery != null) {
            mHeaderImageGallery.setOnPreferenceClickListener(pref -> {
                startActivity(new android.content.Intent(requireContext(),
                        com.android.settings.display.HeaderImageGalleryActivity.class));
                return true;
            });
        }

        mDaylightPack = findPreference(KEY_QS_HEADER_DAYLIGHT_PACK);
        if (mDaylightPack != null) {
            mDaylightPack.setOnPreferenceChangeListener(this);
            refreshDaylightPackEntries();
        }


        final String provider = mHeaderProvider != null ? mHeaderProvider.getValue() : "static";
        updateProviderDependentPreferences(provider);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = requireActivity().getContentResolver();
        if (preference == mHeaderProvider) {
            final String provider = (String) newValue;
            // Ensure the feature is enabled if user is configuring providers.
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER,
                    1, UserHandle.USER_CURRENT);
            final boolean success = Settings.System.putStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER,
                    provider, UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                        null, false);
                updateProviderSummary(provider);
                updateProviderDependentPreferences(provider);
            }
            return success;
        }
        if (preference == mDaylightPack) {
            final String value = (String) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER,
                    1, UserHandle.USER_CURRENT);
            final boolean success = Settings.System.putStringForUser(resolver,
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK,
                    value != null && !value.isEmpty() ? value : null,
                    UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK),
                        null, false);
                updateDaylightPackSummary(value);
            }
            return success;
        }
        return false;
    }

    private void updateProviderSummary(String provider) {
        if (mHeaderProvider == null) {
            return;
        }
        final String[] entries = getResources().getStringArray(R.array.custom_header_provider_entries);
        final String[] values = getResources().getStringArray(R.array.custom_header_provider_values);
        for (int i = 0; i < values.length; i++) {
            if (provider.equals(values[i])) {
                mHeaderProvider.setSummary(entries[i]);
                return;
            }
        }
    }

    private void updateProviderDependentPreferences(String provider) {
        final boolean isStatic = "static".equals(provider);
        final boolean isDaylight = "daylight".equals(provider);

        if (mHeaderImageGallery != null) {
            mHeaderImageGallery.setVisible(isStatic);
        }
        if (mDaylightPack != null) {
            mDaylightPack.setVisible(isDaylight);
        }
    }

    private void refreshDaylightPackEntries() {
        if (mDaylightPack == null) {
            return;
        }

        final List<String> entries = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        entries.add(getString(R.string.qs_header_daylight_pack_default));
        values.add("");

        final PackageManager pm = requireActivity().getPackageManager();
        final List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo app : apps) {
            if (app == null || app.packageName == null) {
                continue;
            }
            try {
                final Resources res = pm.getResourcesForApplication(app.packageName);
                try (InputStream in = res.getAssets().open("daylight_header.xml")) {
                    // If we can open the asset, it looks like a valid pack.
                    entries.add(pm.getApplicationLabel(app).toString());
                    values.add(app.packageName);
                }
            } catch (Exception ignored) {
                // Not a header pack.
            }
        }

        mDaylightPack.setEntries(entries.toArray(new CharSequence[0]));
        mDaylightPack.setEntryValues(values.toArray(new CharSequence[0]));

        final String currentValue = Settings.System.getStringForUser(
                requireActivity().getContentResolver(),
                Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK,
                UserHandle.USER_CURRENT);
        final String normalized = currentValue != null ? currentValue : "";
        mDaylightPack.setValue(normalized);
        updateDaylightPackSummary(normalized);
    }

    private void updateDaylightPackSummary(String value) {
        if (mDaylightPack == null) {
            return;
        }
        final CharSequence entry = mDaylightPack.getEntry();
        if (entry != null) {
            mDaylightPack.setSummary(entry);
            return;
        }
        mDaylightPack.setSummary(R.string.qs_header_daylight_pack_summary);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.qs_header;
                    result.add(sir);
                    return result;
                }
            };
}

