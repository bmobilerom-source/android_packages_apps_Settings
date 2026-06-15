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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.display.HeaderImageGalleryActivity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QsHeader extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_QS_HEADER_ENABLE = "qs_header_enable";
    private static final String KEY_QS_HEADER_IMAGE_GALLERY = "qs_header_image_gallery";
    private static final String KEY_QS_HEADER_HEIGHT = "qs_header_height";
    private static final String GALLERY_PROVIDER = "static";
    private static final int DEFAULT_HEADER_HEIGHT = 142;

    private Preference mHeaderImageGallery;
    private SeekBarPreference mHeaderHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_header);

        final ContentResolver resolver = requireActivity().getContentResolver();
        ensureHeaderEnabled(resolver);

        final SwitchPreferenceCompat headerEnable = findPreference(KEY_QS_HEADER_ENABLE);
        if (headerEnable != null) {
            // Developer: hidden toggle — header remains enabled via ensureHeaderEnabled().
            headerEnable.setVisible(false);
            headerEnable.setChecked(true);
        }

        mHeaderImageGallery = findPreference(KEY_QS_HEADER_IMAGE_GALLERY);
        if (mHeaderImageGallery != null) {
            mHeaderImageGallery.setOnPreferenceClickListener(pref -> {
                ensureHeaderEnabled(requireActivity().getContentResolver());
                requireActivity().startActivity(
                        new Intent(requireActivity(), HeaderImageGalleryActivity.class));
                return true;
            });
        }

        mHeaderHeight = findPreference(KEY_QS_HEADER_HEIGHT);
        if (mHeaderHeight != null) {
            final int height = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT,
                    DEFAULT_HEADER_HEIGHT, UserHandle.USER_CURRENT);
            mHeaderHeight.setProgress(height);
            mHeaderHeight.setOnPreferenceChangeListener(this);
            updateHeaderHeightSummary(height);
        }

        updateGallerySummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ContentResolver resolver = requireActivity().getContentResolver();
        ensureHeaderEnabled(resolver);

        final SwitchPreferenceCompat headerEnable = findPreference(KEY_QS_HEADER_ENABLE);
        if (headerEnable != null) {
            headerEnable.setVisible(false);
            headerEnable.setChecked(true);
        }

        if (mHeaderHeight != null) {
            final int height = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT,
                    DEFAULT_HEADER_HEIGHT, UserHandle.USER_CURRENT);
            mHeaderHeight.setProgress(height);
            updateHeaderHeightSummary(height);
        }
        updateGallerySummary();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = requireActivity().getContentResolver();
        if (preference == mHeaderHeight) {
            final int height = (Integer) newValue;
            final boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT,
                    height, UserHandle.USER_CURRENT);
            if (success) {
                notifyCustomHeaderChanged(resolver);
                resolver.notifyChange(
                        Settings.System.getUriFor(
                                Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT),
                        null, false);
                updateHeaderHeightSummary(height);
            }
            return success;
        }
        return false;
    }

    /** Custom header is always enabled; toggle hidden in UI. */
    private static void ensureHeaderEnabled(ContentResolver resolver) {
        if (Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER,
                1, UserHandle.USER_CURRENT) != 1) {
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER,
                    1, UserHandle.USER_CURRENT);
            notifyCustomHeaderChanged(resolver);
        }
        final String provider = Settings.System.getStringForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, UserHandle.USER_CURRENT);
        if (!GALLERY_PROVIDER.equals(provider)) {
            Settings.System.putStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER,
                    GALLERY_PROVIDER, UserHandle.USER_CURRENT);
            notifyCustomHeaderChanged(resolver);
        }
    }

    private void updateGallerySummary() {
        if (mHeaderImageGallery == null) {
            return;
        }
        final String currentImage = Settings.System.getStringForUser(
                requireActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE,
                UserHandle.USER_CURRENT);
        if (!TextUtils.isEmpty(currentImage)) {
            mHeaderImageGallery.setSummary(R.string.qs_header_image_custom_selected);
        } else {
            mHeaderImageGallery.setSummary(R.string.qs_header_image_summary);
        }
    }

    private void updateHeaderHeightSummary(int height) {
        if (mHeaderHeight != null) {
            mHeaderHeight.setSummary(getString(R.string.qs_header_height_summary_value, height));
        }
    }

    private static void notifyCustomHeaderChanged(ContentResolver resolver) {
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER),
                null, false);
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                null, false);
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
