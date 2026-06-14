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

/**
 * Preset color metadata loaded from the ThemesStub APK resources.
 */
public final class StubColorPreset {
    public final String bundleId;
    public final String displayName;
    public final int seedColor;
    public final String style;
    public final int index;

    public StubColorPreset(String bundleId, String displayName, int seedColor, String style,
            int index) {
        this.bundleId = bundleId;
        this.displayName = displayName;
        this.seedColor = seedColor;
        this.style = style;
        this.index = index;
    }
}
