/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings.display.darkmode;

import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for Dark UI Mode
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DarkModeSettingsFragment extends DashboardFragment {

    private static final String TAG = "DarkModeSettingsFrag";
    private static final String DARK_THEME_END_TIME = "dark_theme_end_time";
    private static final String DARK_THEME_START_TIME = "dark_theme_start_time";
    private DarkModeObserver mContentObserver;
    private DarkModeCustomPreferenceController mCustomStartController;
    private DarkModeCustomPreferenceController mCustomEndController;
    private SystemThemeVividPreferenceController mThemeVividController;
    private SystemThemeSnowpaintPreferenceController mThemeSnowpaintController;
    private SystemThemeEspressoPreferenceController mThemeEspressoController;
    private ContentObserver mThemeObserver;
    private static final int DIALOG_START_TIME = 0;
    private static final int DIALOG_END_TIME = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getContext();
        if (context == null) {
            return;
        }
        mContentObserver = new DarkModeObserver(context);
        
        // Observe SYSTEM_CUSTOM_THEME changes to keep switches in sync
        // Note: Observer will be registered in onResume() after controllers are initialized
        mThemeObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                if (!selfChange) {
                    // Update all theme preference states
                    // Use post to ensure this runs after controllers are initialized
                    new Handler().post(() -> {
                        if (mThemeVividController != null && 
                            mThemeSnowpaintController != null && 
                            mThemeEspressoController != null) {
                            updateThemePreferenceStates();
                        }
                    });
                }
            }
        };
    }
    
    private void updateThemePreferenceStates() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null && mThemeVividController != null && 
            mThemeSnowpaintController != null && mThemeEspressoController != null) {
            Preference vividPref = screen.findPreference("system_theme_vivid");
            Preference snowpaintPref = screen.findPreference("system_theme_snowpaint");
            Preference espressoPref = screen.findPreference("system_theme_espresso");
            
            if (vividPref != null) mThemeVividController.updateState(vividPref);
            if (snowpaintPref != null) mThemeSnowpaintController.updateState(snowpaintPref);
            if (espressoPref != null) mThemeEspressoController.updateState(espressoPref);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for changes only while visible.
        mContentObserver.subscribe(() -> {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen != null && mCustomStartController != null && mCustomEndController != null) {
                mCustomStartController.displayPreference(preferenceScreen);
                mCustomEndController.displayPreference(preferenceScreen);
                updatePreferenceStates();
            }
        });
        
        // Register theme observer after controllers are initialized
        final Context context = getContext();
        if (context != null && mThemeObserver != null) {
            try {
                context.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Secure.SYSTEM_CUSTOM_THEME),
                        false,
                        mThemeObserver,
                        UserHandle.USER_CURRENT);
            } catch (Exception e) {
                // Ignore if already registered
            }
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList<>(5);
        mCustomStartController = new DarkModeCustomPreferenceController(getContext(),
                DARK_THEME_START_TIME, this);
        mCustomEndController = new DarkModeCustomPreferenceController(getContext(),
                DARK_THEME_END_TIME, this);
        controllers.add(mCustomStartController);
        controllers.add(mCustomEndController);
        
        // Add theme preference controllers
        mThemeVividController = new SystemThemeVividPreferenceController(context, "system_theme_vivid");
        mThemeSnowpaintController = new SystemThemeSnowpaintPreferenceController(context, "system_theme_snowpaint");
        mThemeEspressoController = new SystemThemeEspressoPreferenceController(context, "system_theme_espresso");
        
        controllers.add(mThemeVividController);
        controllers.add(mThemeSnowpaintController);
        controllers.add(mThemeEspressoController);
        
        return controllers;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for state changes.
        mContentObserver.unsubscribe();
        if (mThemeObserver != null && getContext() != null) {
            getContext().getContentResolver().unregisterContentObserver(mThemeObserver);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (DARK_THEME_END_TIME.equals(preference.getKey())) {
            showDialog(DIALOG_END_TIME);
            return true;
        } else if (DARK_THEME_START_TIME.equals(preference.getKey())) {
            showDialog(DIALOG_START_TIME);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void refresh() {
        this.updatePreferenceStates();
    }

    @Override
    public Dialog onCreateDialog(final int dialogId) {
        if (dialogId == DIALOG_START_TIME || dialogId == DIALOG_END_TIME) {
            if (dialogId == DIALOG_START_TIME) {
                return mCustomStartController.getDialog();
            } else {
                return mCustomEndController.getDialog();
            }
        }
        return super.onCreateDialog(dialogId);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.dark_mode_settings;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_dark_theme;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DARK_UI_SETTINGS;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case DIALOG_START_TIME:
                return SettingsEnums.DIALOG_DARK_THEME_SET_START_TIME;
            case DIALOG_END_TIME:
                return SettingsEnums.DIALOG_DARK_THEME_SET_END_TIME;
            default:
                return 0;
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.dark_mode_settings) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return !context.getSystemService(PowerManager.class).isPowerSaveMode();
                }
            };

}
