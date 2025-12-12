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
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class MonetColorPresets extends DashboardFragment {
    private static final String TAG = "MonetColorPresets";

    // Dark color presets
    private static final ColorPreset[] DARK_PRESETS = {
        new ColorPreset("midnight_blue", "Midnight Blue", Color.parseColor("#1A237E")),
        new ColorPreset("charcoal", "Charcoal", Color.parseColor("#263238")),
        new ColorPreset("ebony", "Ebony", Color.parseColor("#0F0F0F")),
        new ColorPreset("obsidian", "Obsidian", Color.parseColor("#000000")),
        new ColorPreset("deep_purple", "Deep Purple", Color.parseColor("#311B92")),
        new ColorPreset("dark_teal", "Dark Teal", Color.parseColor("#004D40")),
        new ColorPreset("pitch_black", "Pitch Black", Color.parseColor("#000000")),
        new ColorPreset("navy_shadow", "Navy Shadow", Color.parseColor("#0D1B2A")),
        new ColorPreset("dark_forest", "Dark Forest", Color.parseColor("#1B2631")),
        new ColorPreset("void", "Void", Color.parseColor("#0A0A0A"))
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
        return R.xml.monet_color_presets;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Add custom preset grid
        addPresetGrid(view);

        return view;
    }

    private void addPresetGrid(View view) {
        LinearLayout container = view.findViewById(R.id.presets_container);
        if (container == null) return;

        Context context = getContext();
        if (context == null) return;

        // Create grid layout
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(2);
        gridLayout.setRowCount((DARK_PRESETS.length + 1) / 2); // Ceiling division
        gridLayout.setUseDefaultMargins(true);

        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        gridParams.setMargins(16, 16, 16, 16);
        gridLayout.setLayoutParams(gridParams);

        // Add preset cards
        for (ColorPreset preset : DARK_PRESETS) {
            View presetCard = createPresetCard(context, preset);
            gridLayout.addView(presetCard);
        }

        container.addView(gridLayout);
    }

    private View createPresetCard(Context context, ColorPreset preset) {
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

        // Add color swatch
        View colorSwatch = new View(context);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            context.getResources().getDimensionPixelSize(R.dimen.monet_preset_swatch_height));
        swatchParams.setMargins(0, 0, 0, context.getResources().getDimensionPixelSize(R.dimen.monet_color_swatch_margin));
        colorSwatch.setLayoutParams(swatchParams);
        colorSwatch.setBackgroundColor(preset.color);

        // Add outline for better visibility
        float radius = context.getResources().getDimension(R.dimen.monet_color_swatch_corner_radius);
        colorSwatch.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        colorSwatch.setClipToOutline(true);

        // Add title
        TextView titleView = new TextView(context);
        titleView.setText(preset.name);
        titleView.setTextColor(context.getColor(R.color.monet_preset_text_color));
        titleView.setTextSize(context.getResources().getDimension(R.dimen.monet_preset_title_text_size) / context.getResources().getDisplayMetrics().density);
        titleView.setGravity(android.view.Gravity.CENTER);

        // Add click listener
        card.setOnClickListener(v -> applyPreset(preset));

        card.addView(colorSwatch);
        card.addView(titleView);

        return card;
    }

    private void applyPreset(ColorPreset preset) {
        // Apply the preset colors like wallpaper picker does
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_COLOR_PRESET,
            preset.id);

        // Apply the actual colors (framework would handle this)
        String colorString = preset.colors[0] + "," + preset.colors[1] + "," +
                           preset.colors[2] + "," + preset.colors[3];
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_COLOR_PRESET_VALUES,
            colorString);

        // Override wallpaper colors with preset
        android.provider.Settings.Secure.putInt(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_COLOR_OVERRIDE_ENABLED, 1);

        // Notify system of color change (like wallpaper picker)
        notifyColorChange();

        // Show confirmation
        showPresetAppliedMessage(preset.name);
    }

    private void notifyColorChange() {
        try {
            // Send broadcast to notify color change
            Intent intent = new Intent("com.android.settings.MONET_COLORS_CHANGED");
            intent.setPackage("com.android.systemui");
            getContext().sendBroadcast(intent);

            // Also notify Settings app to refresh colors
            Intent settingsIntent = new Intent("com.android.settings.COLORS_CHANGED");
            getContext().sendBroadcast(settingsIntent);
        } catch (Exception e) {
            android.util.Log.e("MonetColorPresets", "Error notifying color change", e);
        }
    }

    private void showPresetAppliedMessage(String presetName) {
        // This would show a toast or snackbar
        // For now, just log
        android.util.Log.d(TAG, "Applied preset: " + presetName);
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
            new BaseSearchIndexProvider(R.xml.monet_color_presets) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "monet_color_presets";
    }

    private static class ColorPreset {
        final String id;
        final String name;
        final int color;

        ColorPreset(String id, String name, int color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }
    }
}
