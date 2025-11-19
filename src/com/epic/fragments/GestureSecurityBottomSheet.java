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
import com.android.settings.preferences.ui.AdaptiveSwitchPreference;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class GestureSecurityBottomSheet extends BottomSheetDialogFragment {

    public static GestureSecurityBottomSheet newInstance() {
        return new GestureSecurityBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, 0);
    }
    
    @Override
    public void onStart() {
        super.onStart();
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
            View view = inflater.inflate(R.layout.gesture_security_bottom_sheet, container, false);
            
            if (savedInstanceState == null) {
                try {
                    androidx.fragment.app.FragmentManager fragmentManager = getChildFragmentManager();
                    if (fragmentManager != null && !fragmentManager.isStateSaved()) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.gesture_security_preference_container, new GestureSecurityPreferenceFragment())
                                .commitAllowingStateLoss();
                    }
                } catch (Exception e) {
                    android.util.Log.e("GestureSecurityBottomSheet", "Error loading preference fragment", e);
                }
            }
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("GestureSecurityBottomSheet", "Error in onCreateView", e);
            Context ctx = getContext();
            if (ctx != null) {
                return new android.widget.FrameLayout(ctx);
            }
            return null;
        }
    }

    public static class GestureSecurityPreferenceFragment extends PreferenceFragmentCompat {
        private GestureSecurityAdapter mAdapter;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                setPreferencesFromResource(R.xml.gesture_security_bottom_sheet, rootKey);
                
                Context context = getContext();
                if (context == null) {
                    return;
                }
                
                mAdapter = new GestureSecurityAdapter(context);
                
                // Setup preferences using adapter - similar to wallpaper background
                AdaptiveSwitchPreference clipboardOverlay = findPreference("show_clipboard_overlay");
                if (clipboardOverlay != null) {
                    mAdapter.setupClipboardOverlayPreference(clipboardOverlay);
                }
                
                AdaptiveSwitchPreference noStorageRestrict = findPreference("no_storage_restrict");
                if (noStorageRestrict != null) {
                    mAdapter.setupNoStorageRestrictPreference(noStorageRestrict);
                }
                
                AdaptiveSwitchPreference windowIgnoreSecure = findPreference("window_ignore_secure");
                if (windowIgnoreSecure != null) {
                    mAdapter.setupWindowIgnoreSecurePreference(windowIgnoreSecure);
                }
                
                AdaptiveSwitchPreference secureLockscreenQs = findPreference("secure_lockscreen_qs_disabled");
                if (secureLockscreenQs != null) {
                    mAdapter.setupSecureLockscreenQsPreference(secureLockscreenQs);
                }
            } catch (Exception e) {
                android.util.Log.e("GestureSecurityBottomSheet", "Error in onCreatePreferences", e);
            }
        }
        
        @Override
        public void onResume() {
            super.onResume();
            // Refresh preference states when bottom sheet is shown
            refreshPreferenceStates();
        }
        
        private void refreshPreferenceStates() {
            Context context = getContext();
            if (context == null || mAdapter == null) {
                return;
            }
            
            // Refresh all preference states to ensure they reflect current Settings values
            AdaptiveSwitchPreference clipboardOverlay = findPreference("show_clipboard_overlay");
            if (clipboardOverlay != null) {
                clipboardOverlay.setChecked(GestureSecurityHelper.isClipboardOverlayEnabled(context));
            }
            
            AdaptiveSwitchPreference noStorageRestrict = findPreference("no_storage_restrict");
            if (noStorageRestrict != null) {
                noStorageRestrict.setChecked(GestureSecurityHelper.isNoStorageRestrictEnabled(context));
            }
            
            AdaptiveSwitchPreference windowIgnoreSecure = findPreference("window_ignore_secure");
            if (windowIgnoreSecure != null) {
                windowIgnoreSecure.setChecked(GestureSecurityHelper.isWindowIgnoreSecureEnabled(context));
            }
            
            AdaptiveSwitchPreference secureLockscreenQs = findPreference("secure_lockscreen_qs_disabled");
            if (secureLockscreenQs != null) {
                secureLockscreenQs.setChecked(GestureSecurityHelper.isSecureLockscreenQsDisabled(context));
            }
        }
    }
}
