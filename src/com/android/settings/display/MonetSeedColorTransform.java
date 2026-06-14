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

import androidx.core.graphics.ColorUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Boosts Lineage extended / night preset seeds before they are applied to Monet.
 */
public final class MonetSeedColorTransform {
    private static final float EXTENDED_SATURATION_FACTOR = 1.55f;
    private static final float EXTENDED_LIGHTNESS_FACTOR = 0.95f;
    private static final float NIGHT_SATURATION_FACTOR = 1.30f;
    private static final float NIGHT_LIGHTNESS_FACTOR = 0.30f;

    private static final Set<String> EXTENDED_BUNDLE_IDS = new HashSet<>(Arrays.asList(
            "slate", "coral", "mint", "cobalt", "amber", "lavender", "teal", "ruby", "sage",
            "plum", "sand", "ocean", "rose", "copper", "frost", "olive", "grape", "peach",
            "pine", "graphite"));

    private MonetSeedColorTransform() {
    }

    public static int enhanceForBundle(String bundleId, int color) {
        if (bundleId != null && bundleId.startsWith("night_")) {
            return enhanceNight(color);
        }
        if (bundleId != null && EXTENDED_BUNDLE_IDS.contains(bundleId)) {
            return enhanceExtended(color);
        }
        return color;
    }

    public static int enhanceExtended(int color) {
        return transform(color, EXTENDED_SATURATION_FACTOR, EXTENDED_LIGHTNESS_FACTOR, 0.08f);
    }

    public static int enhanceNight(int color) {
        return transform(color, NIGHT_SATURATION_FACTOR, NIGHT_LIGHTNESS_FACTOR, 0.05f);
    }

    private static int transform(int color, float saturationFactor, float lightnessFactor,
            float minLightness) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[1] = Math.min(1f, hsl[1] * saturationFactor);
        hsl[2] = Math.max(minLightness, Math.min(1f, hsl[2] * lightnessFactor));
        return 0xFF000000 | (ColorUtils.HSLToColor(hsl) & 0x00FFFFFF);
    }
}
