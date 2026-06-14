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
import android.content.Intent;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Applies preset accent colors via the canonical ThemePicker / SystemUI overlay JSON contract.
 */
public final class MonetThemeApplier {
    private static final String OVERLAY_CATEGORY_COLOR = "android.theme.customization.accent_color";
    private static final String OVERLAY_CATEGORY_SYSTEM_PALETTE =
            "android.theme.customization.system_palette";
    private static final String OVERLAY_CATEGORY_THEME_STYLE =
            "android.theme.customization.theme_style";
    private static final String OVERLAY_COLOR_SOURCE = "android.theme.customization.color_source";
    private static final String OVERLAY_COLOR_INDEX = "android.theme.customization.color_index";
    private static final String OVERLAY_COLOR_BOTH = "android.theme.customization.color_both";
    private static final String COLOR_SOURCE_PRESET = "preset";

    private MonetThemeApplier() {
    }

    public static boolean applyPreset(Context context, int seedColor, String style, int index) {
        String hex = String.format("%06X", 0xFFFFFF & seedColor);
        String styleName = (style == null || style.isEmpty()) ? "TONAL_SPOT" : style.toUpperCase();

        try {
            String current = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);
            JSONObject object;
            if (current != null && !current.isEmpty()) {
                object = new JSONObject(current);
            } else {
                object = new JSONObject();
            }

            object.remove(OVERLAY_CATEGORY_SYSTEM_PALETTE);
            object.remove(OVERLAY_CATEGORY_COLOR);
            object.remove(OVERLAY_COLOR_SOURCE);
            object.remove(OVERLAY_CATEGORY_THEME_STYLE);
            object.remove(OVERLAY_COLOR_BOTH);

            object.put(OVERLAY_CATEGORY_SYSTEM_PALETTE, hex);
            object.put(OVERLAY_CATEGORY_COLOR, hex);
            object.put(OVERLAY_CATEGORY_THEME_STYLE, styleName);
            object.put(OVERLAY_COLOR_SOURCE, COLOR_SOURCE_PRESET);
            object.put(OVERLAY_COLOR_INDEX, String.valueOf(index));

            boolean saved = Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES, object.toString());
            if (saved) {
                triggerThemeRefresh(context);
            }
            return saved;
        } catch (JSONException e) {
            return false;
        }
    }

    public static int getCurrentSeedColor(Context context) {
        String current = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);
        if (current == null || current.isEmpty()) {
            return 0;
        }
        try {
            JSONObject object = new JSONObject(current);
            String hex = object.optString(OVERLAY_CATEGORY_SYSTEM_PALETTE, "");
            if (hex.isEmpty()) {
                hex = object.optString(OVERLAY_CATEGORY_COLOR, "");
            }
            if (hex.isEmpty()) {
                return 0;
            }
            return (int) Long.parseLong(hex, 16) | 0xFF000000;
        } catch (JSONException | NumberFormatException e) {
            return 0;
        }
    }

    private static void triggerThemeRefresh(Context context) {
        try {
            Intent wallpaperIntent = new Intent(Intent.ACTION_WALLPAPER_CHANGED);
            wallpaperIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            context.sendBroadcast(wallpaperIntent);

            Intent configIntent = new Intent(Intent.ACTION_CONFIGURATION_CHANGED);
            configIntent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            context.sendBroadcast(configIntent);
        } catch (Exception ignored) {
        }
    }
}
