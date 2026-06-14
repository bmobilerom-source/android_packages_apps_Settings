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
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads extended and night color presets from the ThemesStub resource APK.
 */
public final class ThemesStubResourceLoader {
    private static final String STUB_PACKAGE = "com.android.customization.themes";
    private static final String EXTENDED_BUNDLES_ARRAY = "lineage_extended_color_bundles";
    private static final String NIGHT_BUNDLES_ARRAY = "lineage_night_color_bundles";
    private static final String BUNDLE_NAME_PREFIX = "bundle_name_";
    private static final String COLOR_SECONDARY_PREFIX = "color_secondary_";
    private static final String COLOR_STYLE_PREFIX = "color_style_";
    private static final String DEFAULT_STYLE = "TONAL_SPOT";
    private static final int EXTENDED_INDEX_OFFSET = 18;
    private static final int NIGHT_INDEX_OFFSET = 38;

    private ThemesStubResourceLoader() {
    }

    public static boolean isStubAvailable(Context context) {
        try {
            context.getPackageManager().getResourcesForApplication(STUB_PACKAGE);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static List<StubColorPreset> loadExtendedPresets(Context context) {
        return loadPresets(context, EXTENDED_BUNDLES_ARRAY, EXTENDED_INDEX_OFFSET);
    }

    public static List<StubColorPreset> loadNightPresets(Context context) {
        return loadPresets(context, NIGHT_BUNDLES_ARRAY, NIGHT_INDEX_OFFSET);
    }

    private static List<StubColorPreset> loadPresets(Context context, String arrayName,
            int indexOffset) {
        Resources stubResources;
        try {
            stubResources = context.getPackageManager().getResourcesForApplication(STUB_PACKAGE);
        } catch (PackageManager.NameNotFoundException e) {
            return Collections.emptyList();
        }

        int arrayId = stubResources.getIdentifier(arrayName, "array", STUB_PACKAGE);
        if (arrayId == 0) {
            return Collections.emptyList();
        }

        String[] bundleIds = stubResources.getStringArray(arrayId);
        List<StubColorPreset> presets = new ArrayList<>(bundleIds.length);
        for (int i = 0; i < bundleIds.length; i++) {
            String bundleId = bundleIds[i];
            String displayName = resolveDisplayName(stubResources, bundleId);
            int seedColor = resolveSeedColor(stubResources, bundleId);
            String style = resolveStyle(stubResources, bundleId);
            if (displayName != null && seedColor != 0) {
                presets.add(new StubColorPreset(bundleId, displayName, seedColor, style,
                        indexOffset + i));
            }
        }
        return presets;
    }

    private static String resolveDisplayName(Resources stubResources, String bundleId) {
        int nameId = stubResources.getIdentifier(BUNDLE_NAME_PREFIX + bundleId, "string",
                STUB_PACKAGE);
        if (nameId == 0) {
            return null;
        }
        return stubResources.getString(nameId);
    }

    private static int resolveSeedColor(Resources stubResources, String bundleId) {
        int colorId = stubResources.getIdentifier(COLOR_SECONDARY_PREFIX + bundleId, "color",
                STUB_PACKAGE);
        if (colorId == 0) {
            return 0;
        }
        int rawColor = stubResources.getColor(colorId, null);
        return MonetSeedColorTransform.enhanceForBundle(bundleId, rawColor);
    }

    private static String resolveStyle(Resources stubResources, String bundleId) {
        int styleId = stubResources.getIdentifier(COLOR_STYLE_PREFIX + bundleId, "string",
                STUB_PACKAGE);
        if (styleId == 0) {
            return DEFAULT_STYLE;
        }
        return stubResources.getString(styleId);
    }
}
