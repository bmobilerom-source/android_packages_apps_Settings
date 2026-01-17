/*
 * Copyright (C) 2025 BashaMobile
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

package com.epic.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.widget.LayoutPreference;

import java.util.List;
import java.util.ArrayList;

/**
 * System Basic Defaults Settings Fragment
 * Displays default app cards in a RecyclerView with grid layout
 */
public class SystemBasicDefaultsSettings extends SettingsPreferenceFragment {
    private static final String TAG = "SystemBasicDefaultsSettings";
    private RecyclerView mRecyclerView;
    private SystemBasicDefaultsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            addPreferencesFromResource(R.xml.system_basic_defaults);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load preferences", e);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Find the LayoutPreference view and then the RecyclerView
            View rootView = getView();
            if (rootView != null) {
                findAndSetupRecyclerView(rootView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup RecyclerView", e);
        }
    }
    
    private void findAndSetupRecyclerView(View rootView) {
        // Look for the LayoutPreference first
        android.app.Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Activity is null");
            return;
        }

        // Try to find via preference screen
        androidx.preference.PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            androidx.preference.Preference layoutPref = screen.findPreference("system_basic_defaults");
            if (layoutPref instanceof LayoutPreference) {
                LayoutPreference lp = (LayoutPreference) layoutPref;
                RecyclerView rv = lp.findViewById(R.id.system_basic_defaults_recycler);
                if (rv != null) {
                    mRecyclerView = rv;
                    setupRecyclerView();
                    return;
                }
            }
        }
        
        // Fallback: search recursively in view hierarchy
        View recyclerView = rootView.findViewById(R.id.system_basic_defaults_recycler);
        if (recyclerView instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) recyclerView;
            setupRecyclerView();
            return;
        }
        
        // If not found directly, search recursively
        if (rootView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) rootView;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child.getId() == R.id.system_basic_defaults_recycler && child instanceof RecyclerView) {
                    mRecyclerView = (RecyclerView) child;
                    setupRecyclerView();
                    return;
                }
                if (child instanceof ViewGroup) {
                    findAndSetupRecyclerView(child);
                    if (mRecyclerView != null) {
                        return;
                    }
                }
            }
        }
    }

    private void setupRecyclerView() {
        if (mRecyclerView == null || getContext() == null) {
            Log.e(TAG, "RecyclerView or context is null");
            return;
        }

        android.app.Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Activity is null");
            return;
        }

        try {
            // Use 3 columns: Large left card spans 2 columns, others span 1
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            mRecyclerView.setLayoutManager(layoutManager);

            // Create card items - all bottom cards lead to Anatolia settings
            List<SystemBasicDefaultsAdapter.CardItem> items = new ArrayList<>();
            
            // Large left card - Aurora Store (biggest card)
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_LARGE_LEFT,
                    R.string.anatolia_settings_title, // Use available string
                    R.string.anatolia_settings_summary, // Use available string
                    null,
                    "aurora_store", // Special key for intent launch
                    "Aurora Store"));
            
            // Second card - Default Apps
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_ABOUT_US,
                    R.string.anatolia_settings_title, // Use available string
                    R.string.anatolia_settings_summary, // Use available string
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "default_apps")); // Special key for intent launch
            
            // Circular buttons - all lead to Anatolia settings for now
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));
            
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));
            
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));
            
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));
            
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));
            
            items.add(new SystemBasicDefaultsAdapter.CardItem(
                    SystemBasicDefaultsAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                    R.string.anatolia_settings_title,
                    R.string.anatolia_settings_summary,
                    android.R.drawable.ic_menu_preferences, // Use system icon
                    "com.epic.Anatolia"));

            mAdapter = new SystemBasicDefaultsAdapter(activity, items, getMetricsCategory());
            mRecyclerView.setAdapter(mAdapter);
            
            Log.d(TAG, "RecyclerView setup complete with " + items.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup RecyclerView", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
