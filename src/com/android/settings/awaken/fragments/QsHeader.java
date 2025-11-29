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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;


@SearchIndexable
public class QsHeader extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QsHeader";
    private static final String KEY_QS_HEADER_IMAGE = "qs_header_image";
    private static final String KEY_QS_HEADER_SHADOW = "qs_header_shadow";
    private static final String KEY_QS_HEADER_HEIGHT = "qs_header_height";
    private static final String KEY_QS_HEADER_CUSTOM_FILE = "qs_header_custom_file";
    private static final int REQUEST_CODE_PICK_HEADER_FILE = 1001;

    private QsHeaderHelper mQsHeaderHelper;
    private ListPreference mHeaderImage;
    private ListPreference mHeaderShadow;
    private ListPreference mHeaderHeight;
    private Preference mCustomFilePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_header);
        mQsHeaderHelper = new QsHeaderHelper(getContext());

        // Enable QS header by default and set provider to static
        ContentResolver resolver = getActivity().getContentResolver();
        Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CUSTOM_HEADER, 1);
        Settings.System.putString(resolver, Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static");

        mHeaderImage = (ListPreference) findPreference(KEY_QS_HEADER_IMAGE);
        if (mHeaderImage != null) {
            // Use dynamic discovery of available headers
            java.util.List<String> names = mQsHeaderHelper.getAvailableImageNames();
            java.util.List<String> values = mQsHeaderHelper.getAvailableImageValues();

            if (names.size() > 0 && values.size() > 0) {
                mHeaderImage.setEntries(names.toArray(new String[0]));
                mHeaderImage.setEntryValues(values.toArray(new String[0]));

                mHeaderImage.setOnPreferenceChangeListener(this);
                String currentValue = mQsHeaderHelper.getCurrentHeaderValue();
                if (currentValue == null || !values.contains(currentValue)) {
                    currentValue = values.get(0); // Default to first available header
                }
                if (currentValue != null) {
                    mHeaderImage.setValue(currentValue);
                    updateImageSummary(currentValue);
                }
            } else {
                Log.w(TAG, "No QS header images found via dynamic discovery");
                // Fallback to static arrays
                String[] entries = getResources().getStringArray(R.array.anciui_header_img_entries);
                String[] staticValues = getResources().getStringArray(R.array.anciui_header_img_values);
                mHeaderImage.setEntries(entries);
                mHeaderImage.setEntryValues(staticValues);
                mHeaderImage.setOnPreferenceChangeListener(this);
            }
        }

        // Shadow / brightness preference (maps to STATUS_BAR_CUSTOM_HEADER_SHADOW)
        mHeaderShadow = (ListPreference) findPreference(KEY_QS_HEADER_SHADOW);
        if (mHeaderShadow != null) {
            mHeaderShadow.setOnPreferenceChangeListener(this);
            int currentShadow = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
            // Values are 0-3, map to index
            mHeaderShadow.setValue(String.valueOf(currentShadow));
        }

        // Header height preference (maps to STATUS_BAR_CUSTOM_HEADER_HEIGHT)
        mHeaderHeight = (ListPreference) findPreference(KEY_QS_HEADER_HEIGHT);
        if (mHeaderHeight != null) {
            mHeaderHeight.setOnPreferenceChangeListener(this);
            int currentHeight = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT, 142);
            mHeaderHeight.setValue(String.valueOf(currentHeight));
        }

        // Custom file picker preference
        mCustomFilePicker = findPreference(KEY_QS_HEADER_CUSTOM_FILE);
        if (mCustomFilePicker != null) {
            mCustomFilePicker.setOnPreferenceClickListener(preference -> {
                openFilePicker();
                return true;
            });
            updateCustomFileSummary();
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeaderImage) {
            String value = (String) newValue;
            // Validate against dynamic values
            java.util.List<String> values = mQsHeaderHelper.getAvailableImageValues();
            if (value == null || !values.contains(value)) {
                Log.w(TAG, "Invalid header image value: " + value);
                return false;
            }
            boolean success = mQsHeaderHelper.setHeaderImageValue(value);
            if (success) {
                updateImageSummary(value);
                Log.d(TAG, "QS Header image changed to: " + value);
            }
            return success;
        } else if (preference == mHeaderShadow) {
            try {
                int shadowValue = Integer.parseInt((String) newValue);
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, shadowValue);
                Log.d(TAG, "QS Header shadow changed to: " + shadowValue);
                return true;
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid shadow value: " + newValue, e);
                return false;
            }
        } else if (preference == mHeaderHeight) {
            try {
                int height = Integer.parseInt((String) newValue);
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_HEIGHT, height);
                Log.d(TAG, "QS Header height changed to: " + height);
                return true;
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid height value: " + newValue, e);
                return false;
            }
        }
        return false;
    }


    private void updateImageSummary(String value) {
        if (mHeaderImage != null) {
            java.util.List<String> names = mQsHeaderHelper.getAvailableImageNames();
            java.util.List<String> values = mQsHeaderHelper.getAvailableImageValues();

            int index = values.indexOf(value);
            if (index >= 0 && index < names.size()) {
                mHeaderImage.setSummary(names.get(index));
            } else {
                mHeaderImage.setSummary(R.string.qs_header_image_summary);
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "image/gif", "image/png", "image/jpeg", "image/jpg", "image/webp"});
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.qs_header_file_picker_title)), REQUEST_CODE_PICK_HEADER_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.qs_header_file_picker_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_HEADER_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedFileUri = data.getData();
                handleCustomFileSelected(selectedFileUri);
            }
        }
    }

    private void handleCustomFileSelected(Uri fileUri) {
        try {
            ContentResolver resolver = getContext().getContentResolver();
            // Store the file URI as a string
            String uriString = fileUri.toString();
            Settings.System.putStringForUser(resolver, Settings.System.STATUS_BAR_FILE_HEADER_IMAGE, uriString, UserHandle.USER_CURRENT);
            
            // Set provider to "file" to use custom file
            Settings.System.putStringForUser(resolver, Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, "file", UserHandle.USER_CURRENT);
            
            // Enable header
            Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_CUSTOM_HEADER, 1, UserHandle.USER_CURRENT);
            
            // Notify SystemUI
            resolver.notifyChange(Settings.System.getUriFor(Settings.System.STATUS_BAR_FILE_HEADER_IMAGE), null, true, UserHandle.USER_ALL);
            
            updateCustomFileSummary();
            Toast.makeText(getContext(), R.string.qs_header_file_picker_success, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Custom header file selected: " + uriString);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set custom header file", e);
            Toast.makeText(getContext(), R.string.qs_header_file_picker_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCustomFileSummary() {
        if (mCustomFilePicker != null) {
            ContentResolver resolver = getContext().getContentResolver();
            String fileUri = Settings.System.getStringForUser(resolver, Settings.System.STATUS_BAR_FILE_HEADER_IMAGE, UserHandle.USER_CURRENT);
            if (fileUri != null && !fileUri.isEmpty()) {
                try {
                    Uri uri = Uri.parse(fileUri);
                    String fileName = uri.getLastPathSegment();
                    if (fileName != null && fileName.length() > 30) {
                        fileName = fileName.substring(0, 27) + "...";
                    }
                    mCustomFilePicker.setSummary(fileName != null ? fileName : getString(R.string.qs_header_file_picker_summary));
                } catch (Exception e) {
                    mCustomFilePicker.setSummary(getString(R.string.qs_header_file_picker_summary));
                }
            } else {
                mCustomFilePicker.setSummary(getString(R.string.qs_header_file_picker_summary));
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

