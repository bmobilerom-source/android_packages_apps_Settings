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
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QsHeader extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QsHeader";
    private static final String KEY_QS_HEADER_PROVIDER = "qs_header_provider";
    private static final String KEY_QS_HEADER_VISIBILITY = "qs_header_visibility";
    private static final String KEY_QS_HEADER_IMAGE = "qs_header_image";

    private ListPreference mHeaderProvider;
    private ListPreference mHeaderVisibility;
    private ListPreference mHeaderImage;
    
    private List<String> mHeaderImageEntries = new ArrayList<>();
    private List<String> mHeaderImageValues = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_header);

        final ContentResolver resolver = getActivity().getContentResolver();

        mHeaderProvider = (ListPreference) findPreference(KEY_QS_HEADER_PROVIDER);
        if (mHeaderProvider != null) {
            mHeaderProvider.setOnPreferenceChangeListener(this);
            String provider = Settings.System.getStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, UserHandle.USER_CURRENT);
            if (provider == null || provider.isEmpty()) {
                provider = "static"; // Default from frameworks/base
            }
            mHeaderProvider.setValue(provider);
            updateProviderSummary(provider);
        }

        mHeaderVisibility = (ListPreference) findPreference(KEY_QS_HEADER_VISIBILITY);
        if (mHeaderVisibility != null) {
            mHeaderVisibility.setOnPreferenceChangeListener(this);
            int visibility = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER, 0, UserHandle.USER_CURRENT);
            mHeaderVisibility.setValue(String.valueOf(visibility));
            updateVisibilitySummary(visibility);
        }

        mHeaderImage = (ListPreference) findPreference(KEY_QS_HEADER_IMAGE);
        if (mHeaderImage != null) {
            // Dynamically discover all available header images
            discoverAvailableHeaderImages();
            
            // Set entries and values
            mHeaderImage.setEntries(mHeaderImageEntries.toArray(new CharSequence[0]));
            mHeaderImage.setEntryValues(mHeaderImageValues.toArray(new CharSequence[0]));
            
            mHeaderImage.setOnPreferenceChangeListener(this);
            String image = Settings.System.getStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE, UserHandle.USER_CURRENT);
            if (image == null || image.isEmpty()) {
                image = "com.android.systemui/qs_header_image_1";
            }
            // Extract image index from string like "com.android.systemui/qs_header_image_1"
            int imageIndex = extractImageIndex(image);
            // Make sure the index is valid
            if (imageIndex < 0 || imageIndex >= mHeaderImageValues.size()) {
                imageIndex = 0;
                image = "com.android.systemui/qs_header_image_1";
                Settings.System.putStringForUser(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE,
                        image, UserHandle.USER_CURRENT);
            }
            mHeaderImage.setValue(String.valueOf(imageIndex));
            updateImageSummary(imageIndex);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mHeaderProvider) {
            String provider = (String) newValue;
            boolean success = Settings.System.putStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER,
                    provider, UserHandle.USER_CURRENT);
            if (success) {
                // Notify SystemUI of the change
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                        null, false);
                updateProviderSummary(provider);
            }
            return success;
        } else if (preference == mHeaderVisibility) {
            int visibility = Integer.parseInt((String) newValue);
            boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER,
                    visibility, UserHandle.USER_CURRENT);
            if (success) {
                // Notify SystemUI of the change
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER),
                        null, false);
                updateVisibilitySummary(visibility);
            }
            return success;
        } else if (preference == mHeaderImage) {
            int imageIndex = Integer.parseInt((String) newValue);
            // Validate index
            if (imageIndex < 0 || imageIndex >= mHeaderImageValues.size()) {
                Log.w(TAG, "Invalid image index: " + imageIndex);
                return false;
            }
            // Convert 0-based index to 1-based image number
            int imageNumber = imageIndex + 1;
            // Convert index to resource string format
            String imageValue = "com.android.systemui/qs_header_image_" + imageNumber;
            boolean success = Settings.System.putStringForUser(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE,
                    imageValue, UserHandle.USER_CURRENT);
            if (success) {
                // Notify SystemUI of the change
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE),
                        null, false);
                updateImageSummary(imageIndex);
            }
            return success;
        }
        return false;
    }

    private void updateProviderSummary(String provider) {
        if (mHeaderProvider != null) {
            String[] entries = getResources().getStringArray(R.array.custom_header_provider_entries);
            String[] values = getResources().getStringArray(R.array.custom_header_provider_values);
            for (int i = 0; i < values.length; i++) {
                if (provider.equals(values[i])) {
                    mHeaderProvider.setSummary(entries[i]);
                    return;
                }
            }
        }
    }

    private void updateVisibilitySummary(int visibility) {
        if (mHeaderVisibility != null) {
            String[] entries = getResources().getStringArray(R.array.custom_header_visibility_entries);
            if (visibility >= 0 && visibility < entries.length) {
                mHeaderVisibility.setSummary(entries[visibility]);
            }
        }
    }

    /**
     * Dynamically discover all available QS header images from SystemUI package
     */
    private void discoverAvailableHeaderImages() {
        mHeaderImageEntries.clear();
        mHeaderImageValues.clear();
        
        try {
            PackageManager pm = getActivity().getPackageManager();
            Resources sysuiRes = pm.getResourcesForApplication("com.android.systemui");
            
            // Try to find all qs_header_image_* resources
            // We'll check from 1 to 200 (should cover all available headers)
            for (int i = 1; i <= 200; i++) {
                String resourceName = "qs_header_image_" + i;
                int resId = sysuiRes.getIdentifier(resourceName, "drawable", "com.android.systemui");
                if (resId != 0) {
                    // Resource exists, add it to the list
                    String entry = "Header " + i;
                    // Try to get a better name if available
                    try {
                        String entryName = sysuiRes.getResourceEntryName(resId);
                        if (entryName != null && entryName.startsWith("qs_header_image_")) {
                            entry = "Header " + i;
                        }
                    } catch (Exception e) {
                        // Use default name
                    }
                    mHeaderImageEntries.add(entry);
                    mHeaderImageValues.add(String.valueOf(i - 1)); // 0-based index
                }
            }
            
            Log.d(TAG, "Discovered " + mHeaderImageEntries.size() + " header images");
        } catch (Exception e) {
            Log.e(TAG, "Failed to discover header images", e);
            // Fallback to default entries from arrays.xml
            String[] entries = getResources().getStringArray(R.array.anciui_header_img_entries);
            String[] values = getResources().getStringArray(R.array.anciui_header_img_values);
            for (int i = 0; i < entries.length && i < values.length; i++) {
                mHeaderImageEntries.add(entries[i]);
                mHeaderImageValues.add(values[i]);
            }
        }
        
        // Ensure we have at least one entry
        if (mHeaderImageEntries.isEmpty()) {
            mHeaderImageEntries.add("Header 1");
            mHeaderImageValues.add("0");
        }
    }

    private void updateImageSummary(int image) {
        if (mHeaderImage != null) {
            if (image >= 0 && image < mHeaderImageEntries.size()) {
                mHeaderImage.setSummary(mHeaderImageEntries.get(image));
            } else {
                mHeaderImage.setSummary(R.string.qs_header_image_summary);
            }
        }
    }

    /**
     * Extract image index from resource string like "com.android.systemui/qs_header_image_1"
     * Returns 0-based index (e.g., qs_header_image_1 -> 0, qs_header_image_2 -> 1)
     */
    private int extractImageIndex(String imageValue) {
        if (imageValue == null || imageValue.isEmpty()) {
            return 0;
        }
        try {
            // Format: "com.android.systemui/qs_header_image_N" or just "qs_header_image_N"
            String imageName;
            int slashIndex = imageValue.indexOf('/');
            if (slashIndex >= 0 && slashIndex < imageValue.length() - 1) {
                imageName = imageValue.substring(slashIndex + 1);
            } else {
                imageName = imageValue;
            }
            
            // Extract number from string like "qs_header_image_1" or "qs_header_image_25"
            int lastUnderscore = imageName.lastIndexOf('_');
            if (lastUnderscore >= 0 && lastUnderscore < imageName.length() - 1) {
                String numberStr = imageName.substring(lastUnderscore + 1);
                int imageNumber = Integer.parseInt(numberStr);
                // Convert to 0-based index (image_1 -> 0, image_2 -> 1, etc.)
                int index = Math.max(0, imageNumber - 1);
                // Validate against discovered headers
                if (index >= 0 && index < mHeaderImageValues.size()) {
                    return index;
                } else {
                    Log.w(TAG, "Image index " + index + " out of range, using 0");
                    return 0;
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse image index from: " + imageValue, e);
        }
        return 0;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.qs_header;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}

