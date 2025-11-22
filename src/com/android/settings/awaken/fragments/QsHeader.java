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
    private static final String KEY_QS_HEADER_FILE_PICKER = "qs_header_file_picker";
    private static final int REQUEST_PICK_HEADER_IMAGE = 1001;

    private QsHeaderHelper mQsHeaderHelper;

    // Provider values that match SystemUI
    private static final String PROVIDER_STATIC = "static";
    private static final String PROVIDER_FILE = "file";
    private static final String PROVIDER_DAYLIGHT = "daylight";

    private ListPreference mHeaderProvider;
    private ListPreference mHeaderVisibility;
    private ListPreference mHeaderImage;
    private Preference mFilePicker;
    
    private List<String> mHeaderImageEntries = new ArrayList<>();
    private List<String> mHeaderImageValues = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_header);
        mQsHeaderHelper = new QsHeaderHelper(getContext());

        final ContentResolver resolver = getActivity().getContentResolver();

        mHeaderProvider = (ListPreference) findPreference(KEY_QS_HEADER_PROVIDER);
        if (mHeaderProvider != null) {
            mHeaderProvider.setOnPreferenceChangeListener(this);
            String provider = mQsHeaderHelper.getHeaderProvider();
            mHeaderProvider.setValue(provider);
            updateProviderSummary(provider);
        }

        mHeaderVisibility = (ListPreference) findPreference(KEY_QS_HEADER_VISIBILITY);
        if (mHeaderVisibility != null) {
            mHeaderVisibility.setOnPreferenceChangeListener(this);
            int visibility = mQsHeaderHelper.getHeaderVisibility() ? 1 : 0;
            mHeaderVisibility.setValue(String.valueOf(visibility));
            updateVisibilitySummary(visibility);
        }

        mHeaderImage = (ListPreference) findPreference(KEY_QS_HEADER_IMAGE);
        if (mHeaderImage != null) {
            discoverAvailableHeaderImages();

            mHeaderImage.setEntries(mHeaderImageEntries.toArray(new CharSequence[0]));
            mHeaderImage.setEntryValues(mHeaderImageValues.toArray(new CharSequence[0]));

            mHeaderImage.setOnPreferenceChangeListener(this);
            String currentValue = mQsHeaderHelper.getCurrentHeaderValue();
            if (currentValue == null || !mHeaderImageValues.contains(currentValue)) {
                if (!mHeaderImageValues.isEmpty()) {
                    currentValue = mHeaderImageValues.get(0);
                }
            }
            if (currentValue != null) {
                mHeaderImage.setValue(currentValue);
                updateImageSummary(currentValue);
            }
        }

        // File Picker Preference
        mFilePicker = findPreference("qs_header_file_image");
        if (mFilePicker == null) {
            // Create it if not in XML (compatibility)
            mFilePicker = new Preference(getContext());
            mFilePicker.setKey("qs_header_file_image");
            mFilePicker.setTitle(R.string.custom_picture_theme_picker_title);
            mFilePicker.setSummary(R.string.custom_picture_theme_picker_summary);
            mFilePicker.setOrder(15);
            getPreferenceScreen().addPreference(mFilePicker);
        }
        mFilePicker.setOnPreferenceClickListener(preference -> {
            pickFile();
            return true;
        });
        
        updateFilePickerVisibility();
    }

    private void updateFilePickerVisibility() {
        if (mFilePicker != null && mHeaderProvider != null) {
            String provider = mHeaderProvider.getValue();
            mFilePicker.setVisible(PROVIDER_FILE.equals(provider));
        }
    }

    private void pickFile() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        startActivityForResult(intent, REQUEST_PICK_HEADER_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent result) {
        if (requestCode == REQUEST_PICK_HEADER_IMAGE) {
            if (resultCode != android.app.Activity.RESULT_OK || result == null) {
                return;
            }
            final android.net.Uri uri = result.getData();
            if (uri != null) {
                try {
                    getContext().getContentResolver().takePersistableUriPermission(uri, 
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    String uriString = uri.toString();
                    Settings.System.putString(getContentResolver(),
                            Settings.System.STATUS_BAR_FILE_HEADER_IMAGE, uriString);
                    
                    // Also ensure provider is set to file
                    Settings.System.putString(getContentResolver(),
                            Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, PROVIDER_FILE);
                    if (mHeaderProvider != null) {
                        mHeaderProvider.setValue(PROVIDER_FILE);
                        updateProviderSummary(PROVIDER_FILE);
                    }
                    
                    // Notify change
                    getContentResolver().notifyChange(
                            Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                            null, true);
                    getContentResolver().notifyChange(
                            Settings.System.getUriFor(Settings.System.STATUS_BAR_FILE_HEADER_IMAGE),
                            null, true);
                            
                    mFilePicker.setSummary(R.string.image_selected);
                    Log.d(TAG, "Set custom header image: " + uriString);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to set custom header image", e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, result);
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
                // Notify SystemUI of the change - use UserHandle.USER_ALL to notify all users
                resolver.notifyChange(
                        Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                        null, true);
                updateProviderSummary(provider);
                updateFilePickerVisibility(); // Update visibility
                Log.d(TAG, "QS Header provider changed to: " + provider);
            }
            return success;
        } else if (preference == mHeaderVisibility) {
            int visibility = Integer.parseInt((String) newValue);
            boolean success = mQsHeaderHelper.setHeaderVisibility(visibility == 1);
            if (success) {
                updateVisibilitySummary(visibility);
                Log.d(TAG, "QS Header visibility changed to: " + visibility);
            }
            return success;
        } else if (preference == mHeaderImage) {
            String value = (String) newValue;
            if (value == null || !mHeaderImageValues.contains(value)) {
                Log.w(TAG, "Invalid header image value: " + value);
                return false;
            }
            boolean success = mQsHeaderHelper.setHeaderImageValue(value);
            if (success) {
                updateImageSummary(value);
                Log.d(TAG, "QS Header image changed to: " + value);
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
     * Load available QS header images from the helper
     */
    private void discoverAvailableHeaderImages() {
        mHeaderImageEntries.clear();
        mHeaderImageValues.clear();

        // Use helper to get available images
        List<String> entries = mQsHeaderHelper.getAvailableImageNames();
        List<String> values = mQsHeaderHelper.getAvailableImageValues();

        mHeaderImageEntries.addAll(entries);
        mHeaderImageValues.addAll(values);

        Log.d(TAG, "Loaded " + mHeaderImageEntries.size() + " header images");

        // Ensure we have at least one entry/value
        if (mHeaderImageEntries.isEmpty() || mHeaderImageValues.isEmpty()) {
            mHeaderImageEntries.clear();
            mHeaderImageValues.clear();
            mHeaderImageEntries.add("Header 1");
            mHeaderImageValues.add("com.android.systemui/qs_header_image_1");
        }
    }

    private void updateImageSummary(String value) {
        if (mHeaderImage != null) {
            int index = mHeaderImageValues.indexOf(value);
            if (index >= 0 && index < mHeaderImageEntries.size()) {
                mHeaderImage.setSummary(mHeaderImageEntries.get(index));
            } else {
                mHeaderImage.setSummary(R.string.qs_header_image_summary);
            }
        }
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

