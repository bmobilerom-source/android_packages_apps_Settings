/*
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

package com.epic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeviceSecurityBottomSheet extends BottomSheetDialogFragment {

    public static DeviceSecurityBottomSheet newInstance() {
        return new DeviceSecurityBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, 0);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Set transparent background for the bottom sheet dialog container
        // The content view already has the adaptive background
        if (getDialog() != null && getDialog().getWindow() != null) {
            android.view.Window window = getDialog().getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.device_security_bottom_sheet, container, false);
            
            // Load the preference fragment
            if (savedInstanceState == null) {
                try {
                    androidx.fragment.app.FragmentManager fragmentManager = getChildFragmentManager();
                    if (fragmentManager != null && !fragmentManager.isStateSaved()) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.device_security_preference_container, new DeviceSecurityPreferenceFragment())
                                .commitAllowingStateLoss();
                    }
                } catch (Exception e) {
                    android.util.Log.e("DeviceSecurityBottomSheet", "Error loading preference fragment", e);
                }
            }
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("DeviceSecurityBottomSheet", "Error in onCreateView", e);
            // Return a simple view if there's an error
            Context ctx = getContext();
            if (ctx != null) {
                return new android.widget.FrameLayout(ctx);
            }
            // Last resort - return null and let the system handle it
            return null;
        }
    }

    public static class DeviceSecurityPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                // Load preferences without controllers - controllers won't work in bottom sheet context
                setPreferencesFromResource(R.xml.device_security_bottom_sheet, rootKey);
                
                // Manually set up click listeners for preferences that need them
                androidx.preference.Preference simLockPref = findPreference("sim_lock_settings");
                if (simLockPref != null) {
                    simLockPref.setOnPreferenceClickListener(preference -> {
                        try {
                            android.content.Intent intent = new android.content.Intent("android.intent.action.MAIN")
                                    .setClassName("com.android.settings", 
                                        "com.android.settings.Settings$IccLockSettingsActivity");
                            startActivity(intent);
                            // Dismiss the bottom sheet from parent
                            if (getParentFragment() instanceof DeviceSecurityBottomSheet) {
                                ((DeviceSecurityBottomSheet) getParentFragment()).dismiss();
                            }
                        } catch (Exception e) {
                            android.util.Log.e("DeviceSecurityBottomSheet", "Error opening SIM lock", e);
                        }
                        return true;
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("DeviceSecurityBottomSheet", "Error in onCreatePreferences", e);
            }
        }
    }
}

