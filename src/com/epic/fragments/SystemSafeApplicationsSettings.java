/*
 * Copyright (C) 2022 EpicROM-AOSP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epic.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.applications.DefaultAppsPreferenceController;
import com.android.settings.applications.HibernatedAppsPreferenceController;
import com.android.settings.core.BasePreferenceController;

public class SystemSafeApplicationsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private DefaultAppsPreferenceController mDefaultAppsController;
    private HibernatedAppsPreferenceController mHibernatedAppsController;
    private Preference mSeeAllAppsPref;
    private Preference mDefaultAppsPref;
    private Preference mHibernatedAppsPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.system_safe_applications);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        
        // Initialize DefaultAppsPreferenceController
        mDefaultAppsPref = prefScreen.findPreference("default_apps");
        if (mDefaultAppsPref != null) {
            mDefaultAppsController = new DefaultAppsPreferenceController(getContext(), "default_apps");
            mDefaultAppsController.displayPreference(prefScreen);
            // Set summary from controller
            CharSequence summary = mDefaultAppsController.getSummary();
            if (summary != null) {
                mDefaultAppsPref.setSummary(summary);
            }
            mDefaultAppsController.updateState(mDefaultAppsPref);
        }
        
        // Initialize see_all_apps preference
        mSeeAllAppsPref = prefScreen.findPreference("see_all_apps");
        if (mSeeAllAppsPref != null) {
            // Ensure the preference is visible and clickable
            mSeeAllAppsPref.setVisible(true);
            // The fragment attribute should handle navigation automatically
        }
        
        // Make sure the category is visible
        PreferenceCategory recentAppsCategory = (PreferenceCategory) prefScreen.findPreference("recent_apps_category");
        if (recentAppsCategory != null) {
            recentAppsCategory.setVisible(true);
        }
        
        // Initialize HibernatedAppsPreferenceController
        mHibernatedAppsPref = prefScreen.findPreference("hibernated_apps");
        if (mHibernatedAppsPref != null) {
            mHibernatedAppsController = new HibernatedAppsPreferenceController(getContext(), "hibernated_apps");
            mHibernatedAppsController.displayPreference(prefScreen);
            // Register with lifecycle for summary updates
            getSettingsLifecycle().addObserver(mHibernatedAppsController);
            // Set initial summary
            CharSequence summary = mHibernatedAppsController.getSummary();
            if (summary != null) {
                mHibernatedAppsPref.setSummary(summary);
            }
            mHibernatedAppsController.updateState(mHibernatedAppsPref);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Update controllers when fragment resumes
        if (mDefaultAppsController != null && mDefaultAppsPref != null) {
            mDefaultAppsController.updateState(mDefaultAppsPref);
            // Update summary
            CharSequence summary = mDefaultAppsController.getSummary();
            if (summary != null) {
                mDefaultAppsPref.setSummary(summary);
            }
        }
        
        // Update hibernated apps controller (it implements LifecycleObserver and will update automatically)
        if (mHibernatedAppsController != null && mHibernatedAppsPref != null) {
            mHibernatedAppsController.updateState(mHibernatedAppsPref);
            // Update summary
            CharSequence summary = mHibernatedAppsController.getSummary();
            if (summary != null) {
                mHibernatedAppsPref.setSummary(summary);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

