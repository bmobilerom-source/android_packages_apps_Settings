/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;
import com.bmobile.ambient.NotificationPreviewManager;
import com.bmobile.customization.AmbientCustomizationsHelper;
import com.bmobile.view.AODClockView;
import com.bmobile.view.AmbientMusicVisualizer;
import com.bmobile.view.AmbientNotificationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@SearchIndexable
public class AmbientCustomizations extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AmbientCustomizations";
    private static final int REQUEST_PICK_IMAGE = 1000;

    private static final String KEY_AMBIENT_TEXT = "ambient_text";
    private static final String KEY_AMBIENT_TEXT_ALIGN = "ambient_text_align";
    private static final String KEY_AMBIENT_TEXT_TYPE_COLOR = "ambient_text_type_color";
    private static final String KEY_AMBIENT_TEXT_COLOR = "ambient_text_color";
    private static final String KEY_AMBIENT_CUSTOM_IMAGE = "ambient_custom_image";
    private static final String KEY_AOD_CLOCK_STYLE = "aod_clock_style";
    private static final String KEY_MUSIC_VISUALIZER_MODE = "music_visualizer_mode";

    private SwitchPreferenceCompat mAmbientEnable;
    private SwitchPreferenceCompat mDozeAlwaysOn;
    private SwitchPreferenceCompat mAmbientTextToggle;
    private SwitchPreferenceCompat mAmbientTextAnimation;
    private SeekBarPreference mAmbientTextSize;
    private SwitchPreferenceCompat mAmbientImageToggle;
    private CustomEditTextPreferenceCompat mAmbientText;
    private ListPreference mAmbientTextAlign;
    private ListPreference mAmbientTextTypeColor;
    private Preference mAmbientTextColor;
    private Preference mAmbientCustomImage;
    private ListPreference mClockStylePreference;
    private ListPreference mVisualizerModePreference;
    private PreferenceCategory mTextCategory;
    private PreferenceCategory mImageCategory;

    private TextView mPreviewText;
    private ImageView mPreviewImage;
    private AODClockView mPreviewClock;
    private AmbientNotificationView mNotificationPreview;
    private AmbientMusicVisualizer mMusicVisualizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_customization);

        final ContentResolver resolver = getActivity().getContentResolver();

        mAmbientEnable = findPreference("ambient_customization_enable");
        mDozeAlwaysOn = findPreference("doze_always_on");
        mAmbientTextToggle = findPreference("ambient_text_enable");
        mAmbientTextAnimation = findPreference("ambient_text_animation");
        mAmbientTextSize = findPreference("ambient_text_size");
        mAmbientImageToggle = findPreference("ambient_image_enable");
        mTextCategory = findPreference("ambient_text_category");
        mAmbientText = findPreference(KEY_AMBIENT_TEXT);
        mAmbientTextAlign = findPreference(KEY_AMBIENT_TEXT_ALIGN);
        mAmbientTextTypeColor = findPreference(KEY_AMBIENT_TEXT_TYPE_COLOR);
        mAmbientTextColor = findPreference(KEY_AMBIENT_TEXT_COLOR);
        mImageCategory = findPreference("ambient_image_category");
        mAmbientCustomImage = findPreference(KEY_AMBIENT_CUSTOM_IMAGE);

        if (mAmbientEnable != null) {
            final boolean textEnabled =
                    AmbientCustomizationsHelper.isAmbientTextEnabled(getContext());
            final boolean imageEnabled =
                    AmbientCustomizationsHelper.isAmbientImageEnabled(getContext());
            mAmbientEnable.setChecked(textEnabled || imageEnabled);
            mAmbientEnable.setOnPreferenceChangeListener(this);
        }

        if (mDozeAlwaysOn != null) {
            final boolean alwaysOn = Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
            mDozeAlwaysOn.setChecked(alwaysOn);
            mDozeAlwaysOn.setOnPreferenceChangeListener(this);
        }

        if (mAmbientTextToggle != null) {
            mAmbientTextToggle.setChecked(
                    AmbientCustomizationsHelper.isAmbientTextEnabled(getContext()));
            mAmbientTextToggle.setOnPreferenceChangeListener(this);
        }

        if (mAmbientTextAnimation != null) {
            mAmbientTextAnimation.setChecked(
                    AmbientCustomizationsHelper.isAmbientTextAnimationEnabled(getContext()));
            mAmbientTextAnimation.setOnPreferenceChangeListener(this);
        }

        if (mAmbientTextSize != null) {
            mAmbientTextSize.setProgress(
                    AmbientCustomizationsHelper.getAmbientTextSize(getContext()));
            mAmbientTextSize.setOnPreferenceChangeListener(this);
        }

        if (mAmbientImageToggle != null) {
            mAmbientImageToggle.setChecked(
                    AmbientCustomizationsHelper.isAmbientImageEnabled(getContext()));
            mAmbientImageToggle.setOnPreferenceChangeListener(this);
        }

        if (mAmbientText != null) {
            final String textValue = AmbientCustomizationsHelper.getAmbientText(getContext());
            if (textValue != null && !textValue.isEmpty()) {
                mAmbientText.setText(textValue);
                mAmbientText.setSummary(textValue);
            } else {
                mAmbientText.setSummary(R.string.ambient_text_summary);
            }
            mAmbientText.setOnPreferenceChangeListener(this);
        }

        if (mAmbientTextAlign != null) {
            final int align = AmbientCustomizationsHelper.getAmbientTextAlignment(getContext());
            mAmbientTextAlign.setValue(String.valueOf(align));
            mAmbientTextAlign.setSummary(
                    AmbientCustomizationsHelper.getAlignmentSummary(getContext(), align));
            mAmbientTextAlign.setOnPreferenceChangeListener(this);
        }

        if (mAmbientTextTypeColor != null) {
            final int colorType =
                    AmbientCustomizationsHelper.getAmbientTextTypeColor(getContext());
            mAmbientTextTypeColor.setValue(String.valueOf(colorType));
            final int index = mAmbientTextTypeColor.findIndexOfValue(String.valueOf(colorType));
            if (index >= 0) {
                mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
            }
            mAmbientTextTypeColor.setOnPreferenceChangeListener(this);
            updateColorPreferenceState(colorType);
        }

        if (mAmbientTextColor != null) {
            updateColorSummary(AmbientCustomizationsHelper.getAmbientTextColor(getContext()));
            mAmbientTextColor.setOnPreferenceClickListener(preference -> {
                showColorPickerDialog();
                return true;
            });
        }

        if (mAmbientCustomImage != null) {
            final String imageUri = AmbientCustomizationsHelper.getAmbientCustomImage(getContext());
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

        mClockStylePreference = findPreference(KEY_AOD_CLOCK_STYLE);
        if (mClockStylePreference != null) {
            mClockStylePreference.setOnPreferenceChangeListener(this);
            final int currentStyle = getCurrentClockStyle();
            mClockStylePreference.setValue(String.valueOf(currentStyle));
            updateClockStyleSummary(currentStyle);
        }

        mVisualizerModePreference = findPreference(KEY_MUSIC_VISUALIZER_MODE);
        if (mVisualizerModePreference != null) {
            mVisualizerModePreference.setOnPreferenceChangeListener(this);
            final int currentMode = getCurrentVisualizerMode();
            mVisualizerModePreference.setValue(String.valueOf(currentMode));
            updateVisualizerModeSummary(currentMode);
        }

        updateCategoryVisibility();
        updatePreview();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mPreviewText = view.findViewById(R.id.ambient_preview_text);
            mPreviewImage = view.findViewById(R.id.ambient_preview_image);
            mPreviewClock = view.findViewById(R.id.ambient_preview_clock);
            if (mPreviewClock != null) {
                final int clockStyle = getCurrentClockStyle();
                mPreviewClock.setClockStyle(clockStyle);
                mPreviewClock.setColors(
                        AmbientCustomizationsHelper.getAmbientTextColor(getContext()),
                        Color.parseColor("#B0B0B0"),
                        Color.parseColor("#00BFFF"));
            }

            mNotificationPreview = view.findViewById(R.id.ambient_preview_notification);
            if (mNotificationPreview != null) {
                final NotificationPreviewManager.NotificationPreview samplePreview =
                        new NotificationPreviewManager.NotificationPreview();
                samplePreview.smartText = "Sample notification preview";
                samplePreview.category = NotificationPreviewManager.CATEGORY_MESSAGE;
                samplePreview.packageName = "com.example.messaging";
                mNotificationPreview.setNotificationPreview(samplePreview);
            }

            mMusicVisualizer = view.findViewById(R.id.ambient_preview_visualizer);
            if (mMusicVisualizer != null) {
                mMusicVisualizer.setVisualizationMode(getCurrentVisualizerMode());
                mMusicVisualizer.setColors(
                        AmbientCustomizationsHelper.getAmbientTextColor(getContext()),
                        Color.parseColor("#FF1493"),
                        Color.parseColor("#32CD32"));
            }
            updatePreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind preview views", e);
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
        final ContentResolver resolver = getActivity().getContentResolver();
        try {
            if (preference == mAmbientEnable) {
                final boolean enabled = (Boolean) newValue;
                if (mAmbientTextToggle != null) {
                    mAmbientTextToggle.setChecked(enabled);
                    AmbientCustomizationsHelper.setAmbientTextEnabled(getContext(), enabled);
                }
                if (mAmbientImageToggle != null) {
                    mAmbientImageToggle.setChecked(enabled);
                    AmbientCustomizationsHelper.setAmbientImageEnabled(getContext(), enabled);
                }
                updatePreview();
                return true;
            } else if (preference == mDozeAlwaysOn) {
                final boolean alwaysOn = (Boolean) newValue;
                Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_ALWAYS_ON,
                        alwaysOn ? 1 : 0, UserHandle.USER_CURRENT);
                return true;
            } else if (preference == mAmbientTextToggle) {
                AmbientCustomizationsHelper.setAmbientTextEnabled(getContext(), (Boolean) newValue);
                updateMasterSwitchState();
                updatePreview();
                return true;
            } else if (preference == mAmbientTextAnimation) {
                AmbientCustomizationsHelper.setAmbientTextAnimationEnabled(getContext(),
                        (Boolean) newValue);
                updatePreview();
                return true;
            } else if (preference == mAmbientTextSize) {
                AmbientCustomizationsHelper.setAmbientTextSize(getContext(), (Integer) newValue);
                updatePreview();
                return true;
            } else if (preference == mAmbientImageToggle) {
                AmbientCustomizationsHelper.setAmbientImageEnabled(getContext(), (Boolean) newValue);
                updateMasterSwitchState();
                updatePreview();
                return true;
            } else if (preference == mAmbientText) {
                final String value = (String) newValue;
                AmbientCustomizationsHelper.setAmbientText(getContext(), value);
                preference.setSummary((value != null && !value.isEmpty())
                        ? value : getString(R.string.ambient_text_summary));
                updatePreview();
                return true;
            } else if (preference == mAmbientTextAlign) {
                final int align = Integer.parseInt((String) newValue);
                AmbientCustomizationsHelper.setAmbientTextAlignment(getContext(), align);
                mAmbientTextAlign.setSummary(
                        AmbientCustomizationsHelper.getAlignmentSummary(getContext(), align));
                updatePreview();
                return true;
            } else if (preference == mAmbientTextTypeColor) {
                final int value = Integer.parseInt((String) newValue);
                final int index = mAmbientTextTypeColor.findIndexOfValue((String) newValue);
                if (index >= 0) {
                    mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntries()[index]);
                }
                AmbientCustomizationsHelper.setAmbientTextTypeColor(getContext(), value);
                updateColorPreferenceState(value);
                updatePreview();
                return true;
            } else if (preference == mClockStylePreference) {
                final int style = Integer.parseInt((String) newValue);
                setClockStyle(style);
                updateClockStyleSummary(style);
                return true;
            } else if (preference == mVisualizerModePreference) {
                final int mode = Integer.parseInt((String) newValue);
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
            mAmbientTextColor.setEnabled(colorType == 2);
        }
    }

    private void updateMasterSwitchState() {
        if (mAmbientEnable != null) {
            mAmbientEnable.setChecked(AmbientCustomizationsHelper.isAmbientTextEnabled(getContext())
                    || AmbientCustomizationsHelper.isAmbientImageEnabled(getContext()));
        }
    }

    private void updateCategoryVisibility() {
        if (mTextCategory != null) {
            mTextCategory.setVisible(true);
        }
        if (mImageCategory != null) {
            mImageCategory.setVisible(true);
        }
    }

    private void updatePreview() {
        if (mPreviewText == null || mPreviewImage == null) {
            return;
        }
        final Context context = getContext();
        if (AmbientCustomizationsHelper.isAmbientTextEnabled(context)) {
            String text = AmbientCustomizationsHelper.getAmbientText(context);
            if (text == null || text.isEmpty()) {
                text = getString(R.string.ambient_text_title);
            }
            mPreviewText.setText(text);
            mPreviewText.setVisibility(View.VISIBLE);
            mPreviewText.setTextSize(AmbientCustomizationsHelper.getAmbientTextSize(context));
            applyAlignment(mPreviewText,
                    AmbientCustomizationsHelper.getAmbientTextAlignment(context));
            mPreviewText.setTextColor(getTextColor(context,
                    AmbientCustomizationsHelper.getAmbientTextTypeColor(context)));
        } else {
            mPreviewText.setVisibility(View.GONE);
        }

        if (AmbientCustomizationsHelper.isAmbientImageEnabled(context)) {
            final String imageFile = AmbientCustomizationsHelper.getAmbientImageFile(context);
            if (imageFile != null && !imageFile.isEmpty()) {
                final File file = new File(imageFile);
                if (file.exists()) {
                    mPreviewImage.setImageBitmap(BitmapFactory.decodeFile(imageFile));
                    mPreviewImage.setVisibility(View.VISIBLE);
                } else {
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
            case 0:
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.TOP);
                break;
            case 1:
                textView.setGravity(
                        android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 2:
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.BOTTOM);
                break;
            case 4:
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.TOP);
                break;
            case 5:
                textView.setGravity(
                        android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 6:
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.BOTTOM);
                break;
            case 3:
            default:
                textView.setGravity(android.view.Gravity.CENTER);
                break;
        }
    }

    private int getTextColor(Context context, int colorType) {
        switch (colorType) {
            case 1:
                return getWallpaperDominantColor(context);
            case 2:
                return AmbientCustomizationsHelper.getAmbientTextColor(context);
            case 0:
            default:
                return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
        }
    }

    private int getWallpaperDominantColor(Context context) {
        try {
            final android.graphics.drawable.Drawable wallpaper =
                    android.app.WallpaperManager.getInstance(context).getDrawable();
            if (wallpaper instanceof BitmapDrawable) {
                final android.graphics.Bitmap bitmap = ((BitmapDrawable) wallpaper).getBitmap();
                if (bitmap != null) {
                    return bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get wallpaper color", e);
        }
        return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
    }

    private void updateColorSummary(int color) {
        if (mAmbientTextColor == null) {
            return;
        }
        if (color == 0xFF3980FF) {
            mAmbientTextColor.setSummary(R.string.default_string);
        } else {
            mAmbientTextColor.setSummary(String.format("#%08X", color));
        }
    }

    private void showColorPickerDialog() {
        Toast.makeText(getContext(), R.string.ambient_color_picker_not_implemented,
                Toast.LENGTH_SHORT).show();
    }

    private void pickImage() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK
                && result != null && result.getData() != null) {
            copyImageToInternalStorage(result.getData());
        }
        super.onActivityResult(requestCode, resultCode, result);
    }

    private void copyImageToInternalStorage(Uri imageUri) {
        try {
            final Context deviceProtectedContext =
                    getContext().createDeviceProtectedStorageContext();
            if (deviceProtectedContext == null) {
                Toast.makeText(getContext(), "Failed to access device storage", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            final File imageFile = new File(deviceProtectedContext.getFilesDir(),
                    "custom_file_ambient_image");
            try (InputStream input = getContext().getContentResolver().openInputStream(imageUri);
                    FileOutputStream output = new FileOutputStream(imageFile)) {
                final byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
            AmbientCustomizationsHelper.setAmbientCustomImage(getContext(), imageUri.toString());
            AmbientCustomizationsHelper.setAmbientImageFile(getContext(),
                    imageFile.getAbsolutePath());
            if (mAmbientCustomImage != null) {
                mAmbientCustomImage.setSummary(R.string.ambient_image_selected);
            }
            updatePreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy ambient image", e);
            Toast.makeText(getContext(), "Failed to save ambient image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private int getCurrentClockStyle() {
        return Settings.Secure.getInt(getContext().getContentResolver(), KEY_AOD_CLOCK_STYLE,
                AODClockView.STYLE_CLASSIC);
    }

    private void setClockStyle(int style) {
        Settings.Secure.putInt(getContext().getContentResolver(), KEY_AOD_CLOCK_STYLE, style);
        if (mPreviewClock != null) {
            mPreviewClock.setClockStyle(style);
        }
    }

    private void updateClockStyleSummary(int style) {
        if (mClockStylePreference != null && style >= 0
                && style < mClockStylePreference.getEntries().length) {
            mClockStylePreference.setSummary(mClockStylePreference.getEntries()[style]);
        }
    }

    private int getCurrentVisualizerMode() {
        return Settings.Secure.getInt(getContext().getContentResolver(), KEY_MUSIC_VISUALIZER_MODE,
                AmbientMusicVisualizer.MODE_SPECTRUM);
    }

    private void setVisualizerMode(int mode) {
        Settings.Secure.putInt(getContext().getContentResolver(), KEY_MUSIC_VISUALIZER_MODE, mode);
        if (mMusicVisualizer != null) {
            mMusicVisualizer.setVisualizationMode(mode);
        }
    }

    private void updateVisualizerModeSummary(int mode) {
        if (mVisualizerModePreference != null && mode >= 0
                && mode < mVisualizerModePreference.getEntries().length) {
            mVisualizerModePreference.setSummary(mVisualizerModePreference.getEntries()[mode]);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.ambient_customization);
}
