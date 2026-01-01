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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

/**
 * System Basic Defaults Settings Fragment
 * Displays default app cards in a RecyclerView
 */
public class SystemBasicDefaultsSettings extends SettingsPreferenceFragment {
    private static final String TAG = "SystemBasicDefaultsSettings";
    private RecyclerView mRecyclerView;
    private SystemBasicDefaultsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.system_basic_defaults);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Find the LayoutPreference view and then the RecyclerView
        View rootView = getView();
        if (rootView != null) {
            // The LayoutPreference is embedded in the preference list
            // We need to find it by traversing the view hierarchy
            findAndSetupRecyclerView(rootView);
        }
    }
    
    private void findAndSetupRecyclerView(View rootView) {
        // Look for the RecyclerView in the view hierarchy
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
            return;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SystemBasicDefaultsAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        
        Log.d(TAG, "RecyclerView setup complete");
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

