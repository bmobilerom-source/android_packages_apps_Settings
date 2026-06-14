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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomGradientsSettings extends DashboardFragment {
    private static final String TAG = "CustomGradientsSettings";

    // Predefined dark color gradients (10 colors each)
    private static final GradientPreset[] GRADIENT_PRESETS = {
        new GradientPreset("midnight_ocean", "Midnight Ocean",
            new int[]{Color.parseColor("#0F1419"), Color.parseColor("#1A237E"),
                     Color.parseColor("#3949AB"), Color.parseColor("#5E35B1"),
                     Color.parseColor("#311B92"), Color.parseColor("#1A237E"),
                     Color.parseColor("#0D1B2A"), Color.parseColor("#263238"),
                     Color.parseColor("#37474F"), Color.parseColor("#455A64")}),

        new GradientPreset("charcoal_spectrum", "Charcoal Spectrum",
            new int[]{Color.parseColor("#0F0F0F"), Color.parseColor("#1C1C1C"),
                     Color.parseColor("#2D2D2D"), Color.parseColor("#3E3E3E"),
                     Color.parseColor("#4F4F4F"), Color.parseColor("#606060"),
                     Color.parseColor("#717171"), Color.parseColor("#828282"),
                     Color.parseColor("#939393"), Color.parseColor("#A4A4A4")}),

        new GradientPreset("ebony_depths", "Ebony Depths",
            new int[]{Color.parseColor("#0A0A0A"), Color.parseColor("#141414"),
                     Color.parseColor("#1F1F1F"), Color.parseColor("#2A2A2A"),
                     Color.parseColor("#353535"), Color.parseColor("#404040"),
                     Color.parseColor("#4B4B4B"), Color.parseColor("#565656"),
                     Color.parseColor("#616161"), Color.parseColor("#6C6C6C")}),

        new GradientPreset("obsidian_void", "Obsidian Void",
            new int[]{Color.parseColor("#000000"), Color.parseColor("#0A0A0A"),
                     Color.parseColor("#141414"), Color.parseColor("#1F1F1F"),
                     Color.parseColor("#292929"), Color.parseColor("#333333"),
                     Color.parseColor("#3D3D3D"), Color.parseColor("#474747"),
                     Color.parseColor("#525252"), Color.parseColor("#5C5C5C")}),

        new GradientPreset("deep_purple_night", "Deep Purple Night",
            new int[]{Color.parseColor("#0D1B2A"), Color.parseColor("#1A1A2E"),
                     Color.parseColor("#16213E"), Color.parseColor("#0F3460"),
                     Color.parseColor("#1A1A2E"), Color.parseColor("#16213E"),
                     Color.parseColor("#533483"), Color.parseColor("#7C3AED"),
                     Color.parseColor("#8B5CF6"), Color.parseColor("#A78BFA")}),

        new GradientPreset("dark_teal_ocean", "Dark Teal Ocean",
            new int[]{Color.parseColor("#0F1419"), Color.parseColor("#1A252F"),
                     Color.parseColor("#263238"), Color.parseColor("#37474F"),
                     Color.parseColor("#455A64"), Color.parseColor("#546E7A"),
                     Color.parseColor("#607D8B"), Color.parseColor("#78909C"),
                     Color.parseColor("#90A4AE"), Color.parseColor("#B0BEC5")}),

        new GradientPreset("pitch_black_gradient", "Pitch Black Gradient",
            new int[]{Color.parseColor("#000000"), Color.parseColor("#050505"),
                     Color.parseColor("#0A0A0A"), Color.parseColor("#0F0F0F"),
                     Color.parseColor("#141414"), Color.parseColor("#191919"),
                     Color.parseColor("#1E1E1E"), Color.parseColor("#232323"),
                     Color.parseColor("#282828"), Color.parseColor("#2D2D2D")}),

        new GradientPreset("navy_shadow_depth", "Navy Shadow Depth",
            new int[]{Color.parseColor("#0D1B2A"), Color.parseColor("#1B2631"),
                     Color.parseColor("#243447"), Color.parseColor("#2D4A5D"),
                     Color.parseColor("#355F73"), Color.parseColor("#3E7489"),
                     Color.parseColor("#46899F"), Color.parseColor("#4F9EB5"),
                     Color.parseColor("#58B3CB"), Color.parseColor("#61C8E1")}),

        new GradientPreset("dark_forest_mist", "Dark Forest Mist",
            new int[]{Color.parseColor("#0F1419"), Color.parseColor("#1B2631"),
                     Color.parseColor("#243447"), Color.parseColor("#2D4A5D"),
                     Color.parseColor("#355F73"), Color.parseColor("#3E7489"),
                     Color.parseColor("#46899F"), Color.parseColor("#4F9EB5"),
                     Color.parseColor("#58B3CB"), Color.parseColor("#61C8E1")}),

        new GradientPreset("void_energy", "Void Energy",
            new int[]{Color.parseColor("#000000"), Color.parseColor("#0F0F23"),
                     Color.parseColor("#1E1E3F"), Color.parseColor("#2D2D5F"),
                     Color.parseColor("#3C3C7F"), Color.parseColor("#4B4B9F"),
                     Color.parseColor("#5A5ABF"), Color.parseColor("#6969DF"),
                     Color.parseColor("#7878FF"), Color.parseColor("#8787FF")})
    };

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
        return R.xml.custom_gradients_settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutPreference layoutPreference = findPreference("custom_gradients_grid_pref");
        if (layoutPreference == null) {
            return;
        }

        LinearLayout container = layoutPreference.findViewById(R.id.gradients_container);
        if (container == null) {
            return;
        }

        Context context = requireContext();
        container.removeAllViews();

        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(2);
        gridLayout.setUseDefaultMargins(true);

        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        gridParams.setMargins(16, 16, 16, 16);
        gridLayout.setLayoutParams(gridParams);

        for (GradientPreset preset : GRADIENT_PRESETS) {
            gridLayout.addView(createGradientCard(context, preset));
        }

        container.addView(gridLayout);
    }

    private View createGradientCard(Context context, GradientPreset preset) {
        // Create card layout
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(context.getColor(R.color.monet_preset_card_background));
        int padding = context.getResources().getDimensionPixelSize(R.dimen.monet_preset_card_padding);
        card.setPadding(padding, padding, padding, padding);
        card.setClickable(true);
        card.setFocusable(true);

        GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
        cardParams.width = context.getResources().getDimensionPixelSize(R.dimen.monet_preset_card_width);
        cardParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        cardParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.monet_preset_card_margin);
        cardParams.setMargins(margin, margin, margin, margin);
        card.setLayoutParams(cardParams);

        // Add gradient preview
        View gradientView = new View(context);
        LinearLayout.LayoutParams gradientParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            context.getResources().getDimensionPixelSize(R.dimen.monet_preset_swatch_height));
        gradientParams.setMargins(0, 0, 0, context.getResources().getDimensionPixelSize(R.dimen.monet_color_swatch_margin));
        gradientView.setLayoutParams(gradientParams);

        // Create gradient drawable
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, preset.colors);
        float radius = context.getResources().getDimension(R.dimen.monet_color_swatch_corner_radius);
        gradient.setCornerRadius(radius);
        gradientView.setBackground(gradient);

        // Add title
        TextView titleView = new TextView(context);
        titleView.setText(preset.name);
        titleView.setTextColor(context.getColor(R.color.monet_preset_text_color));
        titleView.setTextSize(context.getResources().getDimension(R.dimen.monet_preset_title_text_size) / context.getResources().getDisplayMetrics().density);
        titleView.setGravity(android.view.Gravity.CENTER);

        // Add click listener
        card.setOnClickListener(v -> applyGradient(preset));

        card.addView(gradientView);
        card.addView(titleView);

        return card;
    }

    private void applyGradient(GradientPreset preset) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        Settings.Secure.putString(context.getContentResolver(),
            Settings.Secure.MONET_COLOR_GRADIENT, preset.id);

        StringBuilder colorString = new StringBuilder();
        for (int i = 0; i < preset.colors.length; i++) {
            if (i > 0) colorString.append(",");
            colorString.append(String.format("#%06X", 0xFFFFFF & preset.colors[i]));
        }
        Settings.Secure.putString(context.getContentResolver(),
            Settings.Secure.MONET_GRADIENT_COLORS, colorString.toString());
        Settings.Secure.putInt(context.getContentResolver(),
            Settings.Secure.MONET_GRADIENT_ENABLED, 1);

        String style = MonetThemeApplier.getCurrentStyle(context);
        MonetThemeApplier.applyPreset(context, preset.colors[2], style,
                Math.abs(preset.id.hashCode()) % 1000);

        showGradientAppliedMessage(preset.name);
    }

    private void showGradientAppliedMessage(String gradientName) {
        // This would show a toast or snackbar
        android.util.Log.d(TAG, "Applied gradient: " + gradientName);
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
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.custom_gradients_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "custom_gradients_settings";
    }

    private static class GradientPreset {
        final String id;
        final String name;
        final int[] colors;

        GradientPreset(String id, String name, int[] colors) {
            this.id = id;
            this.name = name;
            this.colors = colors;
        }
    }
}
