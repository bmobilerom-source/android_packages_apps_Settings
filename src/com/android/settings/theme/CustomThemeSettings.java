/*
 * Copyright (C) 2024 The LineageOS Project
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

package com.android.settings.theme;

import android.app.UiModeManager;
import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.theme.CustomThemeHelper;

/**
 * Fragment for custom system theme selection with categorized themes.
 * Dark mode themes: Black, Transparent, Espresso (auto-switch to dark mode)
 * Light mode themes: Custom Blue, Expressive
 * Animated blur: Works in both light and dark mode
 */
public class CustomThemeSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "CustomThemeSettings";
    private static final String KEY_CUSTOM_THEME = "custom_theme";

    // All themes work only in dark mode: Default (0), Black (1), Transparent (2), Espresso (4), Custom Blue (5), Expressive (7), Animated (6), Animated Wallpaper Blur (8), Custom Picture (9)
    private static final int[] DARK_THEMES = {0, 1, 2, 4, 5, 6, 7, 8, 9};

    private ListPreference mThemePreference;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            addPreferencesFromResource(R.xml.custom_theme_settings);
        } catch (Exception e) {
            Log.e(TAG, "Error loading preference XML", e);
            return;
        }

        // Initialize theme preference (all themes work only in dark mode)
        mThemePreference = (ListPreference) findPreference(KEY_CUSTOM_THEME);
        if (mThemePreference != null) {
            mThemePreference.setOnPreferenceChangeListener(this);
        }

        // Setup wallpaper background bottom sheet
        Preference wallpaperBackgroundPref = findPreference("wallpaper_background_preference");
        if (wallpaperBackgroundPref != null) {
            wallpaperBackgroundPref.setOnPreferenceClickListener(preference -> {
                try {
                    com.epic.fragments.WallpaperBackgroundBottomSheet bottomSheet = 
                            com.epic.fragments.WallpaperBackgroundBottomSheet.newInstance();
                    if (getParentFragmentManager() != null) {
                        bottomSheet.show(getParentFragmentManager(), "WallpaperBackgroundBottomSheet");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error showing wallpaper background bottom sheet", e);
                }
                return true;
            });
        }

        // Update preference states based on current theme
        updatePreferenceStates();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Update description visibility after view is created
        if (view != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateDescriptionVisibility();
                }
            }, 100);
        } else {
            updateDescriptionVisibility();
        }
    }

    /**
     * Updates preference states to match current theme value.
     */
    private void updatePreferenceStates() {
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            return;
        }

        int currentTheme = CustomThemeHelper.getCurrentTheme(context);
        
        // Check if current theme is valid
        boolean isValidTheme = false;
        for (int darkTheme : DARK_THEMES) {
            if (currentTheme == darkTheme) {
                isValidTheme = true;
                break;
            }
        }
        
        // Update theme preference
        if (mThemePreference != null) {
            if (isValidTheme) {
                String value = String.valueOf(currentTheme);
                mThemePreference.setValue(value);
                updatePreferenceSummary(mThemePreference, currentTheme);
            } else {
                // Set to default theme
                mThemePreference.setValue(String.valueOf(DARK_THEMES[0]));
            }
        }
    }

    /**
     * Updates summary for a ListPreference based on theme value.
     */
    private void updatePreferenceSummary(ListPreference preference, int themeValue) {
        if (preference == null) {
            return;
        }
        try {
            CharSequence[] entries = preference.getEntries();
            CharSequence[] entryValues = preference.getEntryValues();
            if (entries != null && entryValues != null) {
                for (int i = 0; i < entryValues.length; i++) {
                    if (String.valueOf(themeValue).equals(entryValues[i].toString())) {
                        preference.setSummary(entries[i]);
                        return;
                    }
                }
            }
            // Fallback to theme name
            preference.setSummary(CustomThemeHelper.getThemeName(getContext(), themeValue));
        } catch (Exception e) {
            Log.e(TAG, "Error updating preference summary", e);
            preference.setSummary(CustomThemeHelper.getThemeName(getContext(), themeValue));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == null || newValue == null) {
            Log.w(TAG, "Preference or newValue is null");
            return false;
        }
        
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            Log.e(TAG, "Context is null, cannot apply theme");
            return false;
        }
        
        String key = preference.getKey();
        
        try {
            if (KEY_CUSTOM_THEME.equals(key)) {
                // Theme selected (all themes work only in dark mode)
                String value = (String) newValue;
                int themeValue = Integer.parseInt(value);
                
                // Set the theme
                boolean themeSet = CustomThemeHelper.setTheme(context, themeValue);
                if (!themeSet) {
                    Log.e(TAG, "Failed to set theme: " + themeValue);
                    return false;
                }
                
                // Only switch to dark mode if not default theme
                if (themeValue != CustomThemeHelper.THEME_DEFAULT) {
                    switchToDarkMode(context);
                }
                
                updatePreferenceSummary(mThemePreference, themeValue);
                applyThemeChanges(context, themeValue);
                return true;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid theme value: " + newValue, e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error updating theme preference", e);
            return false;
        }
        
        return false;
    }

    /**
     * Switches system to dark mode.
     */
    private void switchToDarkMode(Context context) {
        try {
            int nightMode = Settings.Secure.getIntForUser(
                    context.getContentResolver(),
                    Settings.Secure.UI_NIGHT_MODE,
                    UiModeManager.MODE_NIGHT_AUTO,
                    UserHandle.USER_CURRENT);
            
            // Only switch if not already in dark mode
            if (nightMode != UiModeManager.MODE_NIGHT_YES) {
                Settings.Secure.putIntForUser(
                        context.getContentResolver(),
                        Settings.Secure.UI_NIGHT_MODE,
                        UiModeManager.MODE_NIGHT_YES,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "Switched system to dark mode");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error switching to dark mode", e);
        }
    }

    /**
     * Switches system to light mode.
     */
    private void switchToLightMode(Context context) {
        try {
            int nightMode = Settings.Secure.getIntForUser(
                    context.getContentResolver(),
                    Settings.Secure.UI_NIGHT_MODE,
                    UiModeManager.MODE_NIGHT_AUTO,
                    UserHandle.USER_CURRENT);
            
            // Only switch if not already in light mode
            if (nightMode != UiModeManager.MODE_NIGHT_NO) {
                Settings.Secure.putIntForUser(
                        context.getContentResolver(),
                        Settings.Secure.UI_NIGHT_MODE,
                        UiModeManager.MODE_NIGHT_NO,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "Switched system to light mode");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error switching to light mode", e);
        }
    }

    /**
     * Applies theme changes and updates UI.
     */
    private void applyThemeChanges(Context context, int themeValue) {
        Log.d(TAG, "Theme setting saved: " + themeValue);
        
        // Update description visibility
        View rootView = getView();
        if (rootView != null) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    updateDescriptionVisibility();
                }
            });
        } else {
            updateDescriptionVisibility();
        }
        
        // Recreate activity to apply theme changes immediately
        if (getActivity() != null) {
            getActivity().recreate();
        }
        
        // Show toast to user
        android.widget.Toast.makeText(context,
                getString(R.string.custom_theme_applied, 
                        CustomThemeHelper.getThemeName(context, themeValue)),
                android.widget.Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Updates visibility of theme descriptions based on selected theme.
     */
    private void updateDescriptionVisibility() {
        try {
            Context context = getContext();
            if (context == null) {
                context = getActivity();
            }
            if (context == null) {
                return;
            }
            
            int currentTheme = CustomThemeHelper.getCurrentTheme(context);
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            
            // Hide all descriptions first
            String[] descriptionKeys = {
                "theme_default_desc",
                "theme_black_desc",
                "theme_transparent_desc",
                "theme_espresso_desc",
                "theme_custom_blue_desc",
                "theme_animated_desc",
                "theme_expressive_desc",
                "theme_animated_wallpaper_blur_desc",
                "theme_custom_picture_desc"
            };
            
            for (String key : descriptionKeys) {
                Preference desc = findPreference(key);
                if (desc != null) {
                    desc.setVisible(false);
                }
            }
            
            // Show description for current theme
            String currentKey = null;
            switch (currentTheme) {
                case 0: currentKey = "theme_default_desc"; break;
                case 1: currentKey = "theme_black_desc"; break;
                case 2: currentKey = "theme_transparent_desc"; break;
                case 4: currentKey = "theme_espresso_desc"; break;
                case 5: currentKey = "theme_custom_blue_desc"; break;
                case 6: currentKey = "theme_animated_desc"; break;
                case 7: currentKey = "theme_expressive_desc"; break;
                case 8: currentKey = "theme_animated_wallpaper_blur_desc"; break;
                case 9: currentKey = "theme_custom_picture_desc"; break;
                default:
                    currentKey = "theme_default_desc";
                    break;
            }
            
            if (currentKey != null) {
                Preference currentDesc = findPreference(currentKey);
                if (currentDesc != null) {
                    currentDesc.setVisible(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating description visibility", e);
        }
    }
}
