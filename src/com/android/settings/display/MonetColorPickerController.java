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

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for custom color picker
 * Inspired by MonetCompat's user-selected wallpaper colors feature
 * In a full implementation, this would open a color picker dialog
 */
public class MonetColorPickerController extends BasePreferenceController {

    public MonetColorPickerController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary("Tap to choose a custom color for Material You theming");
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        // Open a simple color picker dialog
        showColorPickerDialog();

        return true;
    }

    private void showColorPickerDialog() {
        // Create a simple color picker using Android's ColorPickerDialog
        try {
            // Use array to hold mutable currentColor value (workaround for lambda final variable restriction)
            final int[] currentColorHolder = new int[1];
            int existing = MonetThemeApplier.getCurrentSeedColor(mContext);
            currentColorHolder[0] = existing != 0 ? existing : 0xFF6750A4;

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
            builder.setTitle("Choose Custom Seed Color");

            // Create a color picker view (simplified implementation)
            android.widget.LinearLayout layout = new android.widget.LinearLayout(mContext);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(32, 16, 32, 16);

            // Color preview
            android.widget.TextView colorPreview = new android.widget.TextView(mContext);
            colorPreview.setText("Selected Color Preview");
            colorPreview.setTextSize(16);
            colorPreview.setGravity(android.view.Gravity.CENTER);
            colorPreview.setPadding(16, 16, 16, 16);
            colorPreview.setBackgroundColor(currentColorHolder[0]);
            layout.addView(colorPreview);

            // Simple color buttons for common Material You colors
            android.widget.LinearLayout colorButtons = new android.widget.LinearLayout(mContext);
            colorButtons.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            colorButtons.setGravity(android.view.Gravity.CENTER);

            int[] materialColors = {
                0xFF6750A4, // Purple
                0xFF625B71, // Neutral
                0xFF1C1B1F, // Neutral Variant
                0xFF21005D, // Primary Dark
                0xFF381E72, // Secondary
                0xFF4F378B  // Tertiary
            };

            for (int color : materialColors) {
                android.widget.Button colorButton = new android.widget.Button(mContext);
                colorButton.setBackgroundColor(color);
                colorButton.setWidth(60);
                colorButton.setHeight(60);
                final int selectedColor = color; // Make effectively final for lambda
                colorButton.setOnClickListener(v -> {
                    currentColorHolder[0] = selectedColor;
                    colorPreview.setBackgroundColor(selectedColor);
                });
                colorButtons.addView(colorButton);
            }

            layout.addView(colorButtons);

            builder.setView(layout);
            builder.setPositiveButton("Apply", (dialog, which) -> {
                applyCustomColor(currentColorHolder[0]);
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();

        } catch (Exception e) {
            android.util.Log.e("MonetColorPicker", "Failed to show color picker", e);
            android.widget.Toast.makeText(mContext,
                "Color picker unavailable", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void applyCustomColor(int color) {
        try {
            String style = MonetThemeApplier.getCurrentStyle(mContext);
            MonetThemeApplier.applyPreset(mContext, color, style, 0);

            android.widget.Toast.makeText(mContext,
                "Custom color applied!", android.widget.Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            android.util.Log.e("MonetColorPicker", "Failed to apply custom color", e);
        }
    }
}
