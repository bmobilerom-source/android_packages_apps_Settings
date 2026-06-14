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
import android.graphics.Color;
import android.provider.Settings;

import java.util.HashMap;
import java.util.Map;

/** Applies Monet colors based on notification package names. */
public final class MonetContextualColorHelper {
    private static final Map<String, ContextualColor> CONTEXTUAL_COLORS = new HashMap<>();

    static {
        CONTEXTUAL_COLORS.put("com.whatsapp", new ContextualColor("whatsapp",
            new int[]{Color.parseColor("#25D366"), Color.parseColor("#128C7E"),
                     Color.parseColor("#075E54"), Color.parseColor("#DCF8C6")}));
        CONTEXTUAL_COLORS.put("com.instagram.android", new ContextualColor("instagram",
            new int[]{Color.parseColor("#E1306C"), Color.parseColor("#F56040"),
                     Color.parseColor("#F77737"), Color.parseColor("#FCAF45")}));
        CONTEXTUAL_COLORS.put("com.facebook.katana", new ContextualColor("facebook",
            new int[]{Color.parseColor("#1877F2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1E88E5"), Color.parseColor("#1565C0")}));
        CONTEXTUAL_COLORS.put("com.twitter.android", new ContextualColor("twitter",
            new int[]{Color.parseColor("#1DA1F2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1976D2"), Color.parseColor("#0D47A1")}));
        CONTEXTUAL_COLORS.put("com.google.android.gm", new ContextualColor("gmail",
            new int[]{Color.parseColor("#EA4335"), Color.parseColor("#FBBC05"),
                     Color.parseColor("#34A853"), Color.parseColor("#4285F4")}));
        CONTEXTUAL_COLORS.put("com.google.android.apps.messaging", new ContextualColor("messages",
            new int[]{Color.parseColor("#1A73E8"), Color.parseColor("#4285F4"),
                     Color.parseColor("#1565C0"), Color.parseColor("#0D47A1")}));
        CONTEXTUAL_COLORS.put("com.spotify.music", new ContextualColor("spotify",
            new int[]{Color.parseColor("#1DB954"), Color.parseColor("#1ED760"),
                     Color.parseColor("#1DB954"), Color.parseColor("#191414")}));
        CONTEXTUAL_COLORS.put("com.google.android.youtube", new ContextualColor("youtube",
            new int[]{Color.parseColor("#FF0000"), Color.parseColor("#FF4444"),
                     Color.parseColor("#CC0000"), Color.parseColor("#990000")}));
        CONTEXTUAL_COLORS.put("android", new ContextualColor("system",
            new int[]{Color.parseColor("#1976D2"), Color.parseColor("#42A5F5"),
                     Color.parseColor("#1E88E5"), Color.parseColor("#1565C0")}));
    }

    private MonetContextualColorHelper() {
    }

    public static boolean applyForPackage(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }

        boolean enabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.MONET_CONTEXTUAL_ENABLED, 0) == 1;
        if (!enabled) {
            return false;
        }

        ContextualColor color = CONTEXTUAL_COLORS.get(packageName);
        if (color == null) {
            return false;
        }

        String colorString = color.colors[0] + "," + color.colors[1] + ","
                + color.colors[2] + "," + color.colors[3];
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.MONET_CONTEXTUAL_COLORS, colorString);
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.MONET_LAST_NOTIFICATION_PACKAGE, packageName);

        String style = MonetThemeApplier.getCurrentStyle(context);
        return MonetThemeApplier.applyPreset(context, color.colors[0], style,
                Math.abs(color.id.hashCode()) % 1000);
    }

    private static final class ContextualColor {
        final String id;
        final int[] colors;

        ContextualColor(String id, int[] colors) {
            this.id = id;
            this.colors = colors;
        }
    }
}
