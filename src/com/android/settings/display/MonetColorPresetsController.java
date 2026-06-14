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

public class MonetColorPresetsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_COLOR_PRESET = "monet_color_preset";

    public MonetColorPresetsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof androidx.preference.ListPreference) {
            androidx.preference.ListPreference listPreference =
                    (androidx.preference.ListPreference) preference;
            String currentPreset = Settings.Secure.getString(
                    mContext.getContentResolver(), KEY_COLOR_PRESET);
            if (currentPreset == null || currentPreset.isEmpty()) {
                currentPreset = "french_violet";
            }
            listPreference.setValue(currentPreset);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String presetName = (String) newValue;

        if ("wallpaper".equals(presetName)) {
            Settings.Secure.putString(mContext.getContentResolver(), KEY_COLOR_PRESET, presetName);
            return MonetThemeApplier.clearToWallpaper(mContext);
        }

        int seedColor = getPresetSeedColor(presetName);
        if (seedColor == 0) {
            return false;
        }

        String style = MonetThemeApplier.getCurrentStyle(mContext);
        Settings.Secure.putString(mContext.getContentResolver(), KEY_COLOR_PRESET, presetName);
        return MonetThemeApplier.applyPreset(mContext, seedColor, style,
                Math.abs(presetName.hashCode()) % 1000);
    }

    public int getPresetSeedColor(String name) {
        switch (name) {
            case "wallpaper": return 0;
            case "french_violet": return 0xFF8921C2;
            case "rose_bonbon": return 0xFFFE39A4;
            case "vivid_sky_blue": return 0xFF25C4F8;
            case "orange_pantone": return 0xFFFF5800;
            case "off_red_rgb": return 0xFFFF0000;
            case "blue_orchid": return 0xFF2F46FA;
            case "screamin_green": return 0xFF55FC77;
            case "orange_crayola": return 0xFFFB7443;
            case "spring_green": return 0xFF00FF7F;
            case "lime_green": return 0xFF00D61C;
            case "night": return 0xFF021307;
            case "palatinate_blue": return 0xFF0346F4;
            case "federal_blue": return 0xFF00005A;
            case "steel_pink": return 0xFFBF2ED5;
            case "celeste": return 0xFFB6FFFE;
            case "ultramarine": return 0xFFF20BF8;
            case "dark_purple": return 0xFF150390;
            case "pear": return 0xFFD9DA40;
            case "pastel_pink": return 0xFFD99EB0;
            case "tan": return 0xFFCFBA8F;
            case "lilac": return 0xFFC9A5C0;
            case "steel_blue": return 0xFF3F99AC;
            case "deep_moss_green": return 0xFF375F47;
            case "dark_sea_green": return 0xFF93B285;
            case "alabaster": return 0xFFFFFFFF;
            case "american_blue": return 0xFF2E4370;
            case "turquoise": return 0xFF00CED1;
            case "red_accent": return 0xFFFF5252;
            case "purple_accent": return 0xFFE040FB;
            case "indigo_accent": return 0xFF536DFE;
            case "cyan_accent": return 0xFF18FFFF;
            case "green_accent": return 0xFF69F0AE;
            case "yellow_accent": return 0xFFFFFF00;
            default: return 0xFF4285F4;
        }
    }
}
