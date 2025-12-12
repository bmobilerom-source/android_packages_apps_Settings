/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class MonetColorSettings extends DashboardFragment {
    private static final String TAG = "MonetColorSettings";

    private WallpaperManager mWallpaperManager;
    private UiModeManager mUiModeManager;
    private Executor mExecutor;
    private android.app.wallpaper.WallpaperColors mWallpaperColors;

    // UI components
    private View mWallpaperPreview;
    private LinearLayout mColorSwatchesContainer;
    private TextView mCurrentStyleText;
    private ListPreference mColorStylePreference;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.monet_color_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mWallpaperManager = WallpaperManager.getInstance(getContext());
        mUiModeManager = getContext().getSystemService(UiModeManager.class);
        mExecutor = Executors.newSingleThreadExecutor();
        loadWallpaperColors();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Initialize UI components
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        // Find views for dynamic content
        mWallpaperPreview = view.findViewById(R.id.wallpaper_preview);
        mColorSwatchesContainer = view.findViewById(R.id.color_swatches_container);
        mCurrentStyleText = view.findViewById(R.id.current_style_text);

        // Set up wallpaper preview
        setupWallpaperPreview();

        // Update color swatches
        updateColorSwatches();

        // Update current style
        updateCurrentStyle();
    }

    private void loadWallpaperColors() {
        mExecutor.execute(() -> {
            try {
                mWallpaperColors = mWallpaperManager.getWallpaperColors(
                    WallpaperManager.FLAG_SYSTEM);
                getActivity().runOnUiThread(() -> {
                    updateColorSwatches();
                    updateWallpaperPreview();
                });
            } catch (Exception e) {
                // Handle error
            }
        });
    }

    private void updateWallpaperPreview() {
        if (mWallpaperPreview != null) {
            try {
                Drawable wallpaper = mWallpaperManager.getDrawable();
                if (wallpaper != null) {
                    mWallpaperPreview.setBackground(wallpaper);
                }
            } catch (Exception e) {
                // Handle error - wallpaper might not be accessible
            }
        }
    }

    private void updateColorSwatches() {
        if (mColorSwatchesContainer == null || mWallpaperColors == null) return;

        mColorSwatchesContainer.removeAllViews();

        Context context = getContext();
        if (context == null) return;

        // Create color swatches
        int[] colors = getWallpaperColorArray();
        for (int color : colors) {
            View swatch = createColorSwatch(context, color);
            mColorSwatchesContainer.addView(swatch);
        }
    }

    private int[] getWallpaperColorArray() {
        List<Integer> colors = new ArrayList<>();

        if (mWallpaperColors != null) {
            // Primary color (most prominent)
            if (mWallpaperColors.getPrimaryColor() != null) {
                colors.add(mWallpaperColors.getPrimaryColor().toArgb());
            }

            // Secondary color (second most prominent)
            if (mWallpaperColors.getSecondaryColor() != null) {
                colors.add(mWallpaperColors.getSecondaryColor().toArgb());
            }

            // Tertiary color (third most prominent)
            if (mWallpaperColors.getTertiaryColor() != null) {
                colors.add(mWallpaperColors.getTertiaryColor().toArgb());
            }

            // Add a neutral variant based on primary color for the fourth swatch
            if (mWallpaperColors.getPrimaryColor() != null && colors.size() >= 1) {
                int primary = mWallpaperColors.getPrimaryColor().toArgb();
                // Create a neutral variant by desaturating and adjusting brightness
                float[] hsl = new float[3];
                android.graphics.Color.colorToHSV(primary, hsl);
                hsl[1] = Math.max(0.1f, hsl[1] * 0.3f); // Reduce saturation
                hsl[2] = Math.min(0.9f, Math.max(0.4f, hsl[2])); // Adjust brightness
                colors.add(android.graphics.Color.HSVToColor(hsl));
            }
        }

        // Fallback colors if wallpaper colors are not available
        if (colors.isEmpty()) {
            colors.add(Color.parseColor("#4285F4")); // Blue
            colors.add(Color.parseColor("#34A853")); // Green
            colors.add(Color.parseColor("#EA4335")); // Red
            colors.add(Color.parseColor("#FBBC05")); // Yellow
        }

        // Ensure we have exactly 4 colors
        while (colors.size() < 4) {
            colors.add(Color.GRAY);
        }

        return colors.stream().mapToInt(i -> i).toArray();
    }

    private View createColorSwatch(Context context, int color) {
        View swatch = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            context.getResources().getDimensionPixelSize(R.dimen.monet_color_swatch_size),
            context.getResources().getDimensionPixelSize(R.dimen.monet_color_swatch_size)
        );
        int margin = context.getResources().getDimensionPixelSize(R.dimen.monet_color_swatch_margin);
        params.setMargins(margin, 0, margin, 0);
        swatch.setLayoutParams(params);
        swatch.setBackgroundColor(color);

        // Add border
        swatch.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                float radius = context.getResources().getDimension(R.dimen.monet_color_swatch_corner_radius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        swatch.setClipToOutline(true);

        return swatch;
    }

    private void updateCurrentStyle() {
        if (mCurrentStyleText == null) return;

        // Get current monet style (this would need to be implemented in framework)
        String currentStyle = getCurrentMonetStyle();
        mCurrentStyleText.setText(getString(R.string.monet_current_style, currentStyle));
    }

    private String getCurrentMonetStyle() {
        // This would need to be implemented to read current monet style
        // For now, return default
        return "TONAL_SPOT";
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new MonetColorStyleController(context, "monet_color_style"));
        controllers.add(new MonetColorPresetsController(context, "monet_color_presets"));
        controllers.add(new CustomGradientsController(context, "monet_custom_gradients"));
        controllers.add(new TimeBasedColorsController(context, "monet_time_based_colors"));
        controllers.add(new ContextualColorsController(context, "monet_contextual_colors"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.monet_color_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "monet_color_settings";
    }
}
