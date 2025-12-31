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

import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

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
    private static final String KEY_AMBIENT_PREVIEW = "ambient_preview";
    private static final String KEY_AMBIENT_ENABLE = "ambient_customization_enable";
    private static final String KEY_DOZE_ALWAYS_ON = "doze_always_on";
    private static final String KEY_AMBIENT_TEXT_ENABLE = "ambient_text_enable";
    private static final String KEY_AMBIENT_TEXT_ANIMATION = "ambient_text_animation";
    private static final String KEY_AMBIENT_TEXT_SIZE = "ambient_text_size";
    private static final String KEY_AMBIENT_IMAGE_ENABLE = "ambient_image_enable";

    private SwitchPreference mAmbientEnable;
    private SwitchPreference mDozeAlwaysOn;
    private SwitchPreference mAmbientTextToggle;
    private SwitchPreference mAmbientTextAnimation;
    private SeekBarPreference mAmbientTextSize;
    private SwitchPreference mAmbientImageToggle;
    private CustomEditTextPreferenceCompat mAmbientText;
    private ListPreference mAmbientTextAlign;
    private ListPreference mAmbientTextTypeColor;
    private Preference mAmbientTextColor;
    private Preference mAmbientCustomImage;
    private LayoutPreference mAmbientPreview;
    private ImageView mPreviewImage;
    private TextView mPreviewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_customization);

        ContentResolver resolver = getActivity().getContentResolver();

        // Master Switch
        mAmbientEnable = findPreference(KEY_AMBIENT_ENABLE);
        if (mAmbientEnable != null) {
            // Use Settings.Secure.DOZE_ENABLED for master switch
            boolean enabled = Settings.Secure.getInt(resolver, 
                    Settings.Secure.DOZE_ENABLED, 1) == 1;
            mAmbientEnable.setChecked(enabled);
            mAmbientEnable.setOnPreferenceChangeListener(this);
        }

        // Always On Switch
        mDozeAlwaysOn = findPreference(KEY_DOZE_ALWAYS_ON);
        if (mDozeAlwaysOn != null) {
            boolean alwaysOn = Settings.Secure.getInt(resolver, 
                    Settings.Secure.DOZE_ALWAYS_ON, 0) == 1;
            mDozeAlwaysOn.setChecked(alwaysOn);
            mDozeAlwaysOn.setOnPreferenceChangeListener(this);
            // Disable AOD if master switch is off
            mDozeAlwaysOn.setEnabled(mAmbientEnable.isChecked());
        }

        // Ambient text toggle
        mAmbientTextToggle = findPreference(KEY_AMBIENT_TEXT_ENABLE);
        if (mAmbientTextToggle != null) {
            boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(getContext());
            mAmbientTextToggle.setChecked(textEnabled);
            mAmbientTextToggle.setOnPreferenceChangeListener(this);
        }

        // Ambient text animation
        mAmbientTextAnimation = findPreference(KEY_AMBIENT_TEXT_ANIMATION);
        if (mAmbientTextAnimation != null) {
            boolean animate = AmbientCustomizationsHelper.isAmbientTextAnimationEnabled(getContext());
            mAmbientTextAnimation.setChecked(animate);
            mAmbientTextAnimation.setOnPreferenceChangeListener(this);
        }

        // Ambient text size
        mAmbientTextSize = findPreference(KEY_AMBIENT_TEXT_SIZE);
        if (mAmbientTextSize != null) {
            // Set layout resource to ensure TextView exists for value display
            mAmbientTextSize.setLayoutResource(com.android.settings.R.layout.adaptive_preference_card_seekbar);
            mAmbientTextSize.setMin(20);
            mAmbientTextSize.setMax(60);
            mAmbientTextSize.setSeekBarIncrement(1);
            int size = AmbientCustomizationsHelper.getAmbientTextSize(getContext());
            mAmbientTextSize.setValue(size);
            updateTextSizeSummary(size);
            mAmbientTextSize.setOnPreferenceChangeListener(this);
        }

        // Ambient image toggle
        mAmbientImageToggle = findPreference(KEY_AMBIENT_IMAGE_ENABLE);
        if (mAmbientImageToggle != null) {
            boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(getContext());
            mAmbientImageToggle.setChecked(imageEnabled);
            mAmbientImageToggle.setOnPreferenceChangeListener(this);
        }

        // Preview
        mAmbientPreview = findPreference(KEY_AMBIENT_PREVIEW);
        if (mAmbientPreview != null) {
            mPreviewImage = mAmbientPreview.findViewById(R.id.ambient_preview_image);
            mPreviewText = mAmbientPreview.findViewById(R.id.ambient_preview_text);
        }

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
        
        updatePreview();
        updateDependencyState(mAmbientEnable != null && mAmbientEnable.isChecked());
    }

    private void updatePreview() {
        if (getContext() == null) return;

        boolean masterEnabled = mAmbientEnable != null && mAmbientEnable.isChecked();
        boolean textFeatureEnabled = masterEnabled
                && mAmbientTextToggle != null && mAmbientTextToggle.isChecked();
        boolean imageFeatureEnabled = masterEnabled
                && mAmbientImageToggle != null && mAmbientImageToggle.isChecked();

        // Update Text
        if (mPreviewText != null) {
            if (textFeatureEnabled) {
                String text = AmbientCustomizationsHelper.getAmbientText(getContext());
                if (text == null || text.isEmpty()) {
                    text = getString(R.string.ambient_text_title);
                }
                mPreviewText.setVisibility(View.VISIBLE);
                mPreviewText.setText(text);

                int typeColor = AmbientCustomizationsHelper.getAmbientTextTypeColor(getContext());
                int color = 0xFFFFFFFF; // Default white
                if (typeColor == 0) {
                    color = com.android.settingslib.Utils.getColorAttrDefaultColor(
                            getContext(), android.R.attr.colorAccent);
                } else if (typeColor == 2) {
                    color = AmbientCustomizationsHelper.getAmbientTextColor(getContext());
                }
                mPreviewText.setTextColor(color);
            } else {
                mPreviewText.setVisibility(View.GONE);
            }
        }

        // Update Image
        if (mPreviewImage != null) {
            if (imageFeatureEnabled) {
                String imageUriStr = AmbientCustomizationsHelper.getAmbientCustomImage(getContext());
                if (imageUriStr != null && !imageUriStr.isEmpty()) {
                    try {
                        mPreviewImage.setImageURI(Uri.parse(imageUriStr));
                        mPreviewImage.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load preview image", e);
                        mPreviewImage.setVisibility(View.GONE);
                    }
                } else {
                    mPreviewImage.setVisibility(View.GONE);
                }
            } else {
                mPreviewImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean handled = false;
        
        if (preference == mAmbientEnable) {
            boolean enabled = (Boolean) newValue;
            // Toggle secure setting DOZE_ENABLED
            Settings.Secure.putInt(resolver, Settings.Secure.DOZE_ENABLED, enabled ? 1 : 0);
            // Also toggle DOZE_ALWAYS_ON if AOD is desired when enabled
            // For now, just link it to master switch or leave it independent if there's another toggle
            // We'll just handle DOZE_ENABLED as the master switch for Ambient Display features
            
            // Enable/Disable other preferences based on switch
            updateDependencyState(enabled);
            updatePreview();
            return true;
        } else if (preference == mDozeAlwaysOn) {
            boolean alwaysOn = (Boolean) newValue;
            Settings.Secure.putInt(resolver, Settings.Secure.DOZE_ALWAYS_ON, alwaysOn ? 1 : 0);
            return true;
        } else if (preference == mAmbientTextToggle) {
            boolean enabled = (Boolean) newValue;
            AmbientCustomizationsHelper.setAmbientTextEnabled(getContext(), enabled);
            updateDependencyState(mAmbientEnable != null && mAmbientEnable.isChecked());
            handled = true;
        } else if (preference == mAmbientTextAnimation) {
            boolean animate = (Boolean) newValue;
            AmbientCustomizationsHelper.setAmbientTextAnimationEnabled(getContext(), animate);
            handled = true;
        } else if (preference == mAmbientTextSize) {
            int size = (Integer) newValue;
            AmbientCustomizationsHelper.setAmbientTextSize(getContext(), size);
            updateTextSizeSummary(size);
            handled = true;
        } else if (preference == mAmbientImageToggle) {
            boolean enabled = (Boolean) newValue;
            AmbientCustomizationsHelper.setAmbientImageEnabled(getContext(), enabled);
            updateDependencyState(mAmbientEnable != null && mAmbientEnable.isChecked());
            handled = true;
        } else if (preference == mAmbientText) {
            String value = (String) newValue;
            AmbientCustomizationsHelper.setAmbientText(getContext(), value);
            if (value != null && !value.isEmpty()) {
                ((CustomEditTextPreferenceCompat) preference).setText(value);
                preference.setSummary(value);
            } else {
                preference.setSummary(R.string.ambient_text_summary);
            }
            handled = true;
        } else if (preference == mAmbientTextAlign) {
            int align = Integer.parseInt((String) newValue);
            AmbientCustomizationsHelper.setAmbientTextAlignment(getContext(), align);
            int index = mAmbientTextAlign.findIndexOfValue((String) newValue);
            if (index >= 0) {
                mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            }
            handled = true;
        } else if (preference == mAmbientTextTypeColor) {
            int value = Integer.parseInt((String) newValue);
            AmbientCustomizationsHelper.setAmbientTextTypeColor(getContext(), value);
            int index = mAmbientTextTypeColor.findIndexOfValue((String) newValue);
            mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
            updateColorPreferenceState(value);
            handled = true;
        }
        
        if (handled) {
            updatePreview();
        }
        return handled;
    }

    private void updateDependencyState(boolean enabled) {
        boolean textFeatureEnabled = enabled
                && mAmbientTextToggle != null && mAmbientTextToggle.isChecked();
        boolean imageFeatureEnabled = enabled
                && mAmbientImageToggle != null && mAmbientImageToggle.isChecked();

        if (mDozeAlwaysOn != null) mDozeAlwaysOn.setEnabled(enabled);
        if (mAmbientTextToggle != null) mAmbientTextToggle.setEnabled(enabled);
        if (mAmbientTextAnimation != null) mAmbientTextAnimation.setEnabled(textFeatureEnabled);
        if (mAmbientTextSize != null) mAmbientTextSize.setEnabled(textFeatureEnabled);
        if (mAmbientText != null) mAmbientText.setEnabled(textFeatureEnabled);
        if (mAmbientTextAlign != null) mAmbientTextAlign.setEnabled(textFeatureEnabled);
        if (mAmbientTextTypeColor != null) mAmbientTextTypeColor.setEnabled(textFeatureEnabled);
        if (mAmbientTextColor != null) {
            boolean allowColor = textFeatureEnabled &&
                    AmbientCustomizationsHelper.getAmbientTextTypeColor(getContext()) == 2;
            mAmbientTextColor.setEnabled(allowColor);
        }
        if (mAmbientImageToggle != null) mAmbientImageToggle.setEnabled(enabled);
        if (mAmbientCustomImage != null) mAmbientCustomImage.setEnabled(imageFeatureEnabled);
        if (mAmbientPreview != null) {
            mAmbientPreview.setVisible(enabled && (textFeatureEnabled || imageFeatureEnabled));
        }
    }

    private void updateColorPreferenceState(int colorType) {
        if (mAmbientTextColor != null) {
            boolean masterEnabled = mAmbientEnable != null && mAmbientEnable.isChecked();
            boolean textFeatureEnabled = masterEnabled
                    && mAmbientTextToggle != null && mAmbientTextToggle.isChecked();
            mAmbientTextColor.setEnabled(textFeatureEnabled && colorType == 2);
        }
    }

    private void updateTextSizeSummary(int size) {
        if (mAmbientTextSize != null) {
            mAmbientTextSize.setSummary(getString(R.string.ambient_text_size_summary_value, size));
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
                // Take permission to read the URI
                try {
                    getContext().getContentResolver().takePersistableUriPermission(imageUri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to take persistable uri permission", e);
                }
                
                AmbientCustomizationsHelper.setAmbientCustomImage(getContext(), imageUri.toString());
                if (mAmbientCustomImage != null) {
                    mAmbientCustomImage.setSummary(R.string.ambient_image_selected);
                }
                updatePreview();
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

