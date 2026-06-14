/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.CustomEditTextPreferenceCompat;
import com.android.settingslib.search.SearchIndexable;
import com.bmobile.ambient.NotificationPreviewManager;
import com.bmobile.customization.AmbientCustomizationsHelper;
import com.bmobile.view.AmbientNotificationView;

@SearchIndexable
public class AmbientCustomizations extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AmbientCustomizations";

    private static final String KEY_AMBIENT_TEXT = "ambient_text";
    private static final String KEY_AMBIENT_TEXT_ALIGN = "ambient_text_align";

    // TODO(dev): re-enable when custom AOD clock overlay is fixed in SystemUI.
    // private static final String KEY_AOD_CLOCK_STYLE = "aod_clock_style";
    // TODO(dev): re-enable when ambient music visualizer playback hook is wired.
    // private static final String KEY_MUSIC_VISUALIZER_MODE = "music_visualizer_mode";

    private SwitchPreferenceCompat mAmbientEnable;
    private SwitchPreferenceCompat mAmbientTextAnimation;
    private SeekBarPreference mAmbientTextSize;
    private CustomEditTextPreferenceCompat mAmbientText;
    private ListPreference mAmbientTextAlign;

    private TextView mPreviewText;
    private AmbientNotificationView mNotificationPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AmbientCustomizationsHelper.disableExperimentalAmbientFeatures(getContext());
        addPreferencesFromResource(R.xml.ambient_customization);

        mAmbientEnable = findPreference("ambient_customization_enable");
        mAmbientTextAnimation = findPreference("ambient_text_animation");
        mAmbientTextSize = findPreference("ambient_text_size");
        mAmbientText = findPreference(KEY_AMBIENT_TEXT);
        mAmbientTextAlign = findPreference(KEY_AMBIENT_TEXT_ALIGN);

        if (mAmbientEnable != null) {
            mAmbientEnable.setChecked(
                    AmbientCustomizationsHelper.isAmbientDisplayEnabled(getContext()));
            mAmbientEnable.setOnPreferenceChangeListener(this);
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

        updatePreview();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mPreviewText = view.findViewById(R.id.ambient_preview_text);
            final View previewImage = view.findViewById(R.id.ambient_preview_image);
            if (previewImage != null) {
                previewImage.setVisibility(View.GONE);
            }

            // TODO(dev): custom AOD clock preview disabled until overlay path is fixed.
            final View previewClock = view.findViewById(R.id.ambient_preview_clock);
            if (previewClock != null) {
                previewClock.setVisibility(View.GONE);
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

            // TODO(dev): music visualizer preview disabled until feature is re-enabled.
            final View musicVisualizer = view.findViewById(R.id.ambient_preview_visualizer);
            if (musicVisualizer != null) {
                musicVisualizer.setVisibility(View.GONE);
            }

            updatePreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind preview views", e);
            mPreviewText = null;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (getActivity() == null || getActivity().getContentResolver() == null) {
            return false;
        }
        try {
            if (preference == mAmbientEnable) {
                AmbientCustomizationsHelper.setAmbientDisplayEnabled(getContext(),
                        (Boolean) newValue);
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
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling preference change", e);
            return false;
        }
        return false;
    }

    private void updatePreview() {
        if (mPreviewText == null) {
            return;
        }
        final Context context = getContext();
        if (AmbientCustomizationsHelper.isAmbientDisplayEnabled(context)) {
            String text = AmbientCustomizationsHelper.getAmbientText(context);
            if (text == null || text.isEmpty()) {
                text = getString(R.string.ambient_text_title);
            }
            mPreviewText.setText(text);
            mPreviewText.setVisibility(View.VISIBLE);
            mPreviewText.setTextSize(AmbientCustomizationsHelper.getAmbientTextSize(context));
            applyAlignment(mPreviewText,
                    AmbientCustomizationsHelper.getAmbientTextAlignment(context));
            mPreviewText.setTextColor(
                    com.android.settingslib.Utils.getColorAccentDefaultColor(context));
        } else {
            mPreviewText.setVisibility(View.GONE);
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

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.ambient_customization);
}
