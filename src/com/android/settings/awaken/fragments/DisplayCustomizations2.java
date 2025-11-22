/*
 * Copyright (C) 2021 Wave-OS
 * Copyright (C) 2025 LineageOS
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

package com.android.settings.awaken.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.os.UserHandle;
import android.util.Log;
import android.content.om.OverlayInfo;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import com.android.internal.util.android.ThemeUtils;
import lineageos.providers.LineageSettings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class DisplayCustomizations2 extends SettingsPreferenceFragment {

    private static final String TAG = "DisplayCustomizations2";
    private static final String HIDE_IME_SPACE_KEY = "hide_ime_space_enable";
    private static final String HIDE_IME_SPACE_OVERLAY_PKG = "com.custom.overlay.systemui.gestural.hide_ime_space";
    
    private ThemeUtils mThemeUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_customizations2);
        mThemeUtils = ThemeUtils.getInstance(getActivity());
        initializePreferences();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    /**
     * Initialize preferences for future expansion.
     * Wallpaper blur features moved to wallpaper_background_settings.xml
     */
    private void initializePreferences() {
        SwitchPreference hideImeSpacePref = findPreference(HIDE_IME_SPACE_KEY);
        if (hideImeSpacePref != null) {
            // Check if taskbar is enabled - hide preference if taskbar is enabled
            boolean isTaskbarEnabled = false;
            try {
                isTaskbarEnabled = LineageSettings.System.getInt(getContext().getContentResolver(),
                        LineageSettings.System.ENABLE_TASKBAR, isLargeScreen(getContext()) ? 1 : 0) == 1;
            } catch (Exception | NoClassDefFoundError e) {
                // Fallback safely if LineageSettings is not found
                Log.w(TAG, "Error checking taskbar status", e);
            }

            if (isTaskbarEnabled) {
                getPreferenceScreen().removePreference(hideImeSpacePref);
                return;
            }

            // Always show the preference, even if overlay is not available
            // The overlay will be applied if available, otherwise the setting will still work
            boolean isEnabled = Settings.System.getIntForUser(getContext().getContentResolver(),
                    HIDE_IME_SPACE_KEY, 0, UserHandle.USER_CURRENT) != 0;
            hideImeSpacePref.setChecked(isEnabled);
            hideImeSpacePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                Settings.System.putIntForUser(getContext().getContentResolver(),
                        HIDE_IME_SPACE_KEY, value ? 1 : 0, UserHandle.USER_CURRENT);
                // Try to update overlay if available, but don't fail if it's not
                try {
                    if (isOverlayPackageAvailable("android.theme.customization.hide_ime_space", HIDE_IME_SPACE_OVERLAY_PKG)) {
                        updateHideImeSpaceOverlay(value);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Overlay not available, but setting saved", e);
                }
                return true;
            });
        }
    }
    
    private boolean isLargeScreen(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }
    
    private boolean isOverlayPackageAvailable(String category, String packageName) {
        try {
            if (mThemeUtils == null) {
                return false;
            }
            List<OverlayInfo> infos = mThemeUtils.getOverlayInfos(category);
            for (OverlayInfo info : infos) {
                if (packageName.equals(info.packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking overlay availability", e);
        }
        return false;
    }
    
    private void updateHideImeSpaceOverlay(boolean enabled) {
        if (mThemeUtils == null) {
            return;
        }
        try {
            mThemeUtils.setOverlayEnabled(
                    "android.theme.customization.hide_ime_space",
                    enabled ? HIDE_IME_SPACE_OVERLAY_PKG : "android",
                    "android"
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating overlay", e);
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_customizations2) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
