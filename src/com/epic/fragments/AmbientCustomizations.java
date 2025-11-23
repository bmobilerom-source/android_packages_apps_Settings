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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.settings.widget.SeekBarPreference;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.epic.view.AODClockView;
import com.epic.ambient.NotificationPreviewManager;
import com.epic.view.AmbientNotificationView;
import com.epic.view.AmbientMusicVisualizer;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    private LayoutPreference mPreviewPreference;
    private View mPreviewView;
    private TextView mPreviewText;
    private ImageView mPreviewImage;
    private AODClockView mPreviewClock;

    // Clock style preference
    private ListPreference mClockStylePreference;

    // Notification preview
    private NotificationPreviewManager mNotificationManager;
    private AmbientNotificationView mNotificationPreview;

    // Music visualizer
    private AmbientMusicVisualizer mMusicVisualizer;
    private ListPreference mVisualizerModePreference;
    private PreferenceCategory textCategory;
    private PreferenceCategory imageCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_customization);

        ContentResolver resolver = getActivity().getContentResolver();

        // Master switches
        mAmbientEnable = findPreference("ambient_customization_enable");
        mDozeAlwaysOn = findPreference("doze_always_on");
        
        // Text customization switches
        mAmbientTextToggle = findPreference("ambient_text_enable");
        mAmbientTextAnimation = findPreference("ambient_text_animation");
        mAmbientTextSize = findPreference("ambient_text_size");
        
        // Image customization switch
        mAmbientImageToggle = findPreference("ambient_image_enable");
        
        // Text customization preferences
        textCategory = findPreference("ambient_text_category");
        mAmbientText = findPreference(KEY_AMBIENT_TEXT);
        mAmbientTextAlign = findPreference(KEY_AMBIENT_TEXT_ALIGN);
        mAmbientTextTypeColor = findPreference(KEY_AMBIENT_TEXT_TYPE_COLOR);
        mAmbientTextColor = findPreference(KEY_AMBIENT_TEXT_COLOR);

        // Image customization preferences
        imageCategory = findPreference("ambient_image_category");
        mAmbientCustomImage = findPreference(KEY_AMBIENT_CUSTOM_IMAGE);
        
        // Setup master switch
        if (mAmbientEnable != null) {
            // Master switch controls overall ambient customization feature
            // Check if any customization is enabled
            boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(getContext());
            boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(getContext());
            mAmbientEnable.setChecked(textEnabled || imageEnabled);
            mAmbientEnable.setOnPreferenceChangeListener(this);
        }
        
        // Setup Doze Always On
        if (mDozeAlwaysOn != null) {
            boolean alwaysOn = Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
            mDozeAlwaysOn.setChecked(alwaysOn);
            mDozeAlwaysOn.setOnPreferenceChangeListener(this);
        }
        
        // Setup text toggle
        if (mAmbientTextToggle != null) {
            boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(getContext());
            mAmbientTextToggle.setChecked(textEnabled);
            mAmbientTextToggle.setOnPreferenceChangeListener(this);
        }
        
        // Setup text animation toggle
        if (mAmbientTextAnimation != null) {
            boolean animate = AmbientCustomizationsHelper.isAmbientTextAnimationEnabled(getContext());
            mAmbientTextAnimation.setChecked(animate);
            mAmbientTextAnimation.setOnPreferenceChangeListener(this);
        }
        
        // Setup text size seekbar
        if (mAmbientTextSize != null) {
            int size = AmbientCustomizationsHelper.getAmbientTextSize(getContext());
            mAmbientTextSize.setProgress(size);
            mAmbientTextSize.setOnPreferenceChangeListener(this);
        }
        
        // Setup image toggle
        if (mAmbientImageToggle != null) {
            boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(getContext());
            mAmbientImageToggle.setChecked(imageEnabled);
            mAmbientImageToggle.setOnPreferenceChangeListener(this);
        }

        // Setup text preference
        if (mAmbientText != null) {
            String textValue = AmbientCustomizationsHelper.getAmbientText(getContext());
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
            int align = AmbientCustomizationsHelper.getAmbientTextAlignment(getContext());
            mAmbientTextAlign.setValue(String.valueOf(align));
            int index = mAmbientTextAlign.findIndexOfValue(String.valueOf(align));
            if (index >= 0) {
                mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            }
            mAmbientTextAlign.setOnPreferenceChangeListener(this);
        }

        // Setup color type preference
        if (mAmbientTextTypeColor != null) {
            int colorType = AmbientCustomizationsHelper.getAmbientTextTypeColor(getContext());
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
            int color = AmbientCustomizationsHelper.getAmbientTextColor(getContext());
            updateColorSummary(color);
            mAmbientTextColor.setOnPreferenceClickListener(preference -> {
                // Open color picker dialog
                showColorPickerDialog();
                return true;
            });
        }

        // Setup custom image preference
        if (mAmbientCustomImage != null) {
            String imageUri = AmbientCustomizationsHelper.getAmbientCustomImage(getContext());
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

        // Setup clock style preference
        mClockStylePreference = (ListPreference) findPreference(KEY_AOD_CLOCK_STYLE);
        if (mClockStylePreference != null) {
            mClockStylePreference.setOnPreferenceChangeListener(this);
            int currentStyle = getCurrentClockStyle();
            mClockStylePreference.setValue(String.valueOf(currentStyle));
            updateClockStyleSummary(currentStyle);
        }

        // Initialize notification preview manager
        mNotificationManager = new NotificationPreviewManager(getContext());

        // Initialize music visualizer preference
        mVisualizerModePreference = (ListPreference) findPreference(KEY_MUSIC_VISUALIZER_MODE);
        if (mVisualizerModePreference != null) {
            mVisualizerModePreference.setOnPreferenceChangeListener(this);
            int currentMode = getCurrentVisualizerMode();
            mVisualizerModePreference.setValue(String.valueOf(currentMode));
            updateVisualizerModeSummary(currentMode);
        }

        // Initialize preview - defer view access to onViewCreated
        mPreviewPreference = findPreference("ambient_preview");

        // Update UI based on current states
        updateCategoryVisibility();
        updatePreview();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preview views - simplified approach
        // Note: Preview functionality may need additional implementation
        try {
            mPreviewText = view.findViewById(R.id.ambient_preview_text);
            mPreviewImage = view.findViewById(R.id.ambient_preview_image);
            mPreviewClock = view.findViewById(R.id.ambient_preview_clock);
            if (mPreviewClock != null) {
                int clockStyle = getCurrentClockStyle();
                mPreviewClock.setClockStyle(clockStyle);
                mPreviewClock.setColors(
                    AmbientCustomizationsHelper.getAmbientTextColor(getContext()),
                    Color.parseColor("#B0B0B0"),
                    Color.parseColor("#00BFFF")
                );
            }

            mNotificationPreview = view.findViewById(R.id.ambient_preview_notification);
            if (mNotificationPreview != null) {
                // Create a sample notification preview for demonstration
                NotificationPreviewManager.NotificationPreview samplePreview =
                    new NotificationPreviewManager.NotificationPreview();
                samplePreview.smartText = "💬 Sample notification preview";
                samplePreview.category = NotificationPreviewManager.CATEGORY_MESSAGE;
                samplePreview.packageName = "com.example.messaging";
                mNotificationPreview.setNotificationPreview(samplePreview);
            }

            mMusicVisualizer = view.findViewById(R.id.ambient_preview_visualizer);
            if (mMusicVisualizer != null) {
                int visualizerMode = getCurrentVisualizerMode();
                mMusicVisualizer.setVisualizationMode(visualizerMode);
                mMusicVisualizer.setColors(
                    AmbientCustomizationsHelper.getAmbientTextColor(getContext()),
                    Color.parseColor("#FF1493"),
                    Color.parseColor("#32CD32")
                );
            }
        } catch (Exception e) {
            // Preview views may not be available - disable preview functionality
            mPreviewText = null;
            mPreviewImage = null;
            mPreviewClock = null;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (getActivity() == null || getActivity().getContentResolver() == null) {
            return false;
        }
        
        ContentResolver resolver = getActivity().getContentResolver();
        
        try {
            if (preference == mAmbientEnable) {
                boolean enabled = (Boolean) newValue;
                // Master switch enables/disables all ambient customizations
                if (mAmbientTextToggle != null) {
                    mAmbientTextToggle.setChecked(enabled);
                    AmbientCustomizationsHelper.setAmbientTextEnabled(getContext(), enabled);
                }
                if (mAmbientImageToggle != null) {
                    mAmbientImageToggle.setChecked(enabled);
                    AmbientCustomizationsHelper.setAmbientImageEnabled(getContext(), enabled);
                }
                return true;
            } else if (preference == mDozeAlwaysOn) {
                boolean alwaysOn = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver,
                        Settings.Secure.DOZE_ALWAYS_ON, alwaysOn ? 1 : 0, UserHandle.USER_CURRENT);
                return true;
            } else if (preference == mAmbientTextToggle) {
                boolean enabled = (Boolean) newValue;
                AmbientCustomizationsHelper.setAmbientTextEnabled(getContext(), enabled);
                updateMasterSwitchState();
                updateCategoryVisibility();
                updatePreview();
                return true;
            } else if (preference == mAmbientTextAnimation) {
                boolean animate = (Boolean) newValue;
                AmbientCustomizationsHelper.setAmbientTextAnimationEnabled(getContext(), animate);
                updatePreview();
                return true;
            } else if (preference == mAmbientTextSize) {
                int size = (Integer) newValue;
                AmbientCustomizationsHelper.setAmbientTextSize(getContext(), size);
                updatePreview();
                return true;
            } else if (preference == mAmbientImageToggle) {
                boolean enabled = (Boolean) newValue;
                AmbientCustomizationsHelper.setAmbientImageEnabled(getContext(), enabled);
                updateMasterSwitchState();
                updateCategoryVisibility();
                updatePreview();
                return true;
            } else if (preference == mAmbientText) {
                String value = (String) newValue;
                AmbientCustomizationsHelper.setAmbientText(getContext(), value);
                if (value != null && !value.isEmpty()) {
                    preference.setSummary(value);
                } else {
                    preference.setSummary(R.string.ambient_text_summary);
                }
                updatePreview();
                return true;
            } else if (preference == mAmbientTextAlign) {
                int align = Integer.parseInt((String) newValue);
                int index = mAmbientTextAlign.findIndexOfValue((String) newValue);
                AmbientCustomizationsHelper.setAmbientTextAlignment(getContext(), align);
                if (index >= 0) {
                    mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
                }
                updatePreview();
                return true;
            } else if (preference == mAmbientTextTypeColor) {
                int value = Integer.parseInt((String) newValue);
                int index = mAmbientTextTypeColor.findIndexOfValue((String) newValue);
                mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
                AmbientCustomizationsHelper.setAmbientTextTypeColor(getContext(), value);
                updateColorPreferenceState(value);
                updatePreview();
                return true;
            } else if (preference == mClockStylePreference) {
                int style = Integer.parseInt((String) newValue);
                setClockStyle(style);
                updateClockStyleSummary(style);
                return true;
            } else if (preference == mVisualizerModePreference) {
                int mode = Integer.parseInt((String) newValue);
                setVisualizerMode(mode);
                updateVisualizerModeSummary(mode);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling preference change", e);
            return false;
        }
        return false;
    }

    private void updateColorPreferenceState(int colorType) {
        if (mAmbientTextColor != null) {
            // Enable color picker only when custom color is selected (value == 2)
            mAmbientTextColor.setEnabled(colorType == 2);
        }
    }

    private void updateMasterSwitchState() {
        if (mAmbientEnable != null) {
            boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(getContext());
            boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(getContext());
            mAmbientEnable.setChecked(textEnabled || imageEnabled);
        }
    }

    private void updateCategoryVisibility() {
        // Update text category visibility - always visible so users can see options
        if (textCategory != null) {
            textCategory.setVisible(true);
        }

        // Update image category visibility - always visible so users can see options
        if (imageCategory != null) {
            imageCategory.setVisible(true);
        }
    }

    private void updatePreview() {
        if (mPreviewText == null || mPreviewImage == null) {
            return;
        }

        Context context = getContext();

        // Update text
        boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(context);
        if (textEnabled) {
            String text = AmbientCustomizationsHelper.getAmbientText(context);
            if (text == null || text.isEmpty()) {
                text = "Ambient Text";
            }
            mPreviewText.setText(text);
            mPreviewText.setVisibility(View.VISIBLE);

            // Apply text styling
            int textSize = AmbientCustomizationsHelper.getAmbientTextSize(context);
            mPreviewText.setTextSize(textSize);

            int alignment = AmbientCustomizationsHelper.getAmbientTextAlignment(context);
            applyAlignment(mPreviewText, alignment);

            int colorType = AmbientCustomizationsHelper.getAmbientTextTypeColor(context);
            int color = getTextColor(context, colorType);
            mPreviewText.setTextColor(color);

        } else {
            mPreviewText.setVisibility(View.GONE);
        }

        // Update image
        boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(context);
        if (imageEnabled) {
            String imageFile = AmbientCustomizationsHelper.getAmbientImageFile(context);
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    java.io.File file = new java.io.File(imageFile);
                    if (file.exists()) {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageFile);
                        if (bitmap != null) {
                            mPreviewImage.setImageBitmap(bitmap);
                            mPreviewImage.setVisibility(View.VISIBLE);
                        } else {
                            mPreviewImage.setVisibility(View.GONE);
                        }
                    } else {
                        mPreviewImage.setVisibility(View.GONE);
                    }
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

    private void applyAlignment(TextView textView, int alignment) {
        switch (alignment) {
            case 0: // Start Top
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.TOP);
                break;
            case 1: // Start Center
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 2: // Start Bottom
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.BOTTOM);
                break;
            case 3: // Center
            default:
                textView.setGravity(android.view.Gravity.CENTER);
                break;
            case 4: // End Top
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.TOP);
                break;
            case 5: // End Center
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 6: // End Bottom
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.BOTTOM);
                break;
        }
    }

    private int getTextColor(Context context, int colorType) {
        switch (colorType) {
            case 0: // Accent color
                return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
            case 1: // Wallpaper color
                return getWallpaperDominantColor(context);
            case 2: // Custom color
                return AmbientCustomizationsHelper.getAmbientTextColor(context);
            default:
                return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
        }
    }

    private int getWallpaperDominantColor(Context context) {
        try {
            android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(context);
            android.graphics.drawable.Drawable wallpaper = wallpaperManager.getDrawable();
            if (wallpaper instanceof android.graphics.drawable.BitmapDrawable) {
                android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) wallpaper).getBitmap();
                if (bitmap != null) {
                    // Simple color extraction - use a pixel from the center
                    return bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get wallpaper color", e);
        }
        return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
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
        try {
            // Color picker implementation not available - show placeholder
            android.widget.Toast.makeText(getContext(), getString(R.string.ambient_color_picker_not_implemented), android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show color picker placeholder", e);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
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
                // Copy image to device-protected storage and update settings
                copyImageToInternalStorage(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, result);
    }

    private void copyImageToInternalStorage(Uri imageUri) {
        try {
            Context deviceProtectedContext = getContext().createDeviceProtectedStorageContext();
            if (deviceProtectedContext == null) {
                Toast.makeText(getContext(), "Failed to access device storage", Toast.LENGTH_SHORT).show();
                return;
            }

            File filesDir = deviceProtectedContext.getFilesDir();
            File imageFile = new File(filesDir, "custom_file_ambient_image");

            // Copy the image file
            try (InputStream input = getContext().getContentResolver().openInputStream(imageUri);
                 FileOutputStream output = new FileOutputStream(imageFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
            }

            // Update settings with file path and URI for future reference
            AmbientCustomizationsHelper.setAmbientCustomImage(getContext(), imageUri.toString());
            AmbientCustomizationsHelper.setAmbientImageFile(getContext(), imageFile.getAbsolutePath());

            if (mAmbientCustomImage != null) {
                mAmbientCustomImage.setSummary(R.string.ambient_image_selected);
            }

            Toast.makeText(getContext(), "Ambient image updated", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Failed to copy ambient image", e);
            Toast.makeText(getContext(), "Failed to save ambient image", Toast.LENGTH_SHORT).show();
        }
    }

    // AOD Clock Style Methods
    private static final String KEY_AOD_CLOCK_STYLE = "aod_clock_style";

    private int getCurrentClockStyle() {
        try {
            return Settings.Secure.getInt(getContext().getContentResolver(),
                    KEY_AOD_CLOCK_STYLE, AODClockView.STYLE_CLASSIC);
        } catch (Exception e) {
            return AODClockView.STYLE_CLASSIC;
        }
    }

    private void setClockStyle(int style) {
        Settings.Secure.putInt(getContext().getContentResolver(),
                KEY_AOD_CLOCK_STYLE, style);
        if (mPreviewClock != null) {
            mPreviewClock.setClockStyle(style);
        }
    }

    private void updateClockStyleSummary(int style) {
        if (mClockStylePreference != null) {
            String[] entries = getResources().getStringArray(R.array.aod_clock_style_entries);
            if (entries != null && style >= 0 && style < entries.length) {
                mClockStylePreference.setSummary(entries[style]);
            }
        }
    }

    // Music Visualizer Methods
    private static final String KEY_MUSIC_VISUALIZER_MODE = "music_visualizer_mode";

    private int getCurrentVisualizerMode() {
        try {
            return Settings.Secure.getInt(getContext().getContentResolver(),
                    KEY_MUSIC_VISUALIZER_MODE, AmbientMusicVisualizer.MODE_SPECTRUM);
        } catch (Exception e) {
            return AmbientMusicVisualizer.MODE_SPECTRUM;
        }
    }

    private void setVisualizerMode(int mode) {
        Settings.Secure.putInt(getContext().getContentResolver(),
                KEY_MUSIC_VISUALIZER_MODE, mode);
        if (mMusicVisualizer != null) {
            mMusicVisualizer.setVisualizationMode(mode);
        }
    }

    private void updateVisualizerModeSummary(int mode) {
        if (mVisualizerModePreference != null) {
            String[] entries = getResources().getStringArray(R.array.music_visualizer_mode_entries);
            if (entries != null && mode >= 0 && mode < entries.length) {
                mVisualizerModePreference.setSummary(entries[mode]);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Preview updates automatically when settings change
    }

    @Override
    public void onPause() {
        super.onPause();
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

