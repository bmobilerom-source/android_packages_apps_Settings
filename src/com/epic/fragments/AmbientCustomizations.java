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

package com.epic.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class AmbientCustomizations extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "AmbientCustomizations";
    private static final int REQUEST_PICK_IMAGE = 1000;

    private static final String KEY_AMBIENT_TEXT = "ambient_text";
    private static final String KEY_AMBIENT_TEXT_ALIGN = "ambient_text_align";
    private static final String KEY_AMBIENT_TEXT_TYPE_COLOR = "ambient_text_type_color";
    private static final String KEY_AMBIENT_TEXT_COLOR = "ambient_text_color";
    private static final String KEY_AMBIENT_CUSTOM_IMAGE = "ambient_custom_image";

    private CustomEditTextPreferenceCompat mAmbientText;
    private ListPreference mAmbientTextAlign;
    private ListPreference mAmbientTextTypeColor;
    private Preference mAmbientTextColor;
    private Preference mAmbientCustomImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_customization);

        ContentResolver resolver = getActivity().getContentResolver();

        // Text customization
        PreferenceCategory textCategory = findPreference("ambient_text_category");
        mAmbientText = findPreference(KEY_AMBIENT_TEXT);
        mAmbientTextAlign = findPreference(KEY_AMBIENT_TEXT_ALIGN);
        mAmbientTextTypeColor = findPreference(KEY_AMBIENT_TEXT_TYPE_COLOR);
        mAmbientTextColor = findPreference(KEY_AMBIENT_TEXT_COLOR);

        // Image customization
        PreferenceCategory imageCategory = findPreference("ambient_image_category");
        mAmbientCustomImage = findPreference(KEY_AMBIENT_CUSTOM_IMAGE);

        // Setup text preference
        if (mAmbientText != null) {
            String textValue = Settings.System.getString(resolver,
                    Settings.System.AMBIENT_TEXT_STRING);
            if (textValue != null && !textValue.isEmpty()) {
                mAmbientText.setText(textValue);
                mAmbientText.setSummary(textValue);
            } else {
                mAmbientText.setSummary(R.string.ambient_text_summary);
            }
            mAmbientText.setOnPreferenceChangeListener(this);
        }

        // Setup alignment preference
        if (mAmbientTextAlign != null) {
            int align = Settings.System.getInt(resolver,
                    Settings.System.AMBIENT_TEXT_ALIGNMENT, 0);
            mAmbientTextAlign.setValue(String.valueOf(align));
            int index = mAmbientTextAlign.findIndexOfValue(String.valueOf(align));
            if (index >= 0) {
                mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            }
            mAmbientTextAlign.setOnPreferenceChangeListener(this);
        }

        // Setup color type preference
        if (mAmbientTextTypeColor != null) {
            int colorType = Settings.System.getInt(resolver,
                    Settings.System.AMBIENT_TEXT_TYPE_COLOR, 0);
            mAmbientTextTypeColor.setValue(String.valueOf(colorType));
            int index = mAmbientTextTypeColor.findIndexOfValue(String.valueOf(colorType));
            if (index >= 0) {
                mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
            }
            mAmbientTextTypeColor.setOnPreferenceChangeListener(this);
            updateColorPreferenceState(colorType);
        }

        // Setup color preference
        if (mAmbientTextColor != null) {
            int color = Settings.System.getInt(resolver,
                    Settings.System.AMBIENT_TEXT_COLOR, 0xFF3980FF); // Default accent blue
            updateColorSummary(color);
            mAmbientTextColor.setOnPreferenceClickListener(preference -> {
                // Open color picker dialog
                showColorPickerDialog();
                return true;
            });
        }

        // Setup custom image preference
        if (mAmbientCustomImage != null) {
            String imageUri = Settings.System.getString(resolver,
                    Settings.System.AMBIENT_CUSTOM_IMAGE);
            if (imageUri != null && !imageUri.isEmpty()) {
                mAmbientCustomImage.setSummary(R.string.ambient_image_selected);
            } else {
                mAmbientCustomImage.setSummary(R.string.ambient_image_summary);
            }
            mAmbientCustomImage.setOnPreferenceClickListener(preference -> {
                pickImage();
                return true;
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        
        if (preference == mAmbientText) {
            String value = (String) newValue;
            AmbientCustomizationsHelper.setAmbientText(getContext(), value);
            if (value != null && !value.isEmpty()) {
                preference.setSummary(value);
            } else {
                preference.setSummary(R.string.ambient_text_summary);
            }
            return true;
        } else if (preference == mAmbientTextAlign) {
            int align = Integer.parseInt((String) newValue);
            AmbientCustomizationsHelper.setAmbientTextAlignment(getContext(), align);
            int index = mAmbientTextAlign.findIndexOfValue((String) newValue);
            if (index >= 0) {
                mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            }
            return true;
        } else if (preference == mAmbientTextTypeColor) {
            int value = Integer.parseInt((String) newValue);
            AmbientCustomizationsHelper.setAmbientTextTypeColor(getContext(), value);
            int index = mAmbientTextTypeColor.findIndexOfValue((String) newValue);
            mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
            updateColorPreferenceState(value);
            return true;
        }
        return false;
    }

    private void updateColorPreferenceState(int colorType) {
        if (mAmbientTextColor != null) {
            // Enable color picker only when custom color is selected (value == 2)
            mAmbientTextColor.setEnabled(colorType == 2);
        }
    }

    private void updateColorSummary(int color) {
        if (mAmbientTextColor != null) {
            String hex = String.format("#%08X", color);
            if (color == 0xFF3980FF) { // Default accent blue
                mAmbientTextColor.setSummary(R.string.default_string);
            } else {
                mAmbientTextColor.setSummary(hex);
            }
        }
    }

    private void showColorPickerDialog() {
        // For now, use a simple approach - in a full implementation,
        // you'd use a ColorPickerPreference or custom dialog
        Toast.makeText(getContext(), 
                R.string.ambient_color_picker_not_implemented, 
                Toast.LENGTH_SHORT).show();
        // TODO: Implement proper color picker dialog
        // This would require ColorPickerPreference or custom dialog
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK || result == null) {
                return;
            }
            final Uri imageUri = result.getData();
            if (imageUri != null) {
                AmbientCustomizationsHelper.setAmbientCustomImage(getContext(), imageUri.toString());
                if (mAmbientCustomImage != null) {
                    mAmbientCustomImage.setSummary(R.string.ambient_image_selected);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, result);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.ambient_customization) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}

